package com.example.domain.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.domain.model.CompassData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Manages device orientation sensors and emits updates via [compassDataFlow].
 * Prioritizes [Sensor.TYPE_ROTATION_VECTOR] for high accuracy,
 * falling back to [Sensor.TYPE_ACCELEROMETER] + [Sensor.TYPE_MAGNETIC_FIELD] with low-pass filtering.
 */
class CompassSensorManager(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val isHardwareSensorAvailable: Boolean =
        rotationVectorSensor != null || (accelerometerSensor != null && magnetometerSensor != null)

    private val _compassDataFlow = MutableStateFlow(
        CompassData(
            isSensorAvailable = isHardwareSensorAvailable,
            sensorType = when {
                rotationVectorSensor != null -> "Rotation Vector (Fused 9-Axis)"
                isHardwareSensorAvailable -> "Accelerometer + Magnetometer"
                else -> "Hardware Sensor Unavailable"
            }
        )
    )
    val compassDataFlow: StateFlow<CompassData> = _compassDataFlow.asStateFlow()

    private var isListening = false

    // Fallback sensor arrays
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // Rotation matrix calculations
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Low-pass filter smoothing coefficient (0.0 < alpha <= 1.0)
    private val alpha = 0.15f

    // Current sensor accuracy
    private var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    private var currentFieldStrength = 45.0f

    // Simulation job for testing or when sensors are missing
    private var simulationJob: Job? = null

    /**
     * Registers the appropriate hardware sensor listeners to begin receiving updates.
     * Must be called in onResume() / onStart().
     */
    fun startListening() {
        if (isListening || sensorManager == null) return

        if (!isHardwareSensorAvailable) {
            startSimulation(isPermanentFallback = true)
            return
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                this,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            // Also register magnetometer if present to measure magnetic field strength
            magnetometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } else {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            magnetometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        isListening = true
    }

    /**
     * Unregisters sensor listeners to save battery.
     * Must be called in onPause() / onStop().
     */
    fun stopListening() {
        if (!isListening && simulationJob == null) return

        if (sensorManager != null) {
            sensorManager.unregisterListener(this)
        }
        stopSimulation()
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                // High-precision rotation vector sensor
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val azimuthRad = orientationAngles[0]
                val pitchRad = orientationAngles[1]
                val rollRad = orientationAngles[2]

                val azimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f)
                val pitchDeg = Math.toDegrees(pitchRad.toDouble()).toFloat()
                val rollDeg = Math.toDegrees(rollRad.toDouble()).toFloat()

                _compassDataFlow.value = CompassData(
                    azimuthDegrees = azimuthDeg,
                    pitchDegrees = pitchDeg,
                    rollDegrees = rollDeg,
                    accuracy = currentAccuracy,
                    sensorType = "Rotation Vector (Fused)",
                    magneticFieldStrength = currentFieldStrength,
                    isSensorAvailable = true,
                    isSimulated = false,
                    timestamp = event.timestamp
                )
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // Low-pass filter for gravity values
                applyLowPassFilter(event.values, gravityValues)
                hasGravity = true

                if (hasGeomagnetic) {
                    computeOrientationFromGravityAndGeomagnetic(event.timestamp)
                }
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                // Low-pass filter for geomagnetic values
                applyLowPassFilter(event.values, geomagneticValues)
                hasGeomagnetic = true

                // Calculate total magnetic flux density B = sqrt(Bx² + By² + Bz²)
                val bx = event.values[0]
                val by = event.values[1]
                val bz = event.values[2]
                currentFieldStrength = sqrt(bx * bx + by * by + bz * bz)

                if (hasGravity && rotationVectorSensor == null) {
                    computeOrientationFromGravityAndGeomagnetic(event.timestamp)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_ROTATION_VECTOR || sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            currentAccuracy = accuracy
            _compassDataFlow.value = _compassDataFlow.value.copy(accuracy = accuracy)
        }
    }

    private fun applyLowPassFilter(input: FloatArray, output: FloatArray) {
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    private fun computeOrientationFromGravityAndGeomagnetic(timestamp: Long) {
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            gravityValues,
            geomagneticValues
        )
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val azimuthRad = orientationAngles[0]
            val pitchRad = orientationAngles[1]
            val rollRad = orientationAngles[2]

            val azimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f)
            val pitchDeg = Math.toDegrees(pitchRad.toDouble()).toFloat()
            val rollDeg = Math.toDegrees(rollRad.toDouble()).toFloat()

            _compassDataFlow.value = CompassData(
                azimuthDegrees = azimuthDeg,
                pitchDegrees = pitchDeg,
                rollDegrees = rollDeg,
                accuracy = currentAccuracy,
                sensorType = "Accelerometer + Magnetometer",
                magneticFieldStrength = currentFieldStrength,
                isSensorAvailable = true,
                isSimulated = false,
                timestamp = timestamp
            )
        }
    }

    /**
     * Starts realistic compass simulation for devices/emulators lacking sensors,
     * or for live UI demonstration.
     */
    fun startSimulation(isPermanentFallback: Boolean = false) {
        if (simulationJob?.isActive == true) return

        simulationJob = externalScope.launch {
            var currentHeading = 35.0f
            var timeStep = 0.0

            while (isActive) {
                // Produce smooth natural motion with gentle drift and slight pitch/roll leveling movement
                timeStep += 0.04
                val headingDelta = (Math.sin(timeStep * 0.4) * 0.6 + Math.cos(timeStep * 0.15) * 0.3).toFloat()
                currentHeading = (currentHeading + headingDelta + 360f) % 360f

                val simulatedPitch = (Math.sin(timeStep * 0.3) * 1.8).toFloat()
                val simulatedRoll = (Math.cos(timeStep * 0.25) * 1.5).toFloat()

                _compassDataFlow.value = CompassData(
                    azimuthDegrees = currentHeading,
                    pitchDegrees = simulatedPitch,
                    rollDegrees = simulatedRoll,
                    accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
                    sensorType = if (isPermanentFallback) "Simulation Mode (No Hardware)" else "Demo Simulation",
                    magneticFieldStrength = 48.2f,
                    isSensorAvailable = isHardwareSensorAvailable,
                    isSimulated = true
                )
                delay(33) // ~30 fps update
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    fun toggleSimulation() {
        if (simulationJob?.isActive == true) {
            stopSimulation()
            if (isHardwareSensorAvailable) {
                startListening()
            }
        } else {
            startSimulation(isPermanentFallback = false)
        }
    }
}

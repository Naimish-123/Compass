package com.example.domain.model

/**
 * Encapsulates raw and calculated orientation telemetry from the device sensors.
 *
 * @property azimuthDegrees Heading azimuth in degrees [0, 360), where 0° is North.
 * @property pitchDegrees Tilt around X axis in degrees [-180, 180]. Flat is 0°.
 * @property rollDegrees Tilt around Y axis in degrees [-90, 90]. Flat is 0°.
 * @property accuracy Sensor accuracy constant from SensorManager (e.g. SENSOR_STATUS_ACCURACY_HIGH).
 * @property sensorType Description of which sensor was used (Rotation Vector or Accel+Mag).
 * @property magneticFieldStrength Magnetic field magnitude in microteslas (μT).
 * @property isSensorAvailable True if device has required hardware sensors.
 * @property isSimulated True if running in emulator demo mode.
 */
data class CompassData(
    val azimuthDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val accuracy: Int = 3, // SENSOR_STATUS_ACCURACY_HIGH by default
    val sensorType: String = "Rotation Vector",
    val magneticFieldStrength: Float = 45f, // Earth's typical field is ~30-60 μT
    val isSensorAvailable: Boolean = true,
    val isSimulated: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.location.CompassLocationProvider
import com.example.domain.model.CardinalDirection
import com.example.domain.sensor.CompassSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.roundToInt

class CompassViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = CompassSensorManager(application, viewModelScope)
    private val locationProvider = CompassLocationProvider(application)

    private val _uiState = MutableStateFlow(CompassUiState())
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    private var previousCardinal: CardinalDirection? = null

    // Callback for cardinal tick haptics with direction
    var onCardinalCrossed: ((CardinalDirection) -> Unit)? = null

    init {
        // Collect sensor data flow
        sensorManager.compassDataFlow
            .onEach { data ->
                processCompassData(data)
            }
            .launchIn(viewModelScope)

        refreshLocation()
    }

    /**
     * Call when activity resumes or becomes visible.
     */
    fun startSensors() {
        refreshLocation()
        sensorManager.startListening()
    }

    /**
     * Call when activity pauses or stops to conserve device battery.
     */
    fun stopSensors() {
        sensorManager.stopListening()
    }

    fun refreshLocation() {
        val locationInfo = locationProvider.getLastKnownLocation()
        _uiState.update { current ->
            current.copy(
                locationInfo = locationInfo,
                declinationDegrees = locationInfo.declinationDegrees
            )
        }
    }

    private fun processCompassData(data: com.example.domain.model.CompassData) {
        _uiState.update { state ->
            val magneticHeading = data.azimuthDegrees
            val effectiveHeading = if (state.isTrueNorth && state.declinationDegrees != 0f) {
                ((magneticHeading + state.declinationDegrees) % 360f + 360f) % 360f
            } else {
                magneticHeading
            }

            // Compute shortest angle delta to avoid 0° to 360° spin glitches
            val currentContinuous = state.continuousVisualAngle
            var delta = (effectiveHeading - (currentContinuous % 360f)) % 360f
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f
            val newContinuousAngle = currentContinuous + delta

            val cardinal = CardinalDirection.fromDegrees(effectiveHeading)
            val sixteenPoint = CardinalDirection.to16Point(effectiveHeading)

            // Trigger haptic feedback if crossing into a primary cardinal direction
            if (cardinal != previousCardinal && (cardinal == CardinalDirection.NORTH ||
                        cardinal == CardinalDirection.EAST ||
                        cardinal == CardinalDirection.SOUTH ||
                        cardinal == CardinalDirection.WEST)) {
                previousCardinal = cardinal
                if (state.isHapticsEnabled) {
                    onCardinalCrossed?.invoke(cardinal)
                }
            } else if (cardinal != previousCardinal) {
                previousCardinal = cardinal
            }

            // Bearing deviation
            val deviation = state.lockedBearing?.let { locked ->
                var dev = (effectiveHeading - locked) % 360f
                if (dev > 180f) dev -= 360f
                if (dev < -180f) dev += 360f
                dev
            }

            val isLevel = abs(data.pitchDegrees) < 2.0f && abs(data.rollDegrees) < 2.0f

            // Auto suggest calibration if accuracy is unreliable or low on real sensor
            val suggestCalibration = !data.isSimulated && data.isSensorAvailable &&
                    data.accuracy == android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE &&
                    !state.showCalibrationDialog

            state.copy(
                magneticHeading = magneticHeading,
                displayHeading = effectiveHeading,
                continuousVisualAngle = newContinuousAngle,
                pitch = data.pitchDegrees,
                roll = data.rollDegrees,
                isLevel = isLevel,
                accuracy = data.accuracy,
                sensorType = data.sensorType,
                magneticFieldStrength = data.magneticFieldStrength,
                isHardwareAvailable = data.isSensorAvailable,
                isSimulated = data.isSimulated,
                bearingDeviation = deviation,
                cardinalDirection = cardinal,
                sixteenPointDirection = sixteenPoint,
                showCalibrationDialog = if (suggestCalibration) true else state.showCalibrationDialog
            )
        }
    }

    fun toggleTrueNorth() {
        _uiState.update { current ->
            current.copy(isTrueNorth = !current.isTrueNorth)
        }
    }

    fun toggleBearingLock() {
        _uiState.update { current ->
            if (current.lockedBearing == null) {
                val lockTarget = current.displayHeading.roundToInt().toFloat()
                current.copy(
                    lockedBearing = lockTarget,
                    bearingDeviation = 0f
                )
            } else {
                current.copy(
                    lockedBearing = null,
                    bearingDeviation = null
                )
            }
        }
    }

    fun clearLockedBearing() {
        _uiState.update { it.copy(lockedBearing = null, bearingDeviation = null) }
    }

    fun showCalibrationDialog() {
        _uiState.update { it.copy(showCalibrationDialog = true) }
    }

    fun dismissCalibrationDialog() {
        _uiState.update { it.copy(showCalibrationDialog = false) }
    }

    fun toggleSimulation() {
        sensorManager.toggleSimulation()
    }

    fun toggleHaptics() {
        _uiState.update { it.copy(isHapticsEnabled = !it.isHapticsEnabled) }
    }

    fun setManualHeading(degrees: Float) {
        sensorManager.setManualAzimuth(degrees)
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
    }
}

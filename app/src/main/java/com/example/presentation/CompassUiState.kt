package com.example.presentation

import com.example.domain.location.CompassLocationProvider
import com.example.domain.model.CardinalDirection

enum class CompassMode {
    DIRECTION,
    LEVEL
}

data class CompassUiState(
    val currentMode: CompassMode = CompassMode.DIRECTION,
    val magneticHeading: Float = 0f,
    val displayHeading: Float = 0f,
    val continuousVisualAngle: Float = 0f,
    val isTrueNorth: Boolean = false,
    val declinationDegrees: Float = 0f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val totalTilt: Float = 0f,
    val isLevelZero: Boolean = false,
    val isLevel: Boolean = false,
    val accuracy: Int = 3,
    val sensorType: String = "Rotation Vector",
    val magneticFieldStrength: Float = 45f,
    val isHardwareAvailable: Boolean = true,
    val isSimulated: Boolean = false,
    val lockedBearing: Float? = null,
    val bearingDeviation: Float? = null,
    val cardinalDirection: CardinalDirection = CardinalDirection.NORTH,
    val sixteenPointDirection: String = "N",
    val showCalibrationDialog: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val isHapticsEnabled: Boolean = true,
    val locationInfo: CompassLocationProvider.LocationInfo = CompassLocationProvider.LocationInfo()
)

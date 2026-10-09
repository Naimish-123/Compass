package com.example.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CompassUiState
import com.example.ui.theme.HyperOSBorder
import com.example.ui.theme.HyperOSTextMuted
import com.example.ui.theme.HyperOSTextPrimary
import com.example.ui.theme.HyperOSTextSecondary
import com.example.ui.theme.XiaomiRed

/**
 * Xiaomi HyperOS Settings Bottom Sheet (version 17.1.x design).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyperOSSettingsSheet(
    uiState: CompassUiState,
    onToggleTrueNorth: () -> Unit,
    onToggleHaptics: () -> Unit,
    onOpenCalibration: () -> Unit,
    onToggleSimulation: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .testTag("hyperos_settings_sheet")
        ) {
            // Sheet Title
            Text(
                text = "Compass Settings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HyperOSTextPrimary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Group Container
            Surface(
                color = Color(0xFF242426),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // True North Toggle
                    SettingsSwitchRow(
                        title = "True North",
                        subtitle = if (uiState.declinationDegrees != 0f) {
                            String.format("Magnetic declination: %+.1f°", uiState.declinationDegrees)
                        } else {
                            "Use geographic North instead of magnetic North"
                        },
                        icon = Icons.Default.Explore,
                        checked = uiState.isTrueNorth,
                        onCheckedChange = { onToggleTrueNorth() }
                    )

                    HorizontalDivider(color = HyperOSBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Haptics Toggle
                    SettingsSwitchRow(
                        title = "Vibrate on Cardinal Points",
                        subtitle = "Subtle tactile feedback for North, South, East, West and 0° Level",
                        icon = Icons.Default.Vibration,
                        checked = uiState.isHapticsEnabled,
                        onCheckedChange = { onToggleHaptics() }
                    )

                    HorizontalDivider(color = HyperOSBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Calibration Action
                    SettingsActionRow(
                        title = "Calibrate Compass",
                        subtitle = "Wave device in figure-8 motion",
                        icon = Icons.Default.CompassCalibration,
                        onClick = {
                            onDismiss()
                            onOpenCalibration()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware Telemetry Container
            Surface(
                color = Color(0xFF242426),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = HyperOSTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hardware Telemetry",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HyperOSTextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sensor: ${uiState.sensorType}",
                        style = MaterialTheme.typography.bodySmall.copy(color = HyperOSTextPrimary)
                    )
                    Text(
                        text = String.format("Magnetic Flux: %.1f μT", uiState.magneticFieldStrength),
                        style = MaterialTheme.typography.bodySmall.copy(color = HyperOSTextSecondary)
                    )
                    Text(
                        text = if (uiState.isHardwareAvailable) "Status: Hardware OK" else "Status: Running Simulation Mode",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (uiState.isHardwareAvailable) Color(0xFF30D158) else Color(0xFFFF9500)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HyperOSTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = HyperOSTextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = HyperOSTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = XiaomiRed,
                uncheckedThumbColor = Color(0xFF8E8E93),
                uncheckedTrackColor = Color(0xFF38383A)
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HyperOSTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = HyperOSTextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = HyperOSTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = HyperOSTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

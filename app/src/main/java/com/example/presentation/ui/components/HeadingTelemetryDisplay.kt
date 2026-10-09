package com.example.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CompassUiState
import com.example.ui.theme.CalibrationWarningYellow
import com.example.ui.theme.CardinalCyan
import com.example.ui.theme.CompassNeedleRed
import com.example.ui.theme.LevelBubbleGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueNorthCyan
import kotlin.math.roundToInt

@Composable
fun HeadingTelemetryDisplay(
    uiState: CompassUiState,
    onToggleTrueNorth: () -> Unit,
    onShowCalibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingInt = uiState.displayHeading.roundToInt() % 360

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Digital Azimuth & Direction Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$headingInt°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    color = TextPrimary
                ),
                modifier = Modifier.testTag("heading_degrees_text")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.Start) {
                // Direction badge (e.g. "NE" or "N")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (uiState.cardinalDirection.shortName == "N") {
                        CompassNeedleRed.copy(alpha = 0.2f)
                    } else {
                        CardinalCyan.copy(alpha = 0.15f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (uiState.cardinalDirection.shortName == "N") CompassNeedleRed else CardinalCyan
                    )
                ) {
                    Text(
                        text = uiState.sixteenPointDirection,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("cardinal_direction_badge"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.cardinalDirection.shortName == "N") CompassNeedleRed else CardinalCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = uiState.cardinalDirection.fullName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // North Mode & Accuracy Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // True North / Magnetic North Toggle Chip
            AssistChip(
                onClick = onToggleTrueNorth,
                label = {
                    Text(
                        text = if (uiState.isTrueNorth) "True North" else "Magnetic North",
                        color = if (uiState.isTrueNorth) TrueNorthCyan else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = if (uiState.isTrueNorth) TrueNorthCyan else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (uiState.isTrueNorth) TrueNorthCyan.copy(alpha = 0.12f) else SlateSurface
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = if (uiState.isTrueNorth) TrueNorthCyan else SlateBorder
                ),
                modifier = Modifier.testTag("true_north_chip")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Calibration / Accuracy Chip
            val (accuracyLabel, accuracyColor) = when (uiState.accuracy) {
                android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Accuracy: High" to LevelBubbleGreen
                android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Accuracy: Med" to CardinalCyan
                android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Accuracy: Low" to CalibrationWarningYellow
                else -> "Uncalibrated" to CompassNeedleRed
            }

            AssistChip(
                onClick = onShowCalibration,
                label = {
                    Text(
                        text = accuracyLabel,
                        color = accuracyColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accuracyColor)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = SlateSurface
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = SlateBorder
                ),
                modifier = Modifier.testTag("calibration_chip")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Telemetry Grid Card (Pitch, Roll, Field Strength, Declination)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryItem(
                    label = "PITCH",
                    value = String.format("%.1f°", uiState.pitch),
                    highlight = uiState.isLevel
                )
                Box(modifier = Modifier.size(1.dp, 28.dp).background(SlateBorder))
                TelemetryItem(
                    label = "ROLL",
                    value = String.format("%.1f°", uiState.roll),
                    highlight = uiState.isLevel
                )
                Box(modifier = Modifier.size(1.dp, 28.dp).background(SlateBorder))
                TelemetryItem(
                    label = "FIELD",
                    value = String.format("%.0f μT", uiState.magneticFieldStrength),
                    highlight = false
                )
                Box(modifier = Modifier.size(1.dp, 28.dp).background(SlateBorder))
                TelemetryItem(
                    label = "DECL",
                    value = if (uiState.declinationDegrees != 0f) {
                        String.format("%+.1f°", uiState.declinationDegrees)
                    } else {
                        "0.0°"
                    },
                    highlight = uiState.isTrueNorth
                )
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    highlight: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (highlight) LevelBubbleGreen else TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        )
    }
}

package com.example.presentation.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalibrationWarningYellow
import com.example.ui.theme.CardinalCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SensorUnavailableBanner(
    isHardwareAvailable: Boolean,
    isSimulated: Boolean,
    currentHeading: Float,
    onManualHeadingChange: (Float) -> Unit,
    onToggleSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isHardwareAvailable || isSimulated) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .testTag("sensor_unavailable_banner"),
            colors = CardDefaults.cardColors(
                containerColor = SlateSurfaceVariant
            ),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (!isHardwareAvailable) CalibrationWarningYellow else SlateBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (!isHardwareAvailable) Icons.Default.SensorsOff else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (!isHardwareAvailable) CalibrationWarningYellow else CardinalCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (!isHardwareAvailable) "Hardware Sensor Unavailable" else "Demo Simulation Active",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    FilledTonalButton(
                        onClick = onToggleSimulation,
                        modifier = Modifier.testTag("toggle_simulation_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SlateSurface
                        )
                    ) {
                        Text(
                            text = if (isSimulated) "Pause Sim" else "Resume Sim",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (!isHardwareAvailable) {
                        "This device lacks a magnetic sensor or rotation vector. Use the interactive slider below or auto-simulation to test."
                    } else {
                        "Hardware sensors are present, but simulation is currently running for demonstration."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Manual Azimuth slider for interactive testing
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Heading: ${currentHeading.roundToInt()}°",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(85.dp)
                    )

                    Slider(
                        value = currentHeading,
                        onValueChange = onManualHeadingChange,
                        valueRange = 0f..359f,
                        colors = SliderDefaults.colors(
                            thumbColor = CardinalCyan,
                            activeTrackColor = CardinalCyan,
                            inactiveTrackColor = SlateBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_heading_slider")
                    )
                }
            }
        }
    }
}

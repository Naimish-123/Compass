package com.example.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CompassUiState
import com.example.ui.theme.HyperOSBlack
import com.example.ui.theme.HyperOSTextMuted
import com.example.ui.theme.HyperOSTextPrimary
import com.example.ui.theme.HyperOSTextSecondary
import com.example.ui.theme.XiaomiRed
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Xiaomi HyperOS Direction Screen.
 * Strictly presents Direction information (Heading Azimuth, Cardinal point, Geographic Coordinates, Dial).
 * No level or pitch/roll tools are shown here.
 */
@Composable
fun DirectionView(
    uiState: CompassUiState,
    onDialClick: () -> Unit,
    onClearBearing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingInt = uiState.displayHeading.roundToInt() % 360

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HyperOSBlack)
            .testTag("direction_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Xiaomi Heading Header & Geographic Coordinates
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Heading Number and Direction (e.g. "245° SW")
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$headingInt°",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        color = HyperOSTextPrimary
                    ),
                    modifier = Modifier.testTag("direction_heading_text")
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = uiState.cardinalDirection.fullName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (uiState.cardinalDirection.shortName == "N") XiaomiRed else HyperOSTextSecondary
                    ),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .testTag("direction_cardinal_text")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Coordinates in Xiaomi format: "23°07'12" N  72°38'45" E"
            if (uiState.locationInfo.hasLocation) {
                val lat = uiState.locationInfo.latitude
                val lon = uiState.locationInfo.longitude

                val latText = formatCoordinate(abs(lat), if (lat >= 0) "N" else "S")
                val lonText = formatCoordinate(abs(lon), if (lon >= 0) "E" else "W")

                Text(
                    text = "$latText   $lonText",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = HyperOSTextSecondary,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.testTag("direction_coordinates_text")
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Altitude ${uiState.locationInfo.altitudeMeters.roundToInt()} m",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = HyperOSTextMuted
                    )
                )
            } else {
                Text(
                    text = if (uiState.isTrueNorth && uiState.declinationDegrees != 0f) {
                        String.format("True North (Declination %+.1f°)", uiState.declinationDegrees)
                    } else {
                        "Magnetic North"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = HyperOSTextSecondary
                    )
                )
            }
        }

        // CENTER: Signature Xiaomi Compass Dial
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            HyperOSCompassDial(
                uiState = uiState,
                onDialClick = onDialClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            )
        }

        // BOTTOM: Optional Bearing Lock Banner if locked
        AnimatedVisibility(
            visible = uiState.lockedBearing != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            uiState.lockedBearing?.let { locked ->
                val deviation = uiState.bearingDeviation ?: 0f
                val absDev = abs(deviation).roundToInt()
                val onCourse = absDev <= 2

                Surface(
                    color = Color(0xFF1C1C1E),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .clickable(onClick = onClearBearing)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (onCourse) XiaomiRed else Color(0xFFFF9500),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (onCourse) "On Course (${locked.roundToInt()}°)" else "Target: ${locked.roundToInt()}°  •  Turn $absDev° ${if (deviation > 0) "Left" else "Right"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HyperOSTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Unlock",
                            tint = HyperOSTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

/**
 * Formats decimal latitude/longitude into Xiaomi degree-minute format (e.g. 23°07' N).
 */
private fun formatCoordinate(deg: Double, direction: String): String {
    val d = deg.toInt()
    val m = ((deg - d) * 60).toInt()
    return String.format("%d°%02d' %s", d, m, direction)
}

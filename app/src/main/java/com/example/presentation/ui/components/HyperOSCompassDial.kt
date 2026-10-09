package com.example.presentation.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CompassUiState
import com.example.ui.theme.XiaomiRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Xiaomi HyperOS signature compass dial.
 * Clean, high-precision dark aesthetic with thin radial ticks,
 * crisp typography, and red North indicator.
 */
@Composable
fun HyperOSCompassDial(
    uiState: CompassUiState,
    onDialClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Snappy physical-spring rotation angle with instant stopping response
    val animatedRotation by animateFloatAsState(
        targetValue = -uiState.continuousVisualAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "hyperOSCompassRotation"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .clickable(onClick = onDialClick)
            .testTag("hyperos_compass_dial"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // 1. Base dark background & subtle boundary ring
            drawCircle(
                color = Color(0xFF141416),
                radius = radius,
                center = center
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Inner circle track
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = radius * 0.88f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Rotating Compass Rose
            rotate(degrees = animatedRotation, pivot = center) {
                drawHyperOSTicksAndLabels(
                    center = center,
                    radius = radius,
                    textMeasurer = textMeasurer
                )

                // Locked Bearing Flag Marker
                uiState.lockedBearing?.let { lockedBearing ->
                    drawHyperOSLockedMarker(
                        bearing = lockedBearing,
                        center = center,
                        radius = radius
                    )
                }
            }

            // 3. Center Fixed Reticle (Xiaomi minimalist crosshairs)
            drawCenterCrosshair(
                center = center,
                radius = radius
            )

            // 4. Fixed Top Indicator (Xiaomi signature red lubber pointer)
            drawTopRedPointer(
                center = center,
                radius = radius
            )
        }
    }
}

private fun DrawScope.drawHyperOSTicksAndLabels(
    center: Offset,
    radius: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val cardinalDistance = radius * 0.73f
    val numberDistance = radius * 0.58f
    val outerTickR = radius * 0.98f

    for (deg in 0 until 360 step 2) {
        val angleRad = (deg - 90) * (PI / 180.0)
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()

        when {
            // Cardinal Points (0, 90, 180, 270)
            deg % 90 == 0 -> {
                val tickLength = radius * 0.11f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)
                val isNorth = (deg == 0)

                drawLine(
                    color = if (isNorth) XiaomiRed else Color.White,
                    start = start,
                    end = end,
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val label = when (deg) {
                    0 -> "N"
                    90 -> "E"
                    180 -> "S"
                    else -> "W"
                }

                val textLayout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNorth) XiaomiRed else Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                val textX = center.x + cosA * cardinalDistance - (textLayout.size.width / 2f)
                val textY = center.y + sinA * cardinalDistance - (textLayout.size.height / 2f)
                drawText(textLayout, topLeft = Offset(textX, textY))
            }

            // Major 30-degree ticks and numbers
            deg % 30 == 0 -> {
                val tickLength = radius * 0.08f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)

                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = start,
                    end = end,
                    strokeWidth = 1.5.dp.toPx()
                )

                val numLayout = textMeasurer.measure(
                    text = "$deg",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.55f),
                        fontFamily = FontFamily.SansSerif
                    )
                )
                val textX = center.x + cosA * numberDistance - (numLayout.size.width / 2f)
                val textY = center.y + sinA * numberDistance - (numLayout.size.height / 2f)
                drawText(numLayout, topLeft = Offset(textX, textY))
            }

            // Medium 10-degree ticks
            deg % 10 == 0 -> {
                val tickLength = radius * 0.055f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)

                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = start,
                    end = end,
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // Minor 2-degree ticks
            else -> {
                val tickLength = radius * 0.03f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)

                drawLine(
                    color = Color.White.copy(alpha = 0.22f),
                    start = start,
                    end = end,
                    strokeWidth = 0.8.dp.toPx()
                )
            }
        }
    }
}

private fun DrawScope.drawCenterCrosshair(
    center: Offset,
    radius: Float
) {
    val crosshairLen = radius * 0.20f
    val gap = 6.dp.toPx()

    // Horizontal crosshair
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(center.x - crosshairLen, center.y),
        end = Offset(center.x - gap, center.y),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(center.x + gap, center.y),
        end = Offset(center.x + crosshairLen, center.y),
        strokeWidth = 1.dp.toPx()
    )

    // Vertical crosshair
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(center.x, center.y - crosshairLen),
        end = Offset(center.x, center.y - gap),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(center.x, center.y + gap),
        end = Offset(center.x, center.y + crosshairLen),
        strokeWidth = 1.dp.toPx()
    )

    // Center circular reticle
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = 4.dp.toPx(),
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun DrawScope.drawTopRedPointer(
    center: Offset,
    radius: Float
) {
    val topY = center.y - radius
    val pointerLength = 16.dp.toPx()

    // Xiaomi top lubber indicator: crisp red vertical needle line at 12 o'clock
    drawLine(
        color = XiaomiRed,
        start = Offset(center.x, topY),
        end = Offset(center.x, topY + pointerLength),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Highlight dot at top rim
    drawCircle(
        color = XiaomiRed,
        radius = 2.5.dp.toPx(),
        center = Offset(center.x, topY - 2.dp.toPx())
    )
}

private fun DrawScope.drawHyperOSLockedMarker(
    bearing: Float,
    center: Offset,
    radius: Float
) {
    val angleRad = (bearing - 90) * (PI / 180.0)
    val markerR = radius * 0.98f
    val tipX = center.x + cos(angleRad).toFloat() * markerR
    val tipY = center.y + sin(angleRad).toFloat() * markerR

    val perpRad = angleRad + (PI / 2.0)
    val baseHalfWidth = 7.dp.toPx()
    val baseR = markerR - 14.dp.toPx()

    val path = Path().apply {
        moveTo(tipX, tipY)
        lineTo(
            (center.x + cos(angleRad) * baseR + cos(perpRad) * baseHalfWidth).toFloat(),
            (center.y + sin(angleRad) * baseR + sin(perpRad) * baseHalfWidth).toFloat()
        )
        lineTo(
            (center.x + cos(angleRad) * baseR - cos(perpRad) * baseHalfWidth).toFloat(),
            (center.y + sin(angleRad) * baseR - sin(perpRad) * baseHalfWidth).toFloat()
        )
        close()
    }

    drawPath(path, color = Color(0xFFFF9500))
}

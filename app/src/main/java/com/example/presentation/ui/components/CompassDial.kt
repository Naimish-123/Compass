package com.example.presentation.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.CardinalCyan
import com.example.ui.theme.CompassNeedleRed
import com.example.ui.theme.CompassNeedleRedDark
import com.example.ui.theme.CompassNeedleSilver
import com.example.ui.theme.CompassNeedleSilverLight
import com.example.ui.theme.LevelBubbleGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueNorthCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassDial(
    uiState: CompassUiState,
    modifier: Modifier = Modifier
) {
    // Smooth physical-spring rotation angle
    val animatedRotation by animateFloatAsState(
        targetValue = -uiState.continuousVisualAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "compassRotation"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("compass_dial"),
        contentAlignment = Alignment.Center
    ) {
        // Main rotating dial canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // 1. Draw Outer Bezel & Dark Circular Dial Background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SlateSurfaceVariant,
                        SlateSurface,
                        SlateDark
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Outer metallic rim
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        SlateBorder,
                        Color(0xFF475569),
                        SlateBorder,
                        Color(0xFF64748B),
                        SlateBorder
                    ),
                    center = center
                ),
                radius = radius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // Inner subtle track
            drawCircle(
                color = SlateBorder.copy(alpha = 0.5f),
                radius = radius * 0.88f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Rotating Compass Rose (Dial markings, ticks, cardinal points)
            rotate(degrees = animatedRotation, pivot = center) {
                drawTicksAndMarkings(
                    center = center,
                    radius = radius,
                    textMeasurer = textMeasurer,
                    isTrueNorth = uiState.isTrueNorth
                )

                // Draw locked bearing indicator if user locked a heading
                uiState.lockedBearing?.let { lockedBearing ->
                    drawLockedBearingMarker(
                        bearing = lockedBearing,
                        center = center,
                        radius = radius
                    )
                }
            }

            // 3. Center Fixed Reticle / Needle Indicator
            drawCenterNeedleAndPivot(
                center = center,
                radius = radius,
                pitch = uiState.pitch,
                roll = uiState.roll,
                isLevel = uiState.isLevel
            )

            // 4. Fixed Top Lubber Line (Heading Arrow pointing to current direction)
            drawTopLubberLine(
                center = center,
                radius = radius,
                isTrueNorth = uiState.isTrueNorth
            )
        }
    }
}

private fun DrawScope.drawTicksAndMarkings(
    center: Offset,
    radius: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    isTrueNorth: Boolean
) {
    val cardinalDistance = radius * 0.74f
    val numberDistance = radius * 0.60f
    val outerTickR = radius * 0.96f

    for (deg in 0 until 360 step 2) {
        val angleRad = (deg - 90) * (PI / 180.0)
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()

        when {
            // Major cardinal points (0, 90, 180, 270)
            deg % 90 == 0 -> {
                val tickLength = radius * 0.12f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)
                val tickColor = if (deg == 0) CompassNeedleRed else TextPrimary

                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Cardinal text
                val label = when (deg) {
                    0 -> "N"
                    90 -> "E"
                    180 -> "S"
                    else -> "W"
                }
                val labelColor = if (deg == 0) CompassNeedleRed else TextPrimary
                val textLayout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = labelColor,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                val textX = center.x + cosA * cardinalDistance - (textLayout.size.width / 2f)
                val textY = center.y + sinA * cardinalDistance - (textLayout.size.height / 2f)
                drawText(textLayout, topLeft = Offset(textX, textY))
            }

            // Intermediate ordinal points (45, 135, 225, 315)
            deg % 45 == 0 -> {
                val tickLength = radius * 0.08f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)

                drawLine(
                    color = CardinalCyan,
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val label = when (deg) {
                    45 -> "NE"
                    135 -> "SE"
                    225 -> "SW"
                    else -> "NW"
                }
                val textLayout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CardinalCyan.copy(alpha = 0.9f),
                        fontFamily = FontFamily.SansSerif
                    )
                )
                val textX = center.x + cosA * cardinalDistance - (textLayout.size.width / 2f)
                val textY = center.y + sinA * cardinalDistance - (textLayout.size.height / 2f)
                drawText(textLayout, topLeft = Offset(textX, textY))
            }

            // Every 30 degrees (Numeric azimuths 30, 60, 120, etc.)
            deg % 30 == 0 -> {
                val tickLength = radius * 0.07f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)

                drawLine(
                    color = TextSecondary,
                    start = start,
                    end = end,
                    strokeWidth = 1.5.dp.toPx()
                )

                val numLayout = textMeasurer.measure(
                    text = "$deg",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                )
                val textX = center.x + cosA * numberDistance - (numLayout.size.width / 2f)
                val textY = center.y + sinA * numberDistance - (numLayout.size.height / 2f)
                drawText(numLayout, topLeft = Offset(textX, textY))
            }

            // Every 10 degrees
            deg % 10 == 0 -> {
                val tickLength = radius * 0.05f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)

                drawLine(
                    color = TextSecondary.copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Minor 2-degree ticks
            else -> {
                val tickLength = radius * 0.025f
                val start = Offset(center.x + cosA * (outerTickR - tickLength), center.y + sinA * (outerTickR - tickLength))
                val end = Offset(center.x + cosA * outerTickR, center.y + sinA * outerTickR)

                drawLine(
                    color = SlateBorder.copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = 0.8.dp.toPx()
                )
            }
        }
    }
}

private fun DrawScope.drawLockedBearingMarker(
    bearing: Float,
    center: Offset,
    radius: Float
) {
    val angleRad = (bearing - 90) * (PI / 180.0)
    val markerR = radius * 0.94f
    val tipX = center.x + cos(angleRad).toFloat() * markerR
    val tipY = center.y + sin(angleRad).toFloat() * markerR

    // Draw illuminated target flag/triangle
    val perpRad = angleRad + (PI / 2.0)
    val baseHalfWidth = 8.dp.toPx()
    val baseR = markerR - 16.dp.toPx()

    val p1 = Offset(tipX, tipY)
    val p2 = Offset(
        (center.x + cos(angleRad) * baseR + cos(perpRad) * baseHalfWidth).toFloat(),
        (center.y + sin(angleRad) * baseR + sin(perpRad) * baseHalfWidth).toFloat()
    )
    val p3 = Offset(
        (center.x + cos(angleRad) * baseR - cos(perpRad) * baseHalfWidth).toFloat(),
        (center.y + sin(angleRad) * baseR - sin(perpRad) * baseHalfWidth).toFloat()
    )

    val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }

    drawPath(path, color = Color(0xFFF59E0B))
    drawPath(path, color = Color.White, style = Stroke(width = 1.dp.toPx()))
}

private fun DrawScope.drawTopLubberLine(
    center: Offset,
    radius: Float,
    isTrueNorth: Boolean
) {
    // Fixed pointer at 12 o'clock (0 degrees on screen)
    val topY = center.y - radius
    val triangleHeight = 16.dp.toPx()
    val halfWidth = 9.dp.toPx()

    val path = Path().apply {
        moveTo(center.x, topY + triangleHeight) // pointing into center
        lineTo(center.x - halfWidth, topY)
        lineTo(center.x + halfWidth, topY)
        close()
    }

    val pointerColor = if (isTrueNorth) TrueNorthCyan else CompassNeedleRed

    drawPath(path = path, color = pointerColor)

    // Outer highlight dot
    drawCircle(
        color = pointerColor,
        radius = 3.dp.toPx(),
        center = Offset(center.x, topY + 4.dp.toPx())
    )
}

private fun DrawScope.drawCenterNeedleAndPivot(
    center: Offset,
    radius: Float,
    pitch: Float,
    roll: Float,
    isLevel: Boolean
) {
    val centerHubRadius = radius * 0.28f

    // Center circular hub background
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(SlateSurfaceVariant, SlateDark),
            center = center,
            radius = centerHubRadius
        ),
        radius = centerHubRadius,
        center = center
    )

    drawCircle(
        color = if (isLevel) LevelBubbleGreen else SlateBorder,
        radius = centerHubRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Level Crosshairs
    val crossHairLength = centerHubRadius * 0.85f
    drawLine(
        color = SlateBorder.copy(alpha = 0.5f),
        start = Offset(center.x - crossHairLength, center.y),
        end = Offset(center.x + crossHairLength, center.y),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = SlateBorder.copy(alpha = 0.5f),
        start = Offset(center.x, center.y - crossHairLength),
        end = Offset(center.x, center.y + crossHairLength),
        strokeWidth = 1.dp.toPx()
    )

    // Level Target Ring in the center (where bubble should sit)
    val targetRadius = 10.dp.toPx()
    drawCircle(
        color = if (isLevel) LevelBubbleGreen.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f),
        radius = targetRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Moving Level Bubble (Inclinometer)
    // Pitch tilts forward/backward (Y offset), Roll tilts left/right (X offset)
    val maxTilt = 20f
    val clampedRoll = (roll.coerceIn(-maxTilt, maxTilt) / maxTilt)
    val clampedPitch = (pitch.coerceIn(-maxTilt, maxTilt) / maxTilt)

    val maxBubbleOffset = centerHubRadius * 0.7f
    val bubbleOffset = Offset(
        x = center.x + clampedRoll * maxBubbleOffset,
        y = center.y - clampedPitch * maxBubbleOffset
    )

    val bubbleRadius = 8.dp.toPx()
    val bubbleColor = if (isLevel) LevelBubbleGreen else Color(0xFF38BDF8)

    // Bubble glow if leveled
    if (isLevel) {
        drawCircle(
            color = LevelBubbleGreen.copy(alpha = 0.35f),
            radius = bubbleRadius * 1.6f,
            center = bubbleOffset
        )
    }

    drawCircle(
        color = bubbleColor,
        radius = bubbleRadius,
        center = bubbleOffset
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = bubbleRadius * 0.4f,
        center = Offset(bubbleOffset.x - 2.dp.toPx(), bubbleOffset.y - 2.dp.toPx())
    )
}

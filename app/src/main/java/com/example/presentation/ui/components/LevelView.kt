package com.example.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CompassUiState
import com.example.ui.theme.HyperOSBlack
import com.example.ui.theme.HyperOSTextPrimary
import com.example.ui.theme.HyperOSTextSecondary
import com.example.ui.theme.XiaomiRed
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Xiaomi HyperOS Level Screen.
 * Strictly dedicated to Spirit Leveling / Inclinometer functionality.
 * Completely separate from Direction function.
 */
@Composable
fun LevelView(
    uiState: CompassUiState,
    modifier: Modifier = Modifier
) {
    val isVertical = abs(uiState.pitch) > 45f
    val isAligned = uiState.isLevelZero

    val levelAccentColor by animateColorAsState(
        targetValue = if (isAligned) XiaomiRed else Color.White,
        label = "levelColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HyperOSBlack)
            .testTag("level_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Large Tilt Degree Display & Axis Telemetry
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val displayTilt = if (isAligned) {
                0
            } else if (isVertical) {
                abs(uiState.roll).roundToInt()
            } else {
                uiState.totalTilt.roundToInt()
            }

            Text(
                text = "$displayTilt°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.5).sp,
                    color = levelAccentColor
                ),
                modifier = Modifier.testTag("level_tilt_degree_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle status: "Pitch 0°  •  Roll 0°" or "Level"
            Text(
                text = if (isAligned) {
                    "Level Surface"
                } else if (isVertical) {
                    String.format("Wall Incline: %+.1f°", uiState.roll)
                } else {
                    String.format("X: %+.1f°   Y: %+.1f°", uiState.roll, uiState.pitch)
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isAligned) XiaomiRed else HyperOSTextSecondary
                ),
                modifier = Modifier.testTag("level_subtitle_text")
            )
        }

        // CENTER: Xiaomi Dual-Mode Spirit Level Graphics
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isVertical) {
                // Vertical / Wall Mode (Artificial Horizon Line)
                VerticalWallLevel(
                    roll = uiState.roll,
                    isAligned = isAligned,
                    accentColor = levelAccentColor,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Horizontal / Flat Surface Mode (Bullseye Concentric Circles)
                HorizontalSurfaceLevel(
                    pitch = uiState.pitch,
                    roll = uiState.roll,
                    isAligned = isAligned,
                    accentColor = levelAccentColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // BOTTOM: Subtle orientation hint
        Text(
            text = if (isVertical) "Wall / Vertical Mode" else "Surface / Flat Mode",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                color = HyperOSTextSecondary.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )
    }
}

/**
 * Surface / Bullseye spirit level (Xiaomi HyperOS flat circle design).
 */
@Composable
private fun HorizontalSurfaceLevel(
    pitch: Float,
    roll: Float,
    isAligned: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    // Smooth moving bubble offsets
    val animatedRoll by animateFloatAsState(
        targetValue = roll,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "levelRoll"
    )
    val animatedPitch by animateFloatAsState(
        targetValue = pitch,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "levelPitch"
    )

    Canvas(modifier = modifier.padding(16.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        val outerTargetRadius = radius * 0.42f
        val floatingCircleRadius = radius * 0.42f

        // 1. Outer Reference Circle
        drawCircle(
            color = if (isAligned) XiaomiRed else Color.White.copy(alpha = 0.25f),
            radius = outerTargetRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Reference Crosshairs
        val crosshairSpan = outerTargetRadius * 1.5f
        val gap = outerTargetRadius * 0.15f

        // Horizontal crosshairs
        drawLine(
            color = if (isAligned) XiaomiRed else Color.White.copy(alpha = 0.2f),
            start = Offset(center.x - crosshairSpan, center.y),
            end = Offset(center.x - gap, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = if (isAligned) XiaomiRed else Color.White.copy(alpha = 0.2f),
            start = Offset(center.x + gap, center.y),
            end = Offset(center.x + crosshairSpan, center.y),
            strokeWidth = 1.dp.toPx()
        )

        // Vertical crosshairs
        drawLine(
            color = if (isAligned) XiaomiRed else Color.White.copy(alpha = 0.2f),
            start = Offset(center.x, center.y - crosshairSpan),
            end = Offset(center.x, center.y - gap),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = if (isAligned) XiaomiRed else Color.White.copy(alpha = 0.2f),
            start = Offset(center.x, center.y + gap),
            end = Offset(center.x, center.y + crosshairSpan),
            strokeWidth = 1.dp.toPx()
        )

        // 2. Floating Circle (Displaced by device tilt)
        if (isAligned) {
            // When perfectly aligned at 0°, the two circles merge into a single solid/accent circle!
            drawCircle(
                color = XiaomiRed.copy(alpha = 0.15f),
                radius = outerTargetRadius,
                center = center
            )
            drawCircle(
                color = XiaomiRed,
                radius = outerTargetRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            // Center 0° dot
            drawCircle(
                color = XiaomiRed,
                radius = 5.dp.toPx(),
                center = center
            )
        } else {
            // Calculate pixel offset from tilt (max clamp 15 degrees)
            val maxAngle = 15f
            val maxOffsetPixels = radius * 0.45f

            val clampedRoll = (animatedRoll.coerceIn(-maxAngle, maxAngle) / maxAngle)
            val clampedPitch = (animatedPitch.coerceIn(-maxAngle, maxAngle) / maxAngle)

            val bubbleCenter = Offset(
                x = center.x + clampedRoll * maxOffsetPixels,
                y = center.y - clampedPitch * maxOffsetPixels
            )

            // Connecting guide line between center and floating circle
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = center,
                end = bubbleCenter,
                strokeWidth = 1.dp.toPx()
            )

            // The moving circle
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = floatingCircleRadius,
                center = bubbleCenter
            )
            drawCircle(
                color = Color.White,
                radius = floatingCircleRadius,
                center = bubbleCenter,
                style = Stroke(width = 2.dp.toPx())
            )

            // Center dot in the moving circle
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = bubbleCenter
            )
        }
    }
}

/**
 * Vertical / Wall inclinometer (Artificial Horizon mode).
 */
@Composable
private fun VerticalWallLevel(
    roll: Float,
    isAligned: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedRoll by animateFloatAsState(
        targetValue = roll,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "wallRoll"
    )

    Canvas(modifier = modifier.padding(24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val w = size.width
        val h = size.height

        // Static Left & Right Reference Notches
        val notchLen = 28.dp.toPx()
        drawLine(
            color = if (isAligned) XiaomiRed else Color.White.copy(alpha = 0.35f),
            start = Offset(0f, center.y),
            end = Offset(notchLen, center.y),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = if (isAligned) XiaomiRed else Color.White.copy(alpha = 0.35f),
            start = Offset(w - notchLen, center.y),
            end = Offset(w, center.y),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center static reference circle
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = 36.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Rotating Horizon Line
        val angleRad = (animatedRoll) * (PI / 180.0)
        val lineHalfLen = (w * 0.40f)

        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()

        val p1 = Offset(center.x - lineHalfLen * cosA, center.y - lineHalfLen * sinA)
        val p2 = Offset(center.x + lineHalfLen * cosA, center.y + lineHalfLen * sinA)

        drawLine(
            color = accentColor,
            start = p1,
            end = p2,
            strokeWidth = if (isAligned) 3.5.dp.toPx() else 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center indicator dot
        drawCircle(
            color = accentColor,
            radius = if (isAligned) 6.dp.toPx() else 4.dp.toPx(),
            center = center
        )
    }
}

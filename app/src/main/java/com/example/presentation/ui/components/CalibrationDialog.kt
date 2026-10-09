package com.example.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HyperOSBorder
import com.example.ui.theme.HyperOSSurfaceElevated
import com.example.ui.theme.HyperOSTextPrimary
import com.example.ui.theme.HyperOSTextSecondary
import com.example.ui.theme.XiaomiMint
import com.example.ui.theme.XiaomiRed
import com.example.ui.theme.XiaomiYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CalibrationDialog(
    accuracy: Int,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "figure8Animation")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HyperOSSurfaceElevated,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = XiaomiYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calibrate Compass",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = HyperOSTextPrimary
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Move your device in a smooth figure-8 motion in the air to eliminate local magnetic interference.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = HyperOSTextSecondary),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Lemniscate (Figure-8) Canvas
                Box(
                    modifier = Modifier
                        .size(160.dp, 90.dp)
                        .testTag("figure_8_canvas"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(150.dp, 80.dp)) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f
                        val a = w * 0.42f

                        // Draw static dotted figure-8 path: Lemniscate of Bernoulli
                        val path = Path()
                        var first = true
                        for (i in 0..120) {
                            val theta = (i / 120f) * 2f * PI.toFloat()
                            val denom = 1f + sin(theta) * sin(theta)
                            val x = cx + (a * cos(theta)) / denom
                            val y = cy + (a * sin(theta) * cos(theta)) / denom
                            if (first) {
                                path.moveTo(x, y)
                                first = false
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        path.close()

                        drawPath(
                            path = path,
                            color = HyperOSBorder,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw moving animated device dot along path
                        val denomP = 1f + sin(progress) * sin(progress)
                        val dotX = cx + (a * cos(progress)) / denomP
                        val dotY = cy + (a * sin(progress) * cos(progress)) / denomP

                        drawCircle(
                            color = XiaomiRed.copy(alpha = 0.35f),
                            radius = 12.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                        drawCircle(
                            color = XiaomiRed,
                            radius = 6.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Sensor Accuracy Status
                val (accuracyStatus, statusColor) = when (accuracy) {
                    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Sensor Calibrated: High" to XiaomiMint
                    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Sensor Accuracy: Medium" to XiaomiYellow
                    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Sensor Accuracy: Low" to XiaomiYellow
                    else -> "Sensor Status: Unreliable" to Color(0xFFEF4444)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (accuracy >= 2) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = accuracyStatus,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = XiaomiRed),
                modifier = Modifier.testTag("dismiss_calibration_button")
            ) {
                Text(text = "Done", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

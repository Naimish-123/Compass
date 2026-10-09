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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CompassUiState
import com.example.ui.theme.CardinalCyan
import com.example.ui.theme.CompassNeedleRed
import com.example.ui.theme.LevelBubbleGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun BearingLockCard(
    uiState: CompassUiState,
    onToggleLock: () -> Unit,
    onClearLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.lockedBearing != null && uiState.bearingDeviation != null) {
        val deviation = uiState.bearingDeviation
        val absDev = abs(deviation).roundToInt()
        val isOnCourse = absDev <= 2

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("bearing_lock_card"),
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                if (isOnCourse) LevelBubbleGreen else Color(0xFFF59E0B)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bearing Locked",
                        tint = if (isOnCourse) LevelBubbleGreen else Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "TARGET: ${uiState.lockedBearing.roundToInt()}°",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = when {
                                isOnCourse -> "On Course (±${absDev}°)"
                                deviation > 0 -> "Turn $absDev° Left"
                                else -> "Turn $absDev° Right"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isOnCourse) LevelBubbleGreen else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onClearLock,
                    modifier = Modifier.testTag("clear_bearing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Unlock bearing",
                        tint = TextSecondary
                    )
                }
            }
        }
    } else {
        // Button to lock current course
        OutlinedButton(
            onClick = onToggleLock,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("lock_bearing_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SlateSurface.copy(alpha = 0.6f)
            ),
            border = BorderStroke(1.dp, SlateBorder)
        ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                tint = CardinalCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Lock Course Bearing (${uiState.displayHeading.roundToInt()}°)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

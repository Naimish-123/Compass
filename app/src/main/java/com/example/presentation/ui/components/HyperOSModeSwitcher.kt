package com.example.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PanoramaHorizontal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CompassMode
import com.example.ui.theme.HyperOSTextPrimary
import com.example.ui.theme.HyperOSTextSecondary

/**
 * Xiaomi HyperOS signature floating segmented mode switcher.
 * Smoothly toggles between "Direction" and "Level" functions.
 */
@Composable
fun HyperOSModeSwitcher(
    currentMode: CompassMode,
    onModeSelected: (CompassMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 48.dp, vertical = 12.dp)
            .testTag("hyperos_mode_switcher"),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF1C1C1E),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Direction Tab
            ModeItem(
                title = "Direction",
                isSelected = currentMode == CompassMode.DIRECTION,
                onClick = { onModeSelected(CompassMode.DIRECTION) },
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_direction")
            )

            // Level Tab
            ModeItem(
                title = "Level",
                isSelected = currentMode == CompassMode.LEVEL,
                onClick = { onModeSelected(CompassMode.LEVEL) },
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.PanoramaHorizontal,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_level")
            )
        }
    }
}

@Composable
private fun ModeItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2C2C2E) else Color.Transparent,
        label = "modeItemBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) HyperOSTextPrimary else HyperOSTextSecondary,
        label = "modeItemText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon(contentColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor
                )
            )
        }
    }
}

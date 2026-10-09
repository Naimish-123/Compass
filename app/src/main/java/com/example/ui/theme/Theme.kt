package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CompassColorScheme = darkColorScheme(
    primary = CompassNeedleRed,
    onPrimary = Color.White,
    primaryContainer = SlateSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = CardinalCyan,
    onSecondary = SlateDark,
    secondaryContainer = SlateSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = LevelBubbleGreen,
    onTertiary = Color.White,
    background = SlateDark,
    onBackground = TextPrimary,
    surface = SlateBackground,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurface,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Compass apps are inherently designed in sleek high-contrast dark mode for night visibility and OLED clarity
    MaterialTheme(
        colorScheme = CompassColorScheme,
        typography = Typography,
        content = content
    )
}

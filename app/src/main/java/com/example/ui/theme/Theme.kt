package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HyperOSColorScheme = darkColorScheme(
    primary = XiaomiRed,
    onPrimary = Color.White,
    primaryContainer = HyperOSSurfaceElevated,
    onPrimaryContainer = HyperOSTextPrimary,
    secondary = HyperOSTextSecondary,
    onSecondary = HyperOSBlack,
    secondaryContainer = HyperOSSurfacePill,
    onSecondaryContainer = HyperOSTextPrimary,
    tertiary = XiaomiRed,
    onTertiary = Color.White,
    background = HyperOSBlack,
    onBackground = HyperOSTextPrimary,
    surface = HyperOSBlack,
    onSurface = HyperOSTextPrimary,
    surfaceVariant = HyperOSSurfaceElevated,
    onSurfaceVariant = HyperOSTextSecondary,
    outline = HyperOSBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Xiaomi HyperOS Compass features an OLED pitch-black aesthetic
    MaterialTheme(
        colorScheme = HyperOSColorScheme,
        typography = Typography,
        content = content
    )
}

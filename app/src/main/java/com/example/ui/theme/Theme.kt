package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberElectricBlue,
    onPrimary = Color(0xFF001F2A),
    primaryContainer = Color(0xFF004D65),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = CyberBlueSecondary,
    onSecondary = Color(0xFF001F2A),
    secondaryContainer = Color(0xFF003548),
    onSecondaryContainer = Color(0xFFC2E8FF),
    tertiary = CyberBlueTertiary,
    onTertiary = Color(0xFF141A4B),
    tertiaryContainer = Color(0xFF29326D),
    onTertiaryContainer = Color(0xFFDFE0FF),
    background = CyberDarkNavy,
    onBackground = CyberTextPrimary,
    surface = CyberNavySurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberNavySurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    outlineVariant = Color(0xFF1E293B),
    error = CyberHighRisk,
    errorContainer = CyberHighRiskContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD9)
)

@Composable
fun PhishLensTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PhishLensTheme(content = content)
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = TextInverse,
    primaryContainer = CyberCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = SmaliPurple,
    onSecondary = Color.White,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = SeverityCritical,
    onTertiary = Color.White,
    background = CyberBackground,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberCardBorder,
    outlineVariant = CyberSurfaceVariant
)

private val CyberLightColorScheme = lightColorScheme(
    primary = CyberCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF00363A),
    secondary = SmaliPurple,
    onSecondary = Color.White,
    background = LightCyberBackground,
    onBackground = LightTextPrimary,
    surface = LightCyberSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightTextSecondary,
    outline = LightCyberBorder
)

@Composable
fun ApkSentinelTheme(
    darkTheme: Boolean = true, // Default to professional cybersecurity dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

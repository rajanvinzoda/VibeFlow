package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VibeFlowColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    primaryContainer = SpotifyGreenDark(0x33),
    onPrimaryContainer = SpotifyGreenGlow,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0F3040),
    onSecondaryContainer = NeonCyan,
    tertiary = EmeraldNeon,
    onTertiary = Color.Black,
    background = VibeBackground,
    onBackground = VibeTextPrimary,
    surface = VibeSurface,
    onSurface = VibeTextPrimary,
    surfaceVariant = VibeSurfaceVariant,
    onSurfaceVariant = VibeTextSecondary,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private fun SpotifyGreenDark(alpha: Int): Color = Color(0x1DB954 or (alpha shl 24))

@Composable
fun VibeFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VibeFlowColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    VibeFlowTheme(content = content)
}

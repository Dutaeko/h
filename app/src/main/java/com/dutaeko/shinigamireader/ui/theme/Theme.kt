package com.dutaeko.shinigamireader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB58CFF),
    onPrimary = Color(0xFF1F003D),
    secondary = Color(0xFFFF6B8B),
    background = Color(0xFF0D0D10),
    surface = Color(0xFF141419),
    surfaceVariant = Color(0xFF1E1E26),
    onSurface = Color(0xFFF5F5F7),
    onSurfaceVariant = Color(0xFFB5B6C5),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6F38E5),
    secondary = Color(0xFFC0385B),
    background = Color(0xFFF7F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9E8F2),
    onSurface = Color(0xFF121218),
    onSurfaceVariant = Color(0xFF58586A),
)

private val AppTypography = Typography()

@Composable
fun ShinigamiReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

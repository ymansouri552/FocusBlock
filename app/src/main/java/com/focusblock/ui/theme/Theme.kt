package com.focusblock.ui.theme

import androidx.compose.foundation.isSystemInLightTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.focusblock.R

private val DarkColorScheme = darkColorScheme(
    primary = 0xFFCAB4F0,
    primaryContainer = 0xFF4A3A8C,
    secondary = 0xFFCCC2DC,
    secondaryContainer = 0xFF3A3447,
    tertiary = 0xFFEFB8C8,
    tertiaryContainer = 0xFF5D3B47,
    error = 0xFFF2B8B5,
    errorContainer = 0xFF601E1B,
    background = 0xFF1C1B1F,
    onBackground = 0xFFE7E0EC,
    surface = 0xFF1C1B1F,
    onSurface = 0xFFE7E0EC,
    surfaceVariant = 0xFF49454F,
    onSurfaceVariant = 0xFFCAC4D0,
    outline = 0xFF8E8998,
    inverseOnSurface = 0xFF1C1B1F,
    inverseSurface = 0xFFE7E0EC,
    inversePrimary = 0xFF6750A4,
    surfaceDim = 0xFF1C1B1F,
    surfaceBright = 0xFF3A3447,
    surfaceContainerHighest = 0xFF49454F,
    surfaceContainerHigh = 0xFF3A3447,
    surfaceContainer = 0xFF2C2A2F,
    surfaceContainerLow = 0xFF222126,
    surfaceContainerLowest = 0xFF1C1B1F,
)

private val LightColorScheme = lightColorScheme(
    primary = 0xFF6750A4,
    primaryContainer = 0xFFEADDFF,
    secondary = 0xFF625B71,
    secondaryContainer = 0xFFE8DEF8,
    tertiary = 0xFF7D5260,
    tertiaryContainer = 0xFFFFD8E4,
    error = 0xFFB3261E,
    errorContainer = 0xFFF9DEDC,
    background = 0xFFFFFBFE,
    onBackground = 0xFF1C1B1F,
    surface = 0xFFFFFBFE,
    onSurface = 0xFF1C1B1F,
    surfaceVariant = 0xFFE7E0EC,
    onSurfaceVariant = 0xFF49454F,
    outline = 0xFF79747E,
    inverseOnSurface = 0xFFFFFBFE,
    inverseSurface = 0xFF313033,
    inversePrimary = 0xFFCAB4F0,
    surfaceDim = 0xFFD7D2DE,
    surfaceBright = 0xFFFFFBFE,
    surfaceContainerHighest = 0xFFE7E0EC,
    surfaceContainerHigh = 0xFFECE7F0,
    surfaceContainer = 0xFFFFFBFE,
    surfaceContainerLow = 0xFFF7F2FA,
    surfaceContainerLowest = 0xFFFFFFFF,
)

@Composable
fun FocusBlockTheme(
    darkTheme: Boolean = !isSystemInLightTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
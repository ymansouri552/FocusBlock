package com.focusblock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCAB4F0),
    primaryContainer = Color(0xFF4A3A8C),
    secondary = Color(0xFFCCC2DC),
    secondaryContainer = Color(0xFF3A3447),
    tertiary = Color(0xFFEFB8C8),
    tertiaryContainer = Color(0xFF5D3B47),
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF601E1B),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE7E0EC),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE7E0EC),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF8E8998),
    inverseOnSurface = Color(0xFF1C1B1F),
    inverseSurface = Color(0xFFE7E0EC),
    inversePrimary = Color(0xFF6750A4),
    surfaceDim = Color(0xFF1C1B1F),
    surfaceBright = Color(0xFF3A3447),
    surfaceContainerHighest = Color(0xFF49454F),
    surfaceContainerHigh = Color(0xFF3A3447),
    surfaceContainer = Color(0xFF2C2A2F),
    surfaceContainerLow = Color(0xFF222126),
    surfaceContainerLowest = Color(0xFF1C1B1F),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    primaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF625B71),
    secondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFF7D5260),
    tertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    inverseOnSurface = Color(0xFFFFFBFE),
    inverseSurface = Color(0xFF313033),
    inversePrimary = Color(0xFFCAB4F0),
    surfaceDim = Color(0xFFD7D2DE),
    surfaceBright = Color(0xFFFFFBFE),
    surfaceContainerHighest = Color(0xFFE7E0EC),
    surfaceContainerHigh = Color(0xFFECE7F0),
    surfaceContainer = Color(0xFFFFFBFE),
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
)

@Composable
fun FocusBlockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
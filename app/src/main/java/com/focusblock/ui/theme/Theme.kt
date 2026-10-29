package com.focusblock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Semantic colors for Strict Mode & Active Sessions
data class CustomColors(
    val strictAmber: Color,
    val strictContainer: Color,
    val strictOnContainer: Color,
    val activeGreen: Color,
    val activeContainer: Color,
    val activeOnContainer: Color,
    val cardBorder: Color,
    val subtleText: Color
)

val DarkCustomColors = CustomColors(
    strictAmber = Color(0xFFFBBF24),
    strictContainer = Color(0xFF352002),
    strictOnContainer = Color(0xFFFDE68A),
    activeGreen = Color(0xFF34D399),
    activeContainer = Color(0xFF063824),
    activeOnContainer = Color(0xFFA7F3D0),
    cardBorder = Color(0xFF2E384D),
    subtleText = Color(0xFF94A3B8)
)

val LightCustomColors = CustomColors(
    strictAmber = Color(0xFFD97706),
    strictContainer = Color(0xFFFEF3C7),
    strictOnContainer = Color(0xFF78350F),
    activeGreen = Color(0xFF059669),
    activeContainer = Color(0xFFD1FAE5),
    activeOnContainer = Color(0xFF065F46),
    cardBorder = Color(0xFFE2E8F0),
    subtleText = Color(0xFF64748B)
)

val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF4C0519),
    tertiaryContainer = Color(0xFF831843),
    onTertiaryContainer = Color(0xFFFCE7F3),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0B0F19),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    surfaceDim = Color(0xFF0B0F19),
    surfaceBright = Color(0xFF1F2937),
    surfaceContainerLowest = Color(0xFF070B14),
    surfaceContainerLow = Color(0xFF0F172A),
    surfaceContainer = Color(0xFF131D31),
    surfaceContainerHigh = Color(0xFF1E293B),
    surfaceContainerHighest = Color(0xFF28354D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF0D9488),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = Color(0xFFDB2777),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF831843),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    surfaceDim = Color(0xFFEDEFEF),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE8EEF5),
    surfaceContainerHighest = Color(0xFFDEE5EE)
)

@Composable
fun FocusBlockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}

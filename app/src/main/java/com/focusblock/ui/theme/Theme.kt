package com.focusblock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Strict mode and error keep their own colors because they mark real, irreversible state.
// Everything else resolves to one accent.
data class CustomColors(
    val strictAmber: Color,
    val strictContainer: Color,
    val strictOnContainer: Color,
    val positive: Color,
    val positiveContainer: Color,
    val onPositiveContainer: Color,
    val cardBorder: Color,
    val subtleText: Color
)

val DarkCustomColors = CustomColors(
    strictAmber = Color(0xFFE8B45A),
    strictContainer = Color(0xFF2B2110),
    strictOnContainer = Color(0xFFF6D9A0),
    positive = Color(0xFF5FB8A3),
    positiveContainer = Color(0xFF12302A),
    onPositiveContainer = Color(0xFFB7E6DA),
    cardBorder = Color(0xFF2E2B23),
    subtleText = Color(0xFFA8A296)
)

val LightCustomColors = CustomColors(
    strictAmber = Color(0xFF7A4E0A),
    strictContainer = Color(0xFFF6E6C6),
    strictOnContainer = Color(0xFF4A2F05),
    positive = Color(0xFF0D5F52),
    positiveContainer = Color(0xFFD8ECE6),
    onPositiveContainer = Color(0xFF073A32),
    cardBorder = Color(0xFFE3DED0),
    subtleText = Color(0xFF5E5A4F)
)

val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }

// Warm graphite and paper, not the cold slate default. The single accent is oxidized
// copper (patina), the color of a surface that has been used deliberately.
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5FB8A3),
    onPrimary = Color(0xFF06201B),
    primaryContainer = Color(0xFF12302A),
    onPrimaryContainer = Color(0xFFB7E6DA),
    secondary = Color(0xFF5FB8A3),
    onSecondary = Color(0xFF06201B),
    secondaryContainer = Color(0xFF12302A),
    onSecondaryContainer = Color(0xFFB7E6DA),
    tertiary = Color(0xFFE8B45A),
    onTertiary = Color(0xFF2B2110),
    tertiaryContainer = Color(0xFF2B2110),
    onTertiaryContainer = Color(0xFFF6D9A0),
    error = Color(0xFFE8836F),
    onError = Color(0xFF2A0E08),
    errorContainer = Color(0xFF33150F),
    onErrorContainer = Color(0xFFFFD9D0),
    background = Color(0xFF12110E),
    onBackground = Color(0xFFF2EFE7),
    surface = Color(0xFF1B1A16),
    onSurface = Color(0xFFF2EFE7),
    surfaceVariant = Color(0xFF242219),
    onSurfaceVariant = Color(0xFFA8A296),
    outline = Color(0xFF4A463B),
    outlineVariant = Color(0xFF2E2B23),
    surfaceDim = Color(0xFF12110E),
    surfaceBright = Color(0xFF2E2B23),
    surfaceContainerLowest = Color(0xFF0D0C0A),
    surfaceContainerLow = Color(0xFF161511),
    surfaceContainer = Color(0xFF1B1A16),
    surfaceContainerHigh = Color(0xFF242219),
    surfaceContainerHighest = Color(0xFF2E2B23)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0D5F52),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8ECE6),
    onPrimaryContainer = Color(0xFF073A32),
    secondary = Color(0xFF0D5F52),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8ECE6),
    onSecondaryContainer = Color(0xFF073A32),
    tertiary = Color(0xFF7A4E0A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF6E6C6),
    onTertiaryContainer = Color(0xFF4A2F05),
    error = Color(0xFFA32B1C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DCD6),
    onErrorContainer = Color(0xFF5C150C),
    background = Color(0xFFF7F4EC),
    onBackground = Color(0xFF191813),
    surface = Color(0xFFFFFEFA),
    onSurface = Color(0xFF191813),
    surfaceVariant = Color(0xFFEFEBE0),
    onSurfaceVariant = Color(0xFF5E5A4F),
    outline = Color(0xFFB8B1A1),
    outlineVariant = Color(0xFFE3DED0),
    surfaceDim = Color(0xFFEFEBE0),
    surfaceBright = Color(0xFFFFFEFA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF7F0),
    surfaceContainer = Color(0xFFF1EDE3),
    surfaceContainerHigh = Color(0xFFE9E4D8),
    surfaceContainerHighest = Color(0xFFE0DACC)
)

// System sans for the interface, because it is the most legible at small sizes and adds
// no font payload. The character comes from scale and tracking, not from a novelty face.
private val FocusTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.3.sp
    )
)

// Time is read while it changes. Tabular figures stop the readout from shifting width.
fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

@Composable
fun FocusBlockTheme(
    themeMode: String = "DARK",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val effectiveDark = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> darkTheme
    }

    val colorScheme = if (effectiveDark) DarkColorScheme else LightColorScheme
    val customColors = if (effectiveDark) DarkCustomColors else LightCustomColors

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FocusTypography,
            content = content
        )
    }
}

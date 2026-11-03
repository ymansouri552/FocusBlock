package com.focusblock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.R

/**
 * FocusBlock "The Pledge" Design System: Paper & Obsidian palettes.
 *
 * Light "Paper": Warm contract paper, ink black, crisp 2dp borders, ink blue accent, red stamp.
 * Dark "Obsidian": Near-black warm stone, pale ink, acid lime accent, bright orange-red stamp.
 * No arbitrary gradients, no glassmorphism, no pastel/teal AI tropes.
 */

@Immutable
data class PledgePalette(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val ink: Color,
    val inkSecondary: Color,
    val hairline: Color,
    val border: Color,
    val accent: Color,
    val onAccent: Color,
    val stamp: Color,
    val stampSurface: Color
)

val LightPledgePalette = PledgePalette(
    background = Color(0xFFF4EFE6),
    surface = Color(0xFFFBF8F2),
    surfaceRaised = Color(0xFFFFFFFF),
    ink = Color(0xFF14161A),
    inkSecondary = Color(0xFF5B5F66),
    hairline = Color(0x2614161A), // 15% opacity
    border = Color(0xFF14161A),   // Solid 2dp borders
    accent = Color(0xFF1B3FA0),   // Ink blue
    onAccent = Color(0xFFFBF8F2),
    stamp = Color(0xFFD9381E),    // Seal red
    stampSurface = Color(0x14D9381E)
)

val DarkPledgePalette = PledgePalette(
    background = Color(0xFF0C0D0B),
    surface = Color(0xFF161714),
    surfaceRaised = Color(0xFF1E201B),
    ink = Color(0xFFEDEBE4),
    inkSecondary = Color(0xFF8E8F88),
    hairline = Color(0x26EDEBE4), // 15% opacity
    border = Color(0x99EDEBE4),   // 60% opacity key border
    accent = Color(0xFFC6F432),   // Acid lime
    onAccent = Color(0xFF0C0D0B), // High contrast dark text on lime
    stamp = Color(0xFFFF5A36),    // Bright seal red-orange
    stampSurface = Color(0x1CFF5A36)
)

// Legacy compatibility shim for any existing references
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

val LocalPledgePalette = staticCompositionLocalOf { LightPledgePalette }
val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        strictAmber = LightPledgePalette.stamp,
        strictContainer = LightPledgePalette.stampSurface,
        strictOnContainer = LightPledgePalette.stamp,
        positive = LightPledgePalette.accent,
        positiveContainer = LightPledgePalette.accent.copy(alpha = 0.12f),
        onPositiveContainer = LightPledgePalette.accent,
        cardBorder = LightPledgePalette.hairline,
        subtleText = LightPledgePalette.inkSecondary
    )
}

val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val Inter = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

// Pledge Type Scale
val PledgeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 1.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp
    )
)

val HeroNumericStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Bold,
    fontSize = 68.sp,
    lineHeight = 72.sp,
    letterSpacing = (-1.0).sp,
    fontFeatureSettings = "tnum"
)

val CountdownNumericStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
)

val StampTextStyle = TextStyle(
    fontFamily = SpaceGrotesk,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = 13.sp,
    letterSpacing = 2.0.sp
)

fun TextStyle.tabular(): TextStyle = copy(
    fontFamily = JetBrainsMono,
    fontFeatureSettings = "tnum"
)

// Sharp, ledger-like shapes
val PledgeShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPledgePalette.accent,
    onPrimary = LightPledgePalette.onAccent,
    primaryContainer = Color(0x1A1B3FA0),
    onPrimaryContainer = LightPledgePalette.accent,
    secondary = LightPledgePalette.accent,
    onSecondary = LightPledgePalette.onAccent,
    secondaryContainer = Color(0x1A1B3FA0),
    onSecondaryContainer = LightPledgePalette.accent,
    tertiary = LightPledgePalette.stamp,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = LightPledgePalette.stampSurface,
    onTertiaryContainer = LightPledgePalette.stamp,
    error = LightPledgePalette.stamp,
    onError = Color(0xFFFFFFFF),
    errorContainer = LightPledgePalette.stampSurface,
    onErrorContainer = LightPledgePalette.stamp,
    background = LightPledgePalette.background,
    onBackground = LightPledgePalette.ink,
    surface = LightPledgePalette.surface,
    onSurface = LightPledgePalette.ink,
    surfaceVariant = Color(0xFFEBE5DC),
    onSurfaceVariant = LightPledgePalette.inkSecondary,
    outline = LightPledgePalette.border,
    outlineVariant = LightPledgePalette.hairline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPledgePalette.accent,
    onPrimary = DarkPledgePalette.onAccent,
    primaryContainer = Color(0x26C6F432),
    onPrimaryContainer = DarkPledgePalette.accent,
    secondary = DarkPledgePalette.accent,
    onSecondary = DarkPledgePalette.onAccent,
    secondaryContainer = Color(0x26C6F432),
    onSecondaryContainer = DarkPledgePalette.accent,
    tertiary = DarkPledgePalette.stamp,
    onTertiary = DarkPledgePalette.background,
    tertiaryContainer = DarkPledgePalette.stampSurface,
    onTertiaryContainer = DarkPledgePalette.stamp,
    error = DarkPledgePalette.stamp,
    onError = DarkPledgePalette.background,
    errorContainer = DarkPledgePalette.stampSurface,
    onErrorContainer = DarkPledgePalette.stamp,
    background = DarkPledgePalette.background,
    onBackground = DarkPledgePalette.ink,
    surface = DarkPledgePalette.surface,
    onSurface = DarkPledgePalette.ink,
    surfaceVariant = Color(0xFF1E201B),
    onSurfaceVariant = DarkPledgePalette.inkSecondary,
    outline = DarkPledgePalette.border,
    outlineVariant = DarkPledgePalette.hairline
)

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

    val palette = if (effectiveDark) DarkPledgePalette else LightPledgePalette
    val colorScheme = if (effectiveDark) DarkColorScheme else LightColorScheme

    val customColors = CustomColors(
        strictAmber = palette.stamp,
        strictContainer = palette.stampSurface,
        strictOnContainer = palette.stamp,
        positive = palette.accent,
        positiveContainer = palette.accent.copy(alpha = 0.15f),
        onPositiveContainer = palette.accent,
        cardBorder = palette.hairline,
        subtleText = palette.inkSecondary
    )

    CompositionLocalProvider(
        LocalPledgePalette provides palette,
        LocalCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PledgeTypography,
            shapes = PledgeShapes,
            content = content
        )
    }
}

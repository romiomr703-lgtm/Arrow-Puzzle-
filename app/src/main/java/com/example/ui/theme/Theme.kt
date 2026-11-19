package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.GameThemePalette

val LocalArrowBoardPalette = staticCompositionLocalOf { ClassicInkBoardPalette }

private val PrismaticColorScheme = lightColorScheme(
    primary = ElectricAzure,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEEFF),
    onPrimaryContainer = Color(0xFF00326B),
    secondary = VividCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F6FF),
    onSecondaryContainer = Color(0xFF003847),
    tertiary = CoralPunch,
    onTertiary = Color.White,
    background = PrismaticBg,
    onBackground = PrismaticInk,
    surface = PrismaticSurface,
    onSurface = PrismaticInk,
    surfaceVariant = PrismaticSurfaceVariant,
    onSurfaceVariant = PrismaticMuted,
    outline = PrismaticOutline,
    outlineVariant = Color(0xFFEBF0F7),
)

private val ClassicInkColorScheme = lightColorScheme(
    primary = Color(0xFF0088FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F0FF),
    onPrimaryContainer = ClassicInk,
    secondary = ClassicInk,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAF0F8),
    onSecondaryContainer = ClassicInk,
    tertiary = CoralPunch,
    onTertiary = Color.White,
    background = ClassicBg,
    onBackground = ClassicInk,
    surface = ClassicSurface,
    onSurface = ClassicInk,
    surfaceVariant = ClassicSurfaceVariant,
    onSurfaceVariant = ClassicMuted,
    outline = ClassicOutline,
    outlineVariant = Color(0xFFEEF2F7),
)

private val MidnightGlowColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF042230),
    primaryContainer = Color(0xFF103752),
    onPrimaryContainer = Color(0xFFCCF6FF),
    secondary = NeonViolet,
    onSecondary = Color(0xFF1D0D4A),
    secondaryContainer = Color(0xFF281C54),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = NeonCoral,
    onTertiary = Color(0xFF3A0618),
    background = MidnightBg,
    onBackground = MidnightInk,
    surface = MidnightSurface,
    onSurface = MidnightInk,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MidnightMuted,
    outline = MidnightOutline,
    outlineVariant = Color(0xFF1C2B4D),
)

@Composable
fun ArrowEscapeTheme(
    palette: GameThemePalette = GameThemePalette.CLASSIC_INK,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (palette) {
        GameThemePalette.PRISMATIC -> PrismaticColorScheme
        GameThemePalette.CLASSIC_INK -> ClassicInkColorScheme
        GameThemePalette.MIDNIGHT_GLOW -> MidnightGlowColorScheme
    }
    val boardPalette = when (palette) {
        GameThemePalette.PRISMATIC -> PrismaticBoardPalette
        GameThemePalette.CLASSIC_INK -> ClassicInkBoardPalette
        GameThemePalette.MIDNIGHT_GLOW -> MidnightGlowBoardPalette
    }

    CompositionLocalProvider(LocalArrowBoardPalette provides boardPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ArrowEscapeTypography,
            content = content,
        )
    }
}

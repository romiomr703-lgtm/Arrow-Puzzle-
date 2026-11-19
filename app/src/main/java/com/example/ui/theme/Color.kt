package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Prismatic Vibrant Minimalist (Light)
val PrismaticBg = Color(0xFFF6F8FC)
val PrismaticSurface = Color(0xFFFFFFFF)
val PrismaticSurfaceVariant = Color(0xFFEDF2FA)
val PrismaticInk = Color(0xFF0F172A)
val PrismaticMuted = Color(0xFF64748B)
val PrismaticOutline = Color(0xFFE2E8F0)
val ElectricAzure = Color(0xFF0077FF)
val VividCyan = Color(0xFF00B4D8)
val CoralPunch = Color(0xFFFF3B6B)
val EmeraldMint = Color(0xFF00B87C)
val SunsetAmber = Color(0xFFFF9500)
val RoyalViolet = Color(0xFF7C3AED)
val CobaltIndigo = Color(0xFF2563EB)

// Classic Black Minimalist (Light — crisp black arrows on pure white)
val ClassicBg = Color(0xFFFFFFFF)
val ClassicSurface = Color(0xFFFFFFFF)
val ClassicSurfaceVariant = Color(0xFFF2F4F8)
val ClassicInk = Color(0xFF0D1526)
val ClassicMuted = Color(0xFF64748B)
val ClassicOutline = Color(0xFFE5EAF2)

// Midnight Glow Minimalist (Dark — deep obsidian with luminous neon minimalism)
val MidnightBg = Color(0xFF090F1E)
val MidnightSurface = Color(0xFF111B33)
val MidnightSurfaceVariant = Color(0xFF192645)
val MidnightInk = Color(0xFFF1F5F9)
val MidnightMuted = Color(0xFF94A3B8)
val MidnightOutline = Color(0xFF23345C)
val NeonCyan = Color(0xFF22D3EE)
val NeonCoral = Color(0xFFFF5C8A)
val NeonMint = Color(0xFF34D399)
val NeonAmber = Color(0xFFFBBF24)
val NeonViolet = Color(0xFFA78BFA)
val NeonSky = Color(0xFF60A5FA)

// Semantic Difficulty Pill Colors
val DiffEasyBg = Color(0xFFE0FAF0)
val DiffEasyFg = Color(0xFF047857)
val DiffNormalBg = Color(0xFFE0F2FE)
val DiffNormalFg = Color(0xFF0369A1)
val DiffHardBg = Color(0xFFFFF1E0)
val DiffHardFg = Color(0xFFC2410C)
val DiffExpertBg = Color(0xFFFFE4EC)
val DiffExpertFg = Color(0xFFBE123C)
val DiffMasterBg = Color(0xFFF3E8FF)
val DiffMasterFg = Color(0xFF6D28D9)

val DiffEasyBgDark = Color(0xFF064E3B)
val DiffEasyFgDark = Color(0xFF6EE7B7)
val DiffNormalBgDark = Color(0xFF0C4A6E)
val DiffNormalFgDark = Color(0xFF7DD3FC)
val DiffHardBgDark = Color(0xFF7C2D12)
val DiffHardFgDark = Color(0xFFFDBA74)
val DiffExpertBgDark = Color(0xFF881337)
val DiffExpertFgDark = Color(0xFFFDA4AF)
val DiffMasterBgDark = Color(0xFF4C1D95)
val DiffMasterFgDark = Color(0xFFD8B4FE)

@Immutable
data class ArrowBoardPalette(
    val isDark: Boolean,
    val boardBackground: Color,
    val boardAuraStart: Color,
    val gridDot: Color,
    val gridLine: Color,
    val arrowColors: List<Color>,
    val escapingColor: Color,
    val hintColor: Color,
    val hintGlow: Color,
    val collisionColor: Color,
    val heartActive: Color,
    val heartLost: Color,
    val starActive: Color,
    val starInactive: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val sunburstPrimary: Color,
    val sunburstSecondary: Color,
)

val PrismaticBoardPalette = ArrowBoardPalette(
    isDark = false,
    boardBackground = PrismaticBg,
    boardAuraStart = Color(0x160088FF),
    gridDot = Color(0xFFCBD5E1),
    gridLine = Color(0x2664748B),
    arrowColors = listOf(
        CobaltIndigo,
        CoralPunch,
        EmeraldMint,
        RoyalViolet,
        SunsetAmber,
        VividCyan,
    ),
    escapingColor = ElectricAzure,
    hintColor = Color(0xFF0095FF),
    hintGlow = Color(0x5500B4FF),
    collisionColor = Color(0xFFFF2D55),
    heartActive = Color(0xFFFF2D55),
    heartLost = Color(0xFFCBD5E1),
    starActive = Color(0xFFFFB800),
    starInactive = Color(0xFFCBD5E1),
    heroGradientStart = Color(0xFF0077FF),
    heroGradientEnd = Color(0xFF00C2FF),
    sunburstPrimary = Color(0xFF0088FF),
    sunburstSecondary = Color(0xFF00B4FF),
)

val ClassicInkBoardPalette = ArrowBoardPalette(
    isDark = false,
    boardBackground = ClassicBg,
    boardAuraStart = Color.Transparent,
    gridDot = Color(0xFFCBD5E1),
    gridLine = Color(0x2264748B),
    arrowColors = listOf(
        ClassicInk,
        ClassicInk,
        ClassicInk,
        ClassicInk,
        ClassicInk,
        ClassicInk,
    ),
    escapingColor = Color(0xFF0095FF),
    hintColor = Color(0xFF0095FF),
    hintGlow = Color(0x550095FF),
    collisionColor = Color(0xFFFF2D3B),
    heartActive = Color(0xFFFF2D3B),
    heartLost = Color(0xFFD8DEE8),
    starActive = Color(0xFFFFB800),
    starInactive = Color(0xFFCBD5E1),
    heroGradientStart = Color(0xFF0D1526),
    heroGradientEnd = Color(0xFF1E2D4D),
    sunburstPrimary = Color(0xFF0088FF),
    sunburstSecondary = Color(0xFF00B8FF),
)

val MidnightGlowBoardPalette = ArrowBoardPalette(
    isDark = true,
    boardBackground = MidnightBg,
    boardAuraStart = Color(0x2022D3EE),
    gridDot = Color(0xFF334770),
    gridLine = Color(0x3364748B),
    arrowColors = listOf(
        NeonCyan,
        NeonCoral,
        NeonMint,
        NeonViolet,
        NeonAmber,
        NeonSky,
    ),
    escapingColor = Color(0xFFFFFFFF),
    hintColor = NeonCyan,
    hintGlow = Color(0x6622D3EE),
    collisionColor = Color(0xFFFF4D6D),
    heartActive = Color(0xFFFF4D6D),
    heartLost = Color(0xFF2A3B63),
    starActive = Color(0xFFFBBF24),
    starInactive = Color(0xFF2A3B63),
    heroGradientStart = Color(0xFF0E7490),
    heroGradientEnd = Color(0xFF6D28D9),
    sunburstPrimary = Color(0xFF0E7490),
    sunburstSecondary = Color(0xFF1D4ED8),
)

package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// TeaGram Brand Colors
val InstaPurple = Color(0xFF8134AF)
val InstaPink = Color(0xFFDD2A7B)
val InstaOrange = Color(0xFFF58529)
val InstaYellow = Color(0xFFFEE411)
val InstaBlue = Color(0xFF515BD4)
val InstaLinkBlue = Color(0xFF0095F6)
val InstaHeartRed = Color(0xFFFF4B6B) // Frosted theme vibrant like heart

// Frosted Glass Theme Palette (Extracted from Design HTML)
val FrostedCanvas = Color(0xFF0F1113)
val FrostedCardBackground = Color(0x991C1C1E) // #1C1C1E/60
val FrostedCardSolid = Color(0xFF1C1C1E)
val FrostedCardBorder = Color(0x24FFFFFF) // border-[#ffffff10] - slightly enhanced for crispness
val FrostedCardBorderSubtle = Color(0x14FFFFFF)
val FrostedSurfaceVariant = Color(0x592E3033) // #2E3033 semi-translucent
val FrostedSurfaceVariantSolid = Color(0xFF2E3033)

// Frosted Text Tokens
val FrostedTextPrimary = Color(0xFFE2E2E6)
val FrostedTextSecondary = Color(0xFF919194)
val FrostedTextMuted = Color(0xFFC4C7C5)

// Frosted Accent & Gradient Tokens
val FrostedAccentIce = Color(0xFFD0E4FF)
val FrostedAccentIceDark = Color(0xFF00315B)
val FrostedAccentIceEnd = Color(0xFFADC6FF)
val FrostedDivider = Color(0x3344474E)

// Gradients
val FrostedBrandGradient = Brush.horizontalGradient(
    colors = listOf(
        FrostedAccentIce,
        FrostedAccentIceEnd
    )
)

val FrostedStoryRingBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFD600),
        Color(0xFFFF0069),
        Color(0xFF7638FA)
    )
)

// Legacy TeaGram Story Gradient Brush for compatibility
val TeaGramStoryBrush = FrostedStoryRingBrush

// Neutral & Backgrounds
val InstaLightBackground = Color(0xFFF5F7FA)
val InstaLightSurface = Color(0xFFFFFFFF)
val InstaLightDivider = Color(0xFFDBDBDB)
val InstaLightSecondaryText = Color(0xFF737373)
val InstaLightPrimaryText = Color(0xFF262626)

val InstaDarkBackground = FrostedCanvas
val InstaDarkSurface = FrostedCardSolid
val InstaDarkDivider = FrostedDivider
val InstaDarkSecondaryText = FrostedTextSecondary
val InstaDarkPrimaryText = FrostedTextPrimary

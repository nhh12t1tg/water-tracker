package com.watertracker.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. PRIMARY HYDRATION PALETTE (Cyan, Mint, Deep Blue)
// ==========================================
val CyanPrimary = Color(0xFF06B6D4)       // Vibrant Cyan / Water
val CyanLight = Color(0xFF22D3EE)
val CyanDark = Color(0xFF0891B2)

val MintAccent = Color(0xFF10B981)        // Fresh Mint / Hydrated status
val MintLight = Color(0xFF34D399)
val MintDark = Color(0xFF059669)

val DeepBlueDark = Color(0xFF090E17)      // Deep Ocean / Dark Mode Background
val DeepBlueCard = Color(0xFF131E31)      // Dark Card Surface
val SkyBlueLight = Color(0xFFF0F9FF)      // Crystal Ice / Light Mode Background
val WhiteCard = Color(0xFFFFFFFF)

// ==========================================
// 2. GLASSMORPHISM TOKENS
// ==========================================
// Light Mode Glass
val GlassLightBackground = Color(0xD9FFFFFF) // 85% White
val GlassLightBorder = Color(0x66FFFFFF)     // 40% White border reflection
val GlassLightHighlight = Color(0x3306B6D4)  // Subtle cyan tint glow

// Dark Mode Glass
val GlassDarkBackground = Color(0x99131E31)  // 60% Deep Blue
val GlassDarkBorder = Color(0x3338BDF8)      // 20% Sky Blue border glow
val GlassDarkHighlight = Color(0x2606B6D4)

// ==========================================
// 3. LIQUID WATER GRADIENTS
// ==========================================
val WaterGradientCyan = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF22D3EE), // Top light wave
        Color(0xFF06B6D4), // Body
        Color(0xFF0284C7)  // Deep floor
    )
)

val WaterGradientMint = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF34D399),
        Color(0xFF10B981),
        Color(0xFF047857)
    )
)

val WaterGradientSunset = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF472B6),
        Color(0xFFFB923C),
        Color(0xFFFBBF24)
    )
)

// Gold Reward Accent (for Gift & Streaks)
val GoldReward = Color(0xFFF59E0B)
val FlameOrange = Color(0xFFFF5722)

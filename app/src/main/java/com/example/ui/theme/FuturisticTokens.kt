package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized Design Tokens for the Futuristic AI Visual Language.
 * Conforms to Google-inspired minimal aesthetic:
 * - Extremely clean white/light background
 * - Large intentional whitespace
 * - Soft blue particle/dot field
 * - Rounded pill-shaped controls
 * - Black/dark high-contrast primary buttons
 * - Soft light-gray secondary surfaces
 * - Thin borders and subtle shadows
 * - 8dp-based spacing and strict corner radii (Small 12dp, Medium 20dp, Large 28dp, Pill 999dp)
 */
object FuturisticTokens {

    object LightColors {
        val background = Color(0xFFFCFDFF) // Extremely clean near-white
        val primaryText = Color(0xFF101318) // Near-black high contrast
        val secondaryText = Color(0xFF5E6573) // Neutral calm gray
        val tertiaryText = Color(0xFF8A92A2) // Subdued text
        val primaryButton = Color(0xFF14171D) // High-contrast black pill
        val primaryButtonText = Color(0xFFFFFFFF) // Crisp white
        val surface = Color(0xFFF2F4F8) // Very light gray secondary
        val surfaceElevated = Color(0xFFFFFFFF) // Clean elevated white
        val border = Color(0xFFE2E6EE) // Subtle thin border
        val borderLight = Color(0xFFEEF1F6)
        val accent = Color(0xFF4285F4) // Soft Google-like blue
        val accentSubtle = Color(0xFFE8F0FE) // Light blue tint
        val particleDot = Color(0xFF60A5FA) // Particle blue
        val shadow = Color(0x0A000000)
    }

    object DarkColors {
        val background = Color(0xFF0C0F14) // Deep near-black
        val primaryText = Color(0xFFF1F4F9) // Soft white
        val secondaryText = Color(0xFF98A2B3) // Neutral light-mid gray
        val tertiaryText = Color(0xFF667085) // Subdued dark gray
        val primaryButton = Color(0xFFF2F4F7) // Contrast light button in dark mode
        val primaryButtonText = Color(0xFF0C0F14) // Near-black text
        val surface = Color(0xFF161B22) // Dark-gray surface
        val surfaceElevated = Color(0xFF1F2530) // Elevated dark surface
        val border = Color(0xFF262E3B) // Subtle dark border
        val borderLight = Color(0xFF1C222C)
        val accent = Color(0xFF8AB4F8) // Soft futuristic light blue
        val accentSubtle = Color(0xFF17283E) // Dark blue tint
        val particleDot = Color(0xFF38BDF8) // Particle blue glow
        val shadow = Color(0x33000000)
    }

    object CornerRadius {
        val small: Dp = 12.dp
        val medium: Dp = 20.dp
        val large: Dp = 28.dp
        val pill: Dp = 999.dp

        val smallShape = RoundedCornerShape(small)
        val mediumShape = RoundedCornerShape(medium)
        val largeShape = RoundedCornerShape(large)
        val pillShape = RoundedCornerShape(pill)
    }

    object Spacing {
        val xxs: Dp = 4.dp
        val xs: Dp = 8.dp
        val sm: Dp = 12.dp
        val md: Dp = 16.dp
        val lg: Dp = 24.dp
        val xl: Dp = 32.dp
        val xxl: Dp = 40.dp
        val hero: Dp = 48.dp
        val huge: Dp = 64.dp
    }
}

/**
 * Helper to retrieve currently active FuturisticTokens based on theme.
 */
class FuturisticColorPalette(
    val background: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val primaryButton: Color,
    val primaryButtonText: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val borderLight: Color,
    val accent: Color,
    val accentSubtle: Color,
    val particleDot: Color,
    val shadow: Color,
    val isDark: Boolean
)

val FuturisticTheme: FuturisticColorPalette
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = isSystemInDarkTheme()
        return if (isDark) {
            FuturisticColorPalette(
                background = FuturisticTokens.DarkColors.background,
                primaryText = FuturisticTokens.DarkColors.primaryText,
                secondaryText = FuturisticTokens.DarkColors.secondaryText,
                tertiaryText = FuturisticTokens.DarkColors.tertiaryText,
                primaryButton = FuturisticTokens.DarkColors.primaryButton,
                primaryButtonText = FuturisticTokens.DarkColors.primaryButtonText,
                surface = FuturisticTokens.DarkColors.surface,
                surfaceElevated = FuturisticTokens.DarkColors.surfaceElevated,
                border = FuturisticTokens.DarkColors.border,
                borderLight = FuturisticTokens.DarkColors.borderLight,
                accent = FuturisticTokens.DarkColors.accent,
                accentSubtle = FuturisticTokens.DarkColors.accentSubtle,
                particleDot = FuturisticTokens.DarkColors.particleDot,
                shadow = FuturisticTokens.DarkColors.shadow,
                isDark = true
            )
        } else {
            FuturisticColorPalette(
                background = FuturisticTokens.LightColors.background,
                primaryText = FuturisticTokens.LightColors.primaryText,
                secondaryText = FuturisticTokens.LightColors.secondaryText,
                tertiaryText = FuturisticTokens.LightColors.tertiaryText,
                primaryButton = FuturisticTokens.LightColors.primaryButton,
                primaryButtonText = FuturisticTokens.LightColors.primaryButtonText,
                surface = FuturisticTokens.LightColors.surface,
                surfaceElevated = FuturisticTokens.LightColors.surfaceElevated,
                border = FuturisticTokens.LightColors.border,
                borderLight = FuturisticTokens.LightColors.borderLight,
                accent = FuturisticTokens.LightColors.accent,
                accentSubtle = FuturisticTokens.LightColors.accentSubtle,
                particleDot = FuturisticTokens.LightColors.particleDot,
                shadow = FuturisticTokens.LightColors.shadow,
                isDark = false
            )
        }
    }

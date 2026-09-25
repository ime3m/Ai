package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.regional.RegionalStyleDefinition
import com.example.ui.theme.regional.RegionalStyleRegistry
import com.example.ui.theme.tokens.*

enum class ThemeMode {
    LIGHT, DARK, AMOLED, SYSTEM
}

data class ThemeMetadata(
    val id: String,
    val name: String,
    val description: String,
    val mode: ThemeMode,
    val isDark: Boolean,
    val previewPrimary: Color,
    val previewAccent: Color
)

/**
 * Complete Theme Definition hierarchy
 */
data class ThemeDefinition(
    val metadata: ThemeMetadata,
    val colors: ColorTokens,
    val typography: AppTypography,
    val spacing: AppSpacing = AppSpacing(),
    val radius: AppRadius = AppRadius(),
    val elevation: AppElevation = AppElevation(),
    val border: AppBorder,
    val motion: AppMotion = AppMotion(),
    val voice: VoiceTokens,
    val regionalStyle: RegionalStyleDefinition = RegionalStyleRegistry.global
) {
    /**
     * Applies a regional style overlay without mutating the base theme.
     */
    fun withRegionalStyle(regional: RegionalStyleDefinition): ThemeDefinition {
        val updatedTypography = TypographyTokenFactory.createTypography(
            fontFamily = typography.fontFamily,
            scriptLineHeightMultiplier = regional.scriptLineHeightMultiplier
        )

        val updatedAccents = regional.regionalAccentOverride ?: colors.accent
        val updatedColors = colors.copy(
            accent = updatedAccents
        )

        val updatedVoice = if (regional.regionalGlowTint != Color.Transparent) {
            voice.copy(
                listening = voice.listening.copy(glow = regional.regionalGlowTint),
                speaking = voice.speaking.copy(glow = regional.regionalGlowTint)
            )
        } else {
            voice
        }

        return copy(
            colors = updatedColors,
            typography = updatedTypography,
            voice = updatedVoice,
            regionalStyle = regional
        )
    }
}

/**
 * Contrast & Accessibility Safety Validator
 * Ensures custom theme token combinations satisfy WCAG AA readability.
 */
object ThemeSafety {
    /**
     * Calculates relative contrast ratio between two colors (1.0 to 21.0).
     */
    fun contrastRatio(background: Color, foreground: Color): Double {
        val l1 = background.luminance().toDouble() + 0.05
        val l2 = foreground.foregroundLuminance().toDouble() + 0.05
        return if (l1 > l2) l1 / l2 else l2 / l1
    }

    private fun Color.foregroundLuminance(): Float = luminance()

    /**
     * Ensures high readability on any background:
     * Returns white for dark backgrounds, dark neutral for light backgrounds.
     */
    fun readableTextColor(background: Color): Color {
        return if (background.luminance() > 0.45f) {
            PrimitiveTokens.neutral950
        } else {
            PrimitiveTokens.white90
        }
    }
}

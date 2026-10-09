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
 * - Clean adaptive background (near-white in Light mode, deep charcoal in Dark mode)
 * - Large intentional whitespace
 * - Soft particle dot field
 * - Rounded pill-shaped controls
 * - High-contrast primary buttons
 * - Soft secondary surfaces
 * - Consistent border, divider, error, and success tokens
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
        val surfaceVariant = Color(0xFFE9EDF5) // Mid-tint surface
        val border = Color(0xFFE2E6EE) // Subtle thin border
        val borderLight = Color(0xFFEEF1F6)
        val divider = Color(0xFFE2E6EE)
        val accent = Color(0xFF4285F4) // Soft Google-like blue
        val accentSubtle = Color(0xFFE8F0FE) // Light blue tint
        val primary = Color(0xFF14171D)
        val secondary = Color(0xFF4285F4)
        val error = Color(0xFFDC2626) // Accessible red
        val errorContainer = Color(0xFFFEF2F2)
        val onError = Color(0xFFFFFFFF)
        val success = Color(0xFF16A34A) // Accessible green
        val inputBackground = Color(0xFFF2F4F8)
        val chatUserBackground = Color(0xFF14171D)
        val chatUserText = Color(0xFFFFFFFF)
        val chatAssistantBackground = Color(0xFFF2F4F8)
        val chatAssistantText = Color(0xFF101318)
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
        val surfaceVariant = Color(0xFF262E3B)
        val border = Color(0xFF262E3B) // Subtle dark border
        val borderLight = Color(0xFF1C222C)
        val divider = Color(0xFF262E3B)
        val accent = Color(0xFF8AB4F8) // Soft futuristic light blue
        val accentSubtle = Color(0xFF17283E) // Dark blue tint
        val primary = Color(0xFF8AB4F8)
        val secondary = Color(0xFF38BDF8)
        val error = Color(0xFFF87171) // Readable soft red in dark
        val errorContainer = Color(0xFF3B1818)
        val onError = Color(0xFFFFFFFF)
        val success = Color(0xFF34D399) // Readable soft green in dark
        val inputBackground = Color(0xFF161B22)
        val chatUserBackground = Color(0xFF2563EB)
        val chatUserText = Color(0xFFFFFFFF)
        val chatAssistantBackground = Color(0xFF161B22)
        val chatAssistantText = Color(0xFFF1F4F9)
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
 * Centralized Design Token accessor for the Futuristic UI.
 * Conforms to Section 13 of the design specification:
 * Background, Surface, SurfaceVariant, Primary, Secondary, TextPrimary, TextSecondary,
 * Border, Divider, Error, Success, InputBackground, ChatUserBackground, ChatAssistantBackground.
 */
class FuturisticColorPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val secondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val border: Color,
    val borderLight: Color,
    val divider: Color,
    val error: Color,
    val errorContainer: Color,
    val onError: Color,
    val success: Color,
    val inputBackground: Color,
    val chatUserBackground: Color,
    val chatUserText: Color,
    val chatAssistantBackground: Color,
    val chatAssistantText: Color,
    val primaryButton: Color,
    val primaryButtonText: Color,
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
        val appTheme = LocalAppTheme.current
        val colors = appTheme.colors
        val isDark = appTheme.metadata.isDark
        return FuturisticColorPalette(
            background = colors.background.primary,
            surface = colors.surface.primary,
            surfaceElevated = colors.surface.elevated,
            surfaceVariant = colors.surface.secondary,
            primary = colors.accent.primary,
            secondary = colors.accent.secondary,
            textPrimary = colors.text.primary,
            textSecondary = colors.text.secondary,
            primaryText = colors.text.primary,
            secondaryText = colors.text.secondary,
            tertiaryText = colors.text.tertiary,
            border = colors.border.default,
            borderLight = colors.border.subtle,
            divider = colors.border.default,
            error = colors.status.error,
            errorContainer = if (isDark) Color(0xFF3B1818) else Color(0xFFFEF2F2),
            onError = Color.White,
            success = colors.status.success,
            inputBackground = colors.surface.input,
            chatUserBackground = colors.component.chatBubble.user.background,
            chatUserText = colors.component.chatBubble.user.text,
            chatAssistantBackground = colors.component.chatBubble.ai.background,
            chatAssistantText = colors.component.chatBubble.ai.text,
            primaryButton = colors.component.voiceButton.background,
            primaryButtonText = colors.component.voiceButton.foreground,
            accent = colors.accent.primary,
            accentSubtle = colors.accent.secondary.copy(alpha = 0.15f),
            particleDot = colors.accent.secondary,
            shadow = if (isDark) Color(0x33000000) else Color(0x0A000000),
            isDark = isDark
        )
    }

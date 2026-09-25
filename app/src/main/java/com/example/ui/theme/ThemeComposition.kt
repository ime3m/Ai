package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.presets.MidnightTheme
import com.example.ui.theme.regional.RegionalStyleDefinition
import com.example.ui.theme.tokens.*

/**
 * CompositionLocal for centralized semantic design tokens.
 */
val LocalAppTheme = staticCompositionLocalOf<ThemeDefinition> {
    MidnightTheme.definition
}

/**
 * Global Semantic Token Accessor
 * Usage in UI components:
 *   AppTheme.colors.background.primary
 *   AppTheme.colors.text.primary
 *   AppTheme.voice.listening.background
 *   AppTheme.voice.waveform.speaking
 *   AppTheme.spacing.s4
 *   AppTheme.radius.xl
 *   AppTheme.border.style.default
 */
object AppTheme {
    val current: ThemeDefinition
        @Composable
        get() = LocalAppTheme.current

    val colors: ColorTokens
        @Composable
        get() = LocalAppTheme.current.colors

    val typography: AppTypography
        @Composable
        get() = LocalAppTheme.current.typography

    val spacing: AppSpacing
        @Composable
        get() = LocalAppTheme.current.spacing

    val radius: AppRadius
        @Composable
        get() = LocalAppTheme.current.radius

    val elevation: AppElevation
        @Composable
        get() = LocalAppTheme.current.elevation

    val border: AppBorder
        @Composable
        get() = LocalAppTheme.current.border

    val motion: AppMotion
        @Composable
        get() = LocalAppTheme.current.motion

    val voice: VoiceTokens
        @Composable
        get() = LocalAppTheme.current.voice

    val regional: RegionalStyleDefinition
        @Composable
        get() = LocalAppTheme.current.regionalStyle
}

/**
 * Maps semantic tokens directly to Material 3 ColorScheme.
 */
fun ThemeDefinition.toMaterialColorScheme(): ColorScheme {
    return ColorScheme(
        primary = colors.accent.primary,
        onPrimary = colors.text.inverse,
        primaryContainer = colors.surface.secondary,
        onPrimaryContainer = colors.text.primary,
        inversePrimary = colors.accent.secondary,
        secondary = colors.accent.secondary,
        onSecondary = colors.text.inverse,
        secondaryContainer = colors.surface.secondary,
        onSecondaryContainer = colors.text.primary,
        tertiary = colors.accent.tertiary,
        onTertiary = colors.text.inverse,
        tertiaryContainer = colors.surface.elevated,
        onTertiaryContainer = colors.text.primary,
        background = colors.background.primary,
        onBackground = colors.text.primary,
        surface = colors.surface.card,
        onSurface = colors.text.primary,
        surfaceVariant = colors.surface.secondary,
        onSurfaceVariant = colors.text.secondary,
        surfaceTint = colors.accent.primary,
        inverseSurface = colors.text.primary,
        inverseOnSurface = colors.background.primary,
        error = colors.status.error,
        onError = colors.text.inverse,
        errorContainer = colors.status.error.copy(alpha = 0.2f),
        onErrorContainer = colors.status.error,
        outline = colors.border.default,
        outlineVariant = colors.border.subtle,
        scrim = colors.overlay.scrim
    )
}

/**
 * Maps AppTypography tokens to Material 3 Typography.
 */
fun ThemeDefinition.toMaterialTypography(): Typography {
    return Typography(
        displayLarge = typography.display.large,
        displayMedium = typography.display.medium,
        displaySmall = typography.display.small,
        headlineLarge = typography.heading.h1,
        headlineMedium = typography.heading.h2,
        headlineSmall = typography.heading.h3,
        titleLarge = typography.heading.h2,
        titleMedium = typography.heading.h3,
        titleSmall = typography.label.large,
        bodyLarge = typography.body.large,
        bodyMedium = typography.body.medium,
        bodySmall = typography.body.small,
        labelLarge = typography.label.large,
        labelMedium = typography.label.medium,
        labelSmall = typography.label.small
    )
}

package com.example.ui.theme.presets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.theme.tokens.*

object LightTheme {
    val definition = ThemeDefinition(
        metadata = ThemeMetadata(
            id = "light",
            name = "☀️ Light",
            description = "Clean white/light interface",
            mode = ThemeMode.LIGHT,
            isDark = false,
            previewPrimary = PrimitiveTokens.white100,
            previewAccent = PrimitiveTokens.blue600
        ),
        colors = ColorTokens(
            background = BackgroundTokens(
                primary = FuturisticTokens.LightColors.background,
                secondary = FuturisticTokens.LightColors.surfaceElevated,
                tertiary = FuturisticTokens.LightColors.surface,
                overlay = PrimitiveTokens.blackAlpha30
            ),
            surface = SurfaceTokens(
                primary = FuturisticTokens.LightColors.surfaceElevated,
                secondary = FuturisticTokens.LightColors.surface,
                elevated = FuturisticTokens.LightColors.surfaceElevated,
                card = FuturisticTokens.LightColors.surfaceElevated,
                cardHover = FuturisticTokens.LightColors.surface,
                input = FuturisticTokens.LightColors.surface
            ),
            text = TextTokens(
                primary = FuturisticTokens.LightColors.primaryText,
                secondary = FuturisticTokens.LightColors.secondaryText,
                tertiary = FuturisticTokens.LightColors.tertiaryText,
                muted = PrimitiveTokens.neutral500,
                disabled = PrimitiveTokens.neutral400,
                inverse = PrimitiveTokens.white100
            ),
            accent = AccentTokens(
                primary = FuturisticTokens.LightColors.accent,
                secondary = PrimitiveTokens.blue700,
                tertiary = PrimitiveTokens.purple600
            ),
            border = BorderTokens(
                default = FuturisticTokens.LightColors.border,
                subtle = FuturisticTokens.LightColors.borderLight,
                strong = PrimitiveTokens.neutral300,
                focus = FuturisticTokens.LightColors.accent
            ),
            status = StatusTokens(
                success = PrimitiveTokens.green600,
                warning = PrimitiveTokens.amber600,
                error = PrimitiveTokens.red600,
                info = FuturisticTokens.LightColors.accent,
                neutral = PrimitiveTokens.neutral600
            ),
            overlay = OverlayTokens(
                scrim = PrimitiveTokens.blackAlpha50,
                modal = FuturisticTokens.LightColors.surfaceElevated,
                pressed = PrimitiveTokens.blackAlpha10
            ),
            component = ComponentTokens(
                voiceButton = VoiceButtonTokens(
                    background = FuturisticTokens.LightColors.primaryButton,
                    foreground = FuturisticTokens.LightColors.primaryButtonText,
                    glow = FuturisticTokens.LightColors.accent.copy(alpha = 0.25f),
                    activeRing = FuturisticTokens.LightColors.accent
                ),
                chatBubble = ChatBubbleTokens(
                    user = BubbleTokens(
                        background = FuturisticTokens.LightColors.primaryButton,
                        text = FuturisticTokens.LightColors.primaryButtonText,
                        border = Color.Transparent
                    ),
                    ai = BubbleTokens(
                        background = FuturisticTokens.LightColors.surface,
                        text = FuturisticTokens.LightColors.primaryText,
                        border = FuturisticTokens.LightColors.border
                    )
                ),
                waveform = WaveformComponentTokens(
                    active = FuturisticTokens.LightColors.accent,
                    idle = PrimitiveTokens.neutral400,
                    processing = PrimitiveTokens.purple600,
                    speaking = FuturisticTokens.LightColors.accent
                ),
                button = ButtonTokens(
                    primary = ButtonTokenPair(
                        background = FuturisticTokens.LightColors.primaryButton,
                        text = FuturisticTokens.LightColors.primaryButtonText
                    ),
                    secondary = ButtonTokenPair(
                        background = FuturisticTokens.LightColors.surface,
                        text = FuturisticTokens.LightColors.primaryText
                    )
                )
            )
        ),
        typography = TypographyTokenFactory.createTypography(),
        spacing = AppSpacing(),
        radius = AppRadius(),
        elevation = AppElevation(xs = 1.dp, sm = 2.dp, md = 4.dp, lg = 8.dp, floating = 12.dp),
        border = AppBorder(
            style = BorderStyleTokens(
                default = PrimitiveTokens.neutral200,
                subtle = PrimitiveTokens.neutral100,
                focus = PrimitiveTokens.blue600,
                active = PrimitiveTokens.blue500
            )
        ),
        motion = AppMotion(),
        voice = VoiceTokens(
            idle = VoiceStateTokens(
                background = PrimitiveTokens.neutral100,
                foreground = PrimitiveTokens.neutral900,
                ring = PrimitiveTokens.neutral300
            ),
            listening = VoiceStateTokens(
                background = PrimitiveTokens.red600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.red400,
                glow = PrimitiveTokens.red500.copy(alpha = 0.35f)
            ),
            processing = VoiceStateTokens(
                background = PrimitiveTokens.purple600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.purple400,
                glow = PrimitiveTokens.purple500.copy(alpha = 0.3f)
            ),
            speaking = VoiceStateTokens(
                background = PrimitiveTokens.blue600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.blue400,
                glow = PrimitiveTokens.blue500.copy(alpha = 0.3f)
            ),
            waveform = WaveformTokens(
                idle = PrimitiveTokens.neutral400,
                listening = PrimitiveTokens.red500,
                processing = PrimitiveTokens.purple600,
                speaking = PrimitiveTokens.blue600
            )
        )
    )
}

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
                primary = PrimitiveTokens.neutral50,
                secondary = PrimitiveTokens.white100,
                tertiary = PrimitiveTokens.neutral100,
                overlay = PrimitiveTokens.blackAlpha30
            ),
            surface = SurfaceTokens(
                primary = PrimitiveTokens.white100,
                secondary = PrimitiveTokens.neutral100,
                elevated = PrimitiveTokens.white100,
                card = PrimitiveTokens.white100,
                cardHover = PrimitiveTokens.neutral50,
                input = PrimitiveTokens.neutral100
            ),
            text = TextTokens(
                primary = PrimitiveTokens.neutral950,
                secondary = PrimitiveTokens.neutral700,
                tertiary = PrimitiveTokens.neutral600,
                muted = PrimitiveTokens.neutral500,
                disabled = PrimitiveTokens.neutral400,
                inverse = PrimitiveTokens.white100
            ),
            accent = AccentTokens(
                primary = PrimitiveTokens.blue600,
                secondary = PrimitiveTokens.blue700,
                tertiary = PrimitiveTokens.purple600
            ),
            border = BorderTokens(
                default = PrimitiveTokens.neutral200,
                subtle = PrimitiveTokens.neutral100,
                strong = PrimitiveTokens.neutral300,
                focus = PrimitiveTokens.blue600
            ),
            status = StatusTokens(
                success = PrimitiveTokens.green600,
                warning = PrimitiveTokens.amber600,
                error = PrimitiveTokens.red600,
                info = PrimitiveTokens.blue600,
                neutral = PrimitiveTokens.neutral600
            ),
            overlay = OverlayTokens(
                scrim = PrimitiveTokens.blackAlpha50,
                modal = PrimitiveTokens.white100,
                pressed = PrimitiveTokens.blackAlpha10
            ),
            component = ComponentTokens(
                voiceButton = VoiceButtonTokens(
                    background = PrimitiveTokens.blue600,
                    foreground = PrimitiveTokens.white100,
                    glow = PrimitiveTokens.blue500.copy(alpha = 0.25f),
                    activeRing = PrimitiveTokens.blue400
                ),
                chatBubble = ChatBubbleTokens(
                    user = BubbleTokens(
                        background = PrimitiveTokens.blue600,
                        text = PrimitiveTokens.white100,
                        border = Color.Transparent
                    ),
                    ai = BubbleTokens(
                        background = PrimitiveTokens.neutral100,
                        text = PrimitiveTokens.neutral950,
                        border = PrimitiveTokens.neutral200
                    )
                ),
                waveform = WaveformComponentTokens(
                    active = PrimitiveTokens.blue600,
                    idle = PrimitiveTokens.neutral400,
                    processing = PrimitiveTokens.purple600,
                    speaking = PrimitiveTokens.teal600
                ),
                button = ButtonTokens(
                    primary = ButtonTokenPair(
                        background = PrimitiveTokens.blue600,
                        text = PrimitiveTokens.white100
                    ),
                    secondary = ButtonTokenPair(
                        background = PrimitiveTokens.neutral100,
                        text = PrimitiveTokens.neutral900
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

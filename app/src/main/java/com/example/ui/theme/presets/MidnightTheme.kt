package com.example.ui.theme.presets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.theme.tokens.*

object MidnightTheme {
    val definition = ThemeDefinition(
        metadata = ThemeMetadata(
            id = "midnight",
            name = "🌑 Midnight Voice",
            description = "Premium dark AI interface with black/deep-blue background and subtle glowing accents",
            mode = ThemeMode.DARK,
            isDark = true,
            previewPrimary = PrimitiveTokens.neutral900,
            previewAccent = PrimitiveTokens.blue500
        ),
        colors = ColorTokens(
            background = BackgroundTokens(
                primary = PrimitiveTokens.neutral950,
                secondary = PrimitiveTokens.neutral900,
                tertiary = PrimitiveTokens.neutral850,
                overlay = PrimitiveTokens.blackAlpha75
            ),
            surface = SurfaceTokens(
                primary = PrimitiveTokens.neutral900,
                secondary = PrimitiveTokens.neutral800,
                elevated = PrimitiveTokens.neutral850,
                card = PrimitiveTokens.neutral900,
                cardHover = PrimitiveTokens.neutral800,
                input = PrimitiveTokens.neutral900
            ),
            text = TextTokens(
                primary = PrimitiveTokens.white90,
                secondary = PrimitiveTokens.neutral300,
                tertiary = PrimitiveTokens.neutral400,
                muted = PrimitiveTokens.neutral500,
                disabled = PrimitiveTokens.neutral600,
                inverse = PrimitiveTokens.neutral950
            ),
            accent = AccentTokens(
                primary = PrimitiveTokens.blue500,
                secondary = PrimitiveTokens.blue400,
                tertiary = PrimitiveTokens.purple400
            ),
            border = BorderTokens(
                default = PrimitiveTokens.neutral800,
                subtle = PrimitiveTokens.neutral850,
                strong = PrimitiveTokens.neutral700,
                focus = PrimitiveTokens.blue500
            ),
            status = StatusTokens(
                success = PrimitiveTokens.green500,
                warning = PrimitiveTokens.amber500,
                error = PrimitiveTokens.red500,
                info = PrimitiveTokens.blue400,
                neutral = PrimitiveTokens.neutral400
            ),
            overlay = OverlayTokens(
                scrim = PrimitiveTokens.blackAlpha50,
                modal = PrimitiveTokens.neutral900,
                pressed = PrimitiveTokens.whiteAlpha10
            ),
            component = ComponentTokens(
                voiceButton = VoiceButtonTokens(
                    background = PrimitiveTokens.blue600,
                    foreground = PrimitiveTokens.white100,
                    glow = PrimitiveTokens.blue500.copy(alpha = 0.35f),
                    activeRing = PrimitiveTokens.blue400
                ),
                chatBubble = ChatBubbleTokens(
                    user = BubbleTokens(
                        background = PrimitiveTokens.neutral800,
                        text = PrimitiveTokens.white90,
                        border = PrimitiveTokens.neutral700
                    ),
                    ai = BubbleTokens(
                        background = PrimitiveTokens.neutral900,
                        text = PrimitiveTokens.white90,
                        border = PrimitiveTokens.neutral800
                    )
                ),
                waveform = WaveformComponentTokens(
                    active = PrimitiveTokens.blue400,
                    idle = PrimitiveTokens.neutral600,
                    processing = PrimitiveTokens.purple400,
                    speaking = PrimitiveTokens.teal400
                ),
                button = ButtonTokens(
                    primary = ButtonTokenPair(
                        background = PrimitiveTokens.blue600,
                        text = PrimitiveTokens.white100
                    ),
                    secondary = ButtonTokenPair(
                        background = PrimitiveTokens.neutral800,
                        text = PrimitiveTokens.white90
                    )
                )
            )
        ),
        typography = TypographyTokenFactory.createTypography(),
        spacing = AppSpacing(),
        radius = AppRadius(),
        elevation = AppElevation(xs = 1.dp, md = 4.dp),
        border = AppBorder(
            style = BorderStyleTokens(
                default = PrimitiveTokens.neutral800,
                subtle = PrimitiveTokens.neutral850,
                focus = PrimitiveTokens.blue500,
                active = PrimitiveTokens.blue400
            )
        ),
        motion = AppMotion(),
        voice = VoiceTokens(
            idle = VoiceStateTokens(
                background = PrimitiveTokens.neutral800,
                foreground = PrimitiveTokens.white90,
                ring = PrimitiveTokens.neutral700
            ),
            listening = VoiceStateTokens(
                background = PrimitiveTokens.red600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.red400,
                glow = PrimitiveTokens.red500.copy(alpha = 0.4f)
            ),
            processing = VoiceStateTokens(
                background = PrimitiveTokens.purple600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.purple400,
                glow = PrimitiveTokens.purple500.copy(alpha = 0.35f)
            ),
            speaking = VoiceStateTokens(
                background = PrimitiveTokens.blue600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.blue300,
                glow = PrimitiveTokens.blue500.copy(alpha = 0.35f)
            ),
            waveform = WaveformTokens(
                idle = PrimitiveTokens.neutral600,
                listening = PrimitiveTokens.red400,
                processing = PrimitiveTokens.purple400,
                speaking = PrimitiveTokens.blue400
            )
        )
    )
}

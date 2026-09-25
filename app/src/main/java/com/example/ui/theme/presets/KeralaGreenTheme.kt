package com.example.ui.theme.presets

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import com.example.ui.theme.tokens.*

object KeralaGreenTheme {
    val definition = ThemeDefinition(
        metadata = ThemeMetadata(
            id = "kerala-green",
            name = "🌴 Kerala Green",
            description = "Deep green/natural theme inspired by Kerala",
            mode = ThemeMode.DARK,
            isDark = true,
            previewPrimary = Color(0xFF0D1711),
            previewAccent = PrimitiveTokens.green500
        ),
        colors = ColorTokens(
            background = BackgroundTokens(
                primary = Color(0xFF080F0B),
                secondary = Color(0xFF0F1B14),
                tertiary = Color(0xFF17291E),
                overlay = PrimitiveTokens.blackAlpha75
            ),
            surface = SurfaceTokens(
                primary = Color(0xFF0F1B14),
                secondary = Color(0xFF17291E),
                elevated = Color(0xFF1E3628),
                card = Color(0xFF0F1B14),
                cardHover = Color(0xFF17291E),
                input = Color(0xFF0F1B14)
            ),
            text = TextTokens(
                primary = PrimitiveTokens.white90,
                secondary = Color(0xFFD1E7D9),
                tertiary = Color(0xFFA3C7B0),
                muted = Color(0xFF759E84),
                disabled = Color(0xFF4C6E59),
                inverse = Color(0xFF080F0B)
            ),
            accent = AccentTokens(
                primary = PrimitiveTokens.green500,
                secondary = PrimitiveTokens.amber500,
                tertiary = PrimitiveTokens.green400
            ),
            border = BorderTokens(
                default = Color(0xFF1F382A),
                subtle = Color(0xFF15261C),
                strong = Color(0xFF2C523D),
                focus = PrimitiveTokens.green500
            ),
            status = StatusTokens(
                success = PrimitiveTokens.green500,
                warning = PrimitiveTokens.amber500,
                error = PrimitiveTokens.red500,
                info = PrimitiveTokens.teal400,
                neutral = Color(0xFF759E84)
            ),
            overlay = OverlayTokens(
                scrim = PrimitiveTokens.blackAlpha75,
                modal = Color(0xFF0F1B14),
                pressed = PrimitiveTokens.whiteAlpha10
            ),
            component = ComponentTokens(
                voiceButton = VoiceButtonTokens(
                    background = PrimitiveTokens.green600,
                    foreground = PrimitiveTokens.white100,
                    glow = PrimitiveTokens.green500.copy(alpha = 0.4f),
                    activeRing = PrimitiveTokens.green400
                ),
                chatBubble = ChatBubbleTokens(
                    user = BubbleTokens(
                        background = Color(0xFF173322),
                        text = PrimitiveTokens.white90,
                        border = Color(0xFF234D34)
                    ),
                    ai = BubbleTokens(
                        background = Color(0xFF0F1B14),
                        text = PrimitiveTokens.white90,
                        border = Color(0xFF1F382A)
                    )
                ),
                waveform = WaveformComponentTokens(
                    active = PrimitiveTokens.green400,
                    idle = Color(0xFF4C6E59),
                    processing = PrimitiveTokens.amber400,
                    speaking = PrimitiveTokens.teal400
                ),
                button = ButtonTokens(
                    primary = ButtonTokenPair(
                        background = PrimitiveTokens.green600,
                        text = PrimitiveTokens.white100
                    ),
                    secondary = ButtonTokenPair(
                        background = Color(0xFF17291E),
                        text = PrimitiveTokens.white90
                    )
                )
            )
        ),
        typography = TypographyTokenFactory.createTypography(),
        spacing = AppSpacing(),
        radius = AppRadius(),
        elevation = AppElevation(),
        border = AppBorder(
            style = BorderStyleTokens(
                default = Color(0xFF1F382A),
                subtle = Color(0xFF15261C),
                focus = PrimitiveTokens.green500,
                active = PrimitiveTokens.green400
            )
        ),
        motion = AppMotion(),
        voice = VoiceTokens(
            idle = VoiceStateTokens(
                background = Color(0xFF17291E),
                foreground = PrimitiveTokens.white90,
                ring = Color(0xFF1F382A)
            ),
            listening = VoiceStateTokens(
                background = PrimitiveTokens.red600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.red400,
                glow = PrimitiveTokens.red500.copy(alpha = 0.4f)
            ),
            processing = VoiceStateTokens(
                background = PrimitiveTokens.amber600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.amber400,
                glow = PrimitiveTokens.amber500.copy(alpha = 0.35f)
            ),
            speaking = VoiceStateTokens(
                background = PrimitiveTokens.green600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.green300,
                glow = PrimitiveTokens.green500.copy(alpha = 0.4f)
            ),
            waveform = WaveformTokens(
                idle = Color(0xFF4C6E59),
                listening = PrimitiveTokens.red400,
                processing = PrimitiveTokens.amber400,
                speaking = PrimitiveTokens.green400
            )
        )
    )
}

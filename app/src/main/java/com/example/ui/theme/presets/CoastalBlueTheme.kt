package com.example.ui.theme.presets

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import com.example.ui.theme.tokens.*

object CoastalBlueTheme {
    val definition = ThemeDefinition(
        metadata = ThemeMetadata(
            id = "coastal-blue",
            name = "🌊 Coastal Blue",
            description = "Blue/teal modern coastal theme",
            mode = ThemeMode.DARK,
            isDark = true,
            previewPrimary = Color(0xFF0C1929),
            previewAccent = PrimitiveTokens.teal400
        ),
        colors = ColorTokens(
            background = BackgroundTokens(
                primary = Color(0xFF070E18),
                secondary = Color(0xFF0C1929),
                tertiary = Color(0xFF13253B),
                overlay = PrimitiveTokens.blackAlpha75
            ),
            surface = SurfaceTokens(
                primary = Color(0xFF0C1929),
                secondary = Color(0xFF13253B),
                elevated = Color(0xFF1B3452),
                card = Color(0xFF0C1929),
                cardHover = Color(0xFF13253B),
                input = Color(0xFF0C1929)
            ),
            text = TextTokens(
                primary = PrimitiveTokens.white90,
                secondary = Color(0xFFCFE2F5),
                tertiary = Color(0xFF9FC2E5),
                muted = Color(0xFF6F98C4),
                disabled = Color(0xFF4A6B91),
                inverse = Color(0xFF070E18)
            ),
            accent = AccentTokens(
                primary = PrimitiveTokens.teal400,
                secondary = PrimitiveTokens.blue400,
                tertiary = PrimitiveTokens.teal600
            ),
            border = BorderTokens(
                default = Color(0xFF1B334F),
                subtle = Color(0xFF112236),
                strong = Color(0xFF284D75),
                focus = PrimitiveTokens.teal400
            ),
            status = StatusTokens(
                success = PrimitiveTokens.green400,
                warning = PrimitiveTokens.amber400,
                error = PrimitiveTokens.red400,
                info = PrimitiveTokens.blue400,
                neutral = Color(0xFF6F98C4)
            ),
            overlay = OverlayTokens(
                scrim = PrimitiveTokens.blackAlpha75,
                modal = Color(0xFF0C1929),
                pressed = PrimitiveTokens.whiteAlpha10
            ),
            component = ComponentTokens(
                voiceButton = VoiceButtonTokens(
                    background = PrimitiveTokens.teal500,
                    foreground = PrimitiveTokens.white100,
                    glow = PrimitiveTokens.teal400.copy(alpha = 0.45f),
                    activeRing = PrimitiveTokens.teal300
                ),
                chatBubble = ChatBubbleTokens(
                    user = BubbleTokens(
                        background = Color(0xFF15334E),
                        text = PrimitiveTokens.white90,
                        border = Color(0xFF224E77)
                    ),
                    ai = BubbleTokens(
                        background = Color(0xFF0C1929),
                        text = PrimitiveTokens.white90,
                        border = Color(0xFF1B334F)
                    )
                ),
                waveform = WaveformComponentTokens(
                    active = PrimitiveTokens.teal400,
                    idle = Color(0xFF4A6B91),
                    processing = PrimitiveTokens.blue400,
                    speaking = PrimitiveTokens.teal300
                ),
                button = ButtonTokens(
                    primary = ButtonTokenPair(
                        background = PrimitiveTokens.teal500,
                        text = PrimitiveTokens.white100
                    ),
                    secondary = ButtonTokenPair(
                        background = Color(0xFF13253B),
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
                default = Color(0xFF1B334F),
                subtle = Color(0xFF112236),
                focus = PrimitiveTokens.teal400,
                active = PrimitiveTokens.teal300
            )
        ),
        motion = AppMotion(),
        voice = VoiceTokens(
            idle = VoiceStateTokens(
                background = Color(0xFF13253B),
                foreground = PrimitiveTokens.white90,
                ring = Color(0xFF1B334F)
            ),
            listening = VoiceStateTokens(
                background = PrimitiveTokens.red500,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.red400,
                glow = PrimitiveTokens.red400.copy(alpha = 0.4f)
            ),
            processing = VoiceStateTokens(
                background = PrimitiveTokens.blue600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.blue400,
                glow = PrimitiveTokens.blue400.copy(alpha = 0.35f)
            ),
            speaking = VoiceStateTokens(
                background = PrimitiveTokens.teal500,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.teal300,
                glow = PrimitiveTokens.teal400.copy(alpha = 0.45f)
            ),
            waveform = WaveformTokens(
                idle = Color(0xFF4A6B91),
                listening = PrimitiveTokens.red400,
                processing = PrimitiveTokens.blue400,
                speaking = PrimitiveTokens.teal300
            )
        )
    )
}

package com.example.ui.theme.presets

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import com.example.ui.theme.tokens.*

object KeralaSunsetTheme {
    val definition = ThemeDefinition(
        metadata = ThemeMetadata(
            id = "kerala-sunset",
            name = "🌅 Kerala Sunset",
            description = "Warm orange/red gradient inspired by Kerala sunsets",
            mode = ThemeMode.DARK,
            isDark = true,
            previewPrimary = Color(0xFF1C1815),
            previewAccent = PrimitiveTokens.orange500
        ),
        colors = ColorTokens(
            background = BackgroundTokens(
                primary = Color(0xFF14110F),
                secondary = Color(0xFF1C1815),
                tertiary = Color(0xFF26201C),
                overlay = PrimitiveTokens.blackAlpha75
            ),
            surface = SurfaceTokens(
                primary = Color(0xFF1C1815),
                secondary = Color(0xFF26201C),
                elevated = Color(0xFF332B25),
                card = Color(0xFF1C1815),
                cardHover = Color(0xFF26201C),
                input = Color(0xFF1C1815)
            ),
            text = TextTokens(
                primary = PrimitiveTokens.white90,
                secondary = Color(0xFFE6D6CC),
                tertiary = Color(0xFFB8A295),
                muted = Color(0xFF8C7A70),
                disabled = Color(0xFF5E524B),
                inverse = Color(0xFF14110F)
            ),
            accent = AccentTokens(
                primary = PrimitiveTokens.orange500,
                secondary = PrimitiveTokens.amber500,
                tertiary = PrimitiveTokens.orange300
            ),
            border = BorderTokens(
                default = Color(0xFF382F2A),
                subtle = Color(0xFF2B231F),
                strong = Color(0xFF52443C),
                focus = PrimitiveTokens.orange500
            ),
            status = StatusTokens(
                success = PrimitiveTokens.green500,
                warning = PrimitiveTokens.amber500,
                error = PrimitiveTokens.red500,
                info = PrimitiveTokens.orange400,
                neutral = Color(0xFF8C7A70)
            ),
            overlay = OverlayTokens(
                scrim = PrimitiveTokens.blackAlpha75,
                modal = Color(0xFF1C1815),
                pressed = PrimitiveTokens.whiteAlpha10
            ),
            component = ComponentTokens(
                voiceButton = VoiceButtonTokens(
                    background = PrimitiveTokens.orange600,
                    foreground = PrimitiveTokens.white100,
                    glow = PrimitiveTokens.orange500.copy(alpha = 0.4f),
                    activeRing = PrimitiveTokens.amber400
                ),
                chatBubble = ChatBubbleTokens(
                    user = BubbleTokens(
                        background = Color(0xFF33261F),
                        text = PrimitiveTokens.white90,
                        border = Color(0xFF4D392E)
                    ),
                    ai = BubbleTokens(
                        background = Color(0xFF1C1815),
                        text = PrimitiveTokens.white90,
                        border = Color(0xFF382F2A)
                    )
                ),
                waveform = WaveformComponentTokens(
                    active = PrimitiveTokens.orange400,
                    idle = Color(0xFF5E524B),
                    processing = PrimitiveTokens.amber400,
                    speaking = PrimitiveTokens.orange500
                ),
                button = ButtonTokens(
                    primary = ButtonTokenPair(
                        background = PrimitiveTokens.orange600,
                        text = PrimitiveTokens.white100
                    ),
                    secondary = ButtonTokenPair(
                        background = Color(0xFF26201C),
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
                default = Color(0xFF382F2A),
                subtle = Color(0xFF2B231F),
                focus = PrimitiveTokens.orange500,
                active = PrimitiveTokens.amber400
            )
        ),
        motion = AppMotion(),
        voice = VoiceTokens(
            idle = VoiceStateTokens(
                background = Color(0xFF26201C),
                foreground = PrimitiveTokens.white90,
                ring = Color(0xFF382F2A)
            ),
            listening = VoiceStateTokens(
                background = PrimitiveTokens.orange600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.amber400,
                glow = PrimitiveTokens.orange500.copy(alpha = 0.45f)
            ),
            processing = VoiceStateTokens(
                background = PrimitiveTokens.amber600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.amber400,
                glow = PrimitiveTokens.amber500.copy(alpha = 0.4f)
            ),
            speaking = VoiceStateTokens(
                background = PrimitiveTokens.orange500,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.amber300,
                glow = PrimitiveTokens.orange400.copy(alpha = 0.4f)
            ),
            waveform = WaveformTokens(
                idle = Color(0xFF5E524B),
                listening = PrimitiveTokens.orange400,
                processing = PrimitiveTokens.amber400,
                speaking = PrimitiveTokens.orange500
            )
        )
    )
}

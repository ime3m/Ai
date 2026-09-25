package com.example.ui.theme.presets

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import com.example.ui.theme.tokens.*

object AiPurpleTheme {
    val definition = ThemeDefinition(
        metadata = ThemeMetadata(
            id = "ai-purple",
            name = "💜 AI Purple",
            description = "Futuristic purple/indigo AI theme",
            mode = ThemeMode.DARK,
            isDark = true,
            previewPrimary = Color(0xFF140D21),
            previewAccent = PrimitiveTokens.purple500
        ),
        colors = ColorTokens(
            background = BackgroundTokens(
                primary = Color(0xFF0C0714),
                secondary = Color(0xFF140D21),
                tertiary = Color(0xFF1E1430),
                overlay = PrimitiveTokens.blackAlpha75
            ),
            surface = SurfaceTokens(
                primary = Color(0xFF140D21),
                secondary = Color(0xFF1E1430),
                elevated = Color(0xFF2B1C44),
                card = Color(0xFF140D21),
                cardHover = Color(0xFF1E1430),
                input = Color(0xFF140D21)
            ),
            text = TextTokens(
                primary = PrimitiveTokens.white90,
                secondary = Color(0xFFDFD4F2),
                tertiary = Color(0xFFBEA9E0),
                muted = Color(0xFF8F76B8),
                disabled = Color(0xFF5E4980),
                inverse = Color(0xFF0C0714)
            ),
            accent = AccentTokens(
                primary = PrimitiveTokens.purple500,
                secondary = PrimitiveTokens.purple400,
                tertiary = PrimitiveTokens.blue400
            ),
            border = BorderTokens(
                default = Color(0xFF2E1C4A),
                subtle = Color(0xFF1E1332),
                strong = Color(0xFF452A6E),
                focus = PrimitiveTokens.purple400
            ),
            status = StatusTokens(
                success = PrimitiveTokens.green400,
                warning = PrimitiveTokens.amber400,
                error = PrimitiveTokens.red400,
                info = PrimitiveTokens.purple400,
                neutral = Color(0xFF8F76B8)
            ),
            overlay = OverlayTokens(
                scrim = PrimitiveTokens.blackAlpha75,
                modal = Color(0xFF140D21),
                pressed = PrimitiveTokens.whiteAlpha10
            ),
            component = ComponentTokens(
                voiceButton = VoiceButtonTokens(
                    background = PrimitiveTokens.purple600,
                    foreground = PrimitiveTokens.white100,
                    glow = PrimitiveTokens.purple500.copy(alpha = 0.45f),
                    activeRing = PrimitiveTokens.purple300
                ),
                chatBubble = ChatBubbleTokens(
                    user = BubbleTokens(
                        background = Color(0xFF291A42),
                        text = PrimitiveTokens.white90,
                        border = Color(0xFF422B69)
                    ),
                    ai = BubbleTokens(
                        background = Color(0xFF140D21),
                        text = PrimitiveTokens.white90,
                        border = Color(0xFF2E1C4A)
                    )
                ),
                waveform = WaveformComponentTokens(
                    active = PrimitiveTokens.purple400,
                    idle = Color(0xFF5E4980),
                    processing = PrimitiveTokens.blue400,
                    speaking = PrimitiveTokens.purple300
                ),
                button = ButtonTokens(
                    primary = ButtonTokenPair(
                        background = PrimitiveTokens.purple600,
                        text = PrimitiveTokens.white100
                    ),
                    secondary = ButtonTokenPair(
                        background = Color(0xFF1E1430),
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
                default = Color(0xFF2E1C4A),
                subtle = Color(0xFF1E1332),
                focus = PrimitiveTokens.purple400,
                active = PrimitiveTokens.purple300
            )
        ),
        motion = AppMotion(),
        voice = VoiceTokens(
            idle = VoiceStateTokens(
                background = Color(0xFF1E1430),
                foreground = PrimitiveTokens.white90,
                ring = Color(0xFF2E1C4A)
            ),
            listening = VoiceStateTokens(
                background = PrimitiveTokens.red500,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.red400,
                glow = PrimitiveTokens.red400.copy(alpha = 0.4f)
            ),
            processing = VoiceStateTokens(
                background = PrimitiveTokens.purple600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.purple300,
                glow = PrimitiveTokens.purple400.copy(alpha = 0.45f)
            ),
            speaking = VoiceStateTokens(
                background = PrimitiveTokens.purple500,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.purple200,
                glow = PrimitiveTokens.purple400.copy(alpha = 0.45f)
            ),
            waveform = WaveformTokens(
                idle = Color(0xFF5E4980),
                listening = PrimitiveTokens.red400,
                processing = PrimitiveTokens.blue400,
                speaking = PrimitiveTokens.purple300
            )
        )
    )
}

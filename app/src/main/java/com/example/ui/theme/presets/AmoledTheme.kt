package com.example.ui.theme.presets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.theme.tokens.*

object AmoledTheme {
    val definition = ThemeDefinition(
        metadata = ThemeMetadata(
            id = "amoled",
            name = "⚡ AMOLED Dark",
            description = "Pure black background optimized for OLED displays",
            mode = ThemeMode.AMOLED,
            isDark = true,
            previewPrimary = PrimitiveTokens.blackPure,
            previewAccent = PrimitiveTokens.blue400
        ),
        colors = ColorTokens(
            background = BackgroundTokens(
                primary = PrimitiveTokens.blackPure,
                secondary = PrimitiveTokens.black950,
                tertiary = PrimitiveTokens.black900,
                overlay = PrimitiveTokens.blackAlpha90
            ),
            surface = SurfaceTokens(
                primary = PrimitiveTokens.black950,
                secondary = PrimitiveTokens.black900,
                elevated = PrimitiveTokens.black900,
                card = PrimitiveTokens.black950,
                cardHover = PrimitiveTokens.black900,
                input = PrimitiveTokens.black950
            ),
            text = TextTokens(
                primary = PrimitiveTokens.white100,
                secondary = PrimitiveTokens.white80,
                tertiary = PrimitiveTokens.neutral400,
                muted = PrimitiveTokens.neutral500,
                disabled = PrimitiveTokens.neutral700,
                inverse = PrimitiveTokens.blackPure
            ),
            accent = AccentTokens(
                primary = PrimitiveTokens.blue400,
                secondary = PrimitiveTokens.teal400,
                tertiary = PrimitiveTokens.purple300
            ),
            border = BorderTokens(
                default = PrimitiveTokens.neutral800,
                subtle = PrimitiveTokens.neutral900,
                strong = PrimitiveTokens.neutral600,
                focus = PrimitiveTokens.blue400
            ),
            status = StatusTokens(
                success = PrimitiveTokens.green400,
                warning = PrimitiveTokens.amber400,
                error = PrimitiveTokens.red400,
                info = PrimitiveTokens.blue400,
                neutral = PrimitiveTokens.neutral500
            ),
            overlay = OverlayTokens(
                scrim = PrimitiveTokens.blackAlpha90,
                modal = PrimitiveTokens.black950,
                pressed = PrimitiveTokens.whiteAlpha10
            ),
            component = ComponentTokens(
                voiceButton = VoiceButtonTokens(
                    background = PrimitiveTokens.blue600,
                    foreground = PrimitiveTokens.white100,
                    glow = PrimitiveTokens.blue400.copy(alpha = 0.45f),
                    activeRing = PrimitiveTokens.blue400
                ),
                chatBubble = ChatBubbleTokens(
                    user = BubbleTokens(
                        background = PrimitiveTokens.neutral900,
                        text = PrimitiveTokens.white100,
                        border = PrimitiveTokens.neutral700
                    ),
                    ai = BubbleTokens(
                        background = PrimitiveTokens.black950,
                        text = PrimitiveTokens.white90,
                        border = PrimitiveTokens.neutral800
                    )
                ),
                waveform = WaveformComponentTokens(
                    active = PrimitiveTokens.blue400,
                    idle = PrimitiveTokens.neutral700,
                    processing = PrimitiveTokens.purple400,
                    speaking = PrimitiveTokens.teal400
                ),
                button = ButtonTokens(
                    primary = ButtonTokenPair(
                        background = PrimitiveTokens.blue500,
                        text = PrimitiveTokens.white100
                    ),
                    secondary = ButtonTokenPair(
                        background = PrimitiveTokens.black900,
                        text = PrimitiveTokens.white90
                    )
                )
            )
        ),
        typography = TypographyTokenFactory.createTypography(),
        spacing = AppSpacing(),
        radius = AppRadius(),
        elevation = AppElevation(none = 0.dp, xs = 0.dp, sm = 0.dp, md = 0.dp, lg = 0.dp, floating = 2.dp),
        border = AppBorder(
            style = BorderStyleTokens(
                default = PrimitiveTokens.neutral800,
                subtle = PrimitiveTokens.neutral900,
                focus = PrimitiveTokens.blue400,
                active = PrimitiveTokens.blue300
            )
        ),
        motion = AppMotion(),
        voice = VoiceTokens(
            idle = VoiceStateTokens(
                background = PrimitiveTokens.black900,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.neutral800
            ),
            listening = VoiceStateTokens(
                background = PrimitiveTokens.red500,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.red400,
                glow = PrimitiveTokens.red400.copy(alpha = 0.5f)
            ),
            processing = VoiceStateTokens(
                background = PrimitiveTokens.purple600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.purple400,
                glow = PrimitiveTokens.purple400.copy(alpha = 0.4f)
            ),
            speaking = VoiceStateTokens(
                background = PrimitiveTokens.blue600,
                foreground = PrimitiveTokens.white100,
                ring = PrimitiveTokens.blue400,
                glow = PrimitiveTokens.blue400.copy(alpha = 0.45f)
            ),
            waveform = WaveformTokens(
                idle = PrimitiveTokens.neutral700,
                listening = PrimitiveTokens.red400,
                processing = PrimitiveTokens.purple400,
                speaking = PrimitiveTokens.blue400
            )
        )
    )
}

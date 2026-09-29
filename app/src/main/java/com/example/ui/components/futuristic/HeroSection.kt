package com.example.ui.components.futuristic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.example.ui.components.RmsPulseIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceState
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens
import kotlin.math.abs
import kotlin.math.sin

/**
 * Centered Hero Section for Regional Voice AI.
 * Minimal + Futuristic + Human + Premium + Calm.
 * - Headline: "Speak Naturally."
 * - Subheadline: "AI voice conversations that understand your language, accent and regional way of speaking."
 * - Primary button: "Start Speaking"
 * - Secondary action: "Explore Voices"
 * - Subtle, elegant voice-wave / microphone visual focus element.
 * - Smooth transition when speaking begins (microphone becomes visual focus, responsive waveform emerges).
 */
@Composable
fun HeroSection(
    headline: String = "Speak Naturally.",
    description: String = "AI voice conversations that understand your language, accent and regional way of speaking.",
    buttonText: String = "Start Speaking",
    secondaryButtonText: String = "Explore Voices",
    onButtonClick: () -> Unit,
    onSecondaryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    voiceState: VoiceState = VoiceState.IDLE,
    isThinking: Boolean = false,
    soundLevel: Float = 0f,
    dialectBadgeText: String? = null,
    onBadgeClick: (() -> Unit)? = null
) {
    val isVoiceActive = voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING || isThinking

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .testTag("futuristic_hero_section"),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val headlineSize = when {
            screenWidth > 600.dp -> 54.sp
            screenWidth > 400.dp -> 42.sp
            else -> 34.sp
        }

        val descriptionSize = when {
            screenWidth > 600.dp -> 18.sp
            else -> 15.sp
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
        ) {
            // Optional subtle dialect badge above headline
            if (!dialectBadgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(FuturisticTokens.CornerRadius.pillShape)
                        .background(FuturisticTheme.surface)
                        .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                        .clickable(enabled = onBadgeClick != null) { onBadgeClick?.invoke() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("hero_dialect_badge")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isVoiceActive) FuturisticTheme.accent else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dialectBadgeText,
                            color = FuturisticTheme.primaryText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Minimal, elegant voice/mic focal orb in the hero center
            MinimalVoiceOrb(
                voiceState = voiceState,
                isThinking = isThinking,
                soundLevel = soundLevel,
                onClick = onButtonClick,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Modern, calm Headline: "Speak Naturally."
            Text(
                text = headline,
                color = FuturisticTheme.primaryText,
                fontSize = headlineSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = headlineSize * 1.15f,
                letterSpacing = (-0.8).sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subheadline: "AI voice conversations that understand your language, accent and regional way of speaking."
            Text(
                text = description,
                color = FuturisticTheme.secondaryText,
                fontSize = descriptionSize,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = descriptionSize * 1.45f,
                letterSpacing = (-0.1).sp,
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Pill Button ("Start Speaking" / "Listening..." / "Speaking...")
            val ctaIcon = when (voiceState) {
                VoiceState.LISTENING -> Icons.Default.Stop
                else -> Icons.Default.Mic
            }

            val ctaLabel = when {
                isThinking -> "Thinking..."
                voiceState == VoiceState.LISTENING -> "Listening... (Tap to finish)"
                voiceState == VoiceState.SPEAKING -> "Speaking... (Tap to stop)"
                else -> buttonText
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryPillButton(
                    text = ctaLabel,
                    icon = ctaIcon,
                    isLoading = isThinking,
                    onClick = onButtonClick,
                    height = 54.dp,
                    testTag = "hero_primary_cta"
                )

                if (onSecondaryClick != null && !isVoiceActive) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .height(54.dp)
                            .clip(FuturisticTokens.CornerRadius.pillShape)
                            .background(FuturisticTheme.surface)
                            .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                            .clickable(onClick = onSecondaryClick)
                            .padding(horizontal = 20.dp)
                            .testTag("hero_secondary_action"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TravelExplore,
                                contentDescription = null,
                                tint = FuturisticTheme.primaryText,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = secondaryButtonText,
                                color = FuturisticTheme.primaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Minimalist, elegant Voice Orb visual focus element.
 * Exhibits gentle, calm breathing when idle, and expands smoothly with
 * subtle concentric ripples and waveform bars during voice interaction.
 */
@Composable
fun MinimalVoiceOrb(
    voiceState: VoiceState,
    isThinking: Boolean,
    soundLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    orbDiameter: Dp = 80.dp
) {
    val isListening = voiceState == VoiceState.LISTENING
    val isSpeaking = voiceState == VoiceState.SPEAKING
    val isInteracting = isListening || isSpeaking || isThinking

    if (isListening) {
        RmsPulseIndicator(
            soundLevel = soundLevel,
            voiceState = voiceState,
            isListening = true,
            onClick = onClick,
            indicatorSize = orbDiameter * 1.5f,
            primaryColor = FuturisticTheme.accent,
            accentColor = Color(0xFF10B981),
            showRadialBars = true,
            showStatusText = false,
            modifier = modifier
        )
        return
    }

    // Continuous calm breathing transition
    val infiniteTransition = rememberInfiniteTransition(label = "orb_breathing")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_rotation"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Reactive sound scale (smoothed)
    val smoothedSound by animateFloatAsState(
        targetValue = if (isListening) soundLevel.coerceIn(0.05f, 1f) else if (isSpeaking) 0.45f else 0f,
        animationSpec = tween(durationMillis = 90),
        label = "smoothed_sound"
    )

    val orbScale = if (isInteracting) {
        1.0f + smoothedSound * 0.18f
    } else {
        idlePulse
    }

    val orbBorderColor by animateColorAsState(
        targetValue = when {
            isListening -> FuturisticTheme.accent
            isSpeaking -> Color(0xFF10B981) // Soft calm green
            isThinking -> FuturisticTheme.accent.copy(alpha = 0.8f)
            else -> FuturisticTheme.border
        },
        animationSpec = tween(300),
        label = "orb_color"
    )

    Box(
        modifier = modifier
            .size(orbDiameter * 1.5f)
            .testTag("minimal_voice_orb"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle outer concentric ripple during active voice
        Canvas(modifier = Modifier.size(orbDiameter * 1.4f)) {
            val center = Offset(orbDiameter.toPx() * 0.7f, orbDiameter.toPx() * 0.7f)
            val baseRadius = (orbDiameter.toPx() / 2f)

            if (isInteracting) {
                val ripple1 = baseRadius + (14f + smoothedSound * 24f)
                val ripple2 = baseRadius + (26f + smoothedSound * 36f)
                drawCircle(
                    color = orbBorderColor.copy(alpha = (0.12f * (1f - smoothedSound * 0.4f)).coerceIn(0.04f, 0.2f)),
                    radius = ripple1,
                    center = center
                )
                drawCircle(
                    color = orbBorderColor.copy(alpha = (0.06f * (1f - smoothedSound * 0.5f)).coerceIn(0.02f, 0.1f)),
                    radius = ripple2,
                    center = center
                )
            }
        }

        // Inner Core Orb with Mic Icon / Waveform Focus
        Box(
            modifier = Modifier
                .size(orbDiameter)
                .scale(orbScale)
                .clip(CircleShape)
                .background(FuturisticTheme.surfaceElevated)
                .border(
                    width = if (isInteracting) 2.dp else 1.dp,
                    color = orbBorderColor,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isListening || isSpeaking) {
                // Elegant 5-bar mini voice wave inside the orb
                Canvas(modifier = Modifier.size(36.dp, 24.dp)) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barCount = 5
                    val barWidth = 3.dp.toPx()
                    val spacing = (canvasWidth - (barCount * barWidth)) / (barCount - 1)
                    val midY = canvasHeight / 2f

                    for (i in 0 until barCount) {
                        val harmonic = sin(wavePhase + i * 0.8f) * 0.3f + 0.7f
                        val heightMultiplier = (smoothedSound * harmonic * (1.2f - abs(i - 2) * 0.25f)).coerceIn(0.18f, 1f)
                        val barHeight = (canvasHeight * heightMultiplier).coerceIn(4.dp.toPx(), canvasHeight)

                        val x = i * (barWidth + spacing)
                        drawRoundRect(
                            color = orbBorderColor,
                            topLeft = Offset(x, midY - barHeight / 2f),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            } else {
                // Subtle Mic Icon in calm state
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = if (isInteracting) FuturisticTheme.accent else FuturisticTheme.primaryText,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}


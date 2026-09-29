package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceState
import kotlin.math.PI
import kotlin.math.sin

/**
 * Visual Audio-Level Indicator & Listening Pulse Component.
 *
 * Provides immediate real-time feedback using audio RMS data received from
 * SpeechRecognizer's `onRmsChanged` callback, combined with a responsive
 * multi-stage pulse animation whenever the voice state is LISTENING.
 *
 * @param soundLevel Normalized audio RMS input level from `onRmsChanged` (0.0f = silence, 1.0f = peak input).
 * @param voiceState Current VoiceState of the audio engine.
 * @param rmsDb Optional raw decibel reading from `onRmsChanged` (e.g. -2dB to +10dB) for live dB readout.
 * @param isListening Explicit flag or derived from `voiceState.isRecording` or `voiceState == VoiceState.LISTENING`.
 * @param onMicClick Optional callback when user taps the central mic orb to start/stop listening.
 * @param modifier Layout modifier.
 * @param primaryColor Primary color for the pulse and audio bars (defaults to MaterialTheme primary).
 * @param accentColor Secondary accent color for gradient blends (defaults to MaterialTheme tertiary).
 * @param showWaveform Whether to display the animated real-time audio spectrum bars.
 * @param showStatusText Whether to display the contextual status label (e.g., "Listening... Speak now").
 * @param showDecibelBadge Whether to display the live RMS dB/percentage activity badge.
 */
@Composable
fun AudioLevelIndicator(
    soundLevel: Float,
    modifier: Modifier = Modifier,
    voiceState: VoiceState = VoiceState.LISTENING,
    rmsDb: Float? = null,
    isListening: Boolean = voiceState == VoiceState.LISTENING ||
            voiceState == VoiceState.LISTENING_FOR_SPEECH ||
            voiceState == VoiceState.SPEECH_DETECTED,
    onMicClick: (() -> Unit)? = null,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary,
    showWaveform: Boolean = true,
    showStatusText: Boolean = true,
    showDecibelBadge: Boolean = true
) {
    // Immediate, low-latency smoothing of the onRmsChanged audio level (60ms for instant visual responsiveness)
    val smoothedAudioLevel by animateFloatAsState(
        targetValue = if (isListening) soundLevel.coerceIn(0.04f, 1.0f) else 0.0f,
        animationSpec = tween(durationMillis = 60, easing = LinearOutSlowInEasing),
        label = "onRmsChangedSmoothedLevel"
    )

    // Dynamic color shift based on speech detection and RMS amplitude
    val liveColor by animateColorAsState(
        targetValue = when {
            !isListening -> primaryColor.copy(alpha = 0.6f)
            smoothedAudioLevel > 0.45f -> Color(0xFFEF4444) // Active vocal peak (vibrant red/coral)
            smoothedAudioLevel > 0.15f -> Color(0xFF10B981) // Speech detected (warm emerald green)
            else -> primaryColor // Calm primary listening glow
        },
        animationSpec = tween(durationMillis = 150),
        label = "liveAudioColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("audio_level_indicator"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Status text + Decibel/RMS badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showStatusText) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pulsing status dot
                        ListeningStatusDot(isListening = isListening, color = liveColor)

                        Text(
                            text = when {
                                !isListening -> "Microphone Idle"
                                smoothedAudioLevel > 0.18f -> "Listening: Voice Detected"
                                else -> "Listening... Speak now"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            ),
                            color = if (isListening) liveColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (showDecibelBadge && isListening) {
                    LiveAudioBadge(
                        smoothedLevel = smoothedAudioLevel,
                        rmsDb = rmsDb,
                        activeColor = liveColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center: Interactive Listening Pulse Orb with sound-reactive concentric rings
            ListeningPulseOrb(
                soundLevel = smoothedAudioLevel,
                isListening = isListening,
                onClick = onMicClick,
                activeColor = liveColor,
                accentColor = accentColor,
                orbDiameter = 68.dp
            )

            // Bottom: Dynamic Equalizer Spectrum Bars driven directly by onRmsChanged
            if (showWaveform) {
                Spacer(modifier = Modifier.height(14.dp))
                AudioEqualizerSpectrum(
                    soundLevel = smoothedAudioLevel,
                    isListening = isListening,
                    primaryColor = liveColor,
                    accentColor = accentColor,
                    height = 28.dp,
                    barCount = 24
                )
            }
        }
    }
}

/**
 * Center Pulsing Orb with multi-layer concentric ripple rings that animate when state is LISTENING.
 * Directly expands and surges in response to audio level from `onRmsChanged`.
 */
@Composable
fun ListeningPulseOrb(
    soundLevel: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary,
    orbDiameter: Dp = 68.dp
) {
    // Continuous infinite listening pulse transitions
    val infiniteTransition = rememberInfiniteTransition(label = "listeningPulseRings")

    // Core breathing cycle (Phase 1: 750ms rhythmic breathing)
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (isListening) 1.10f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    // Outer expanding wave 1 (Phase 2: continuous outward ripple)
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.55f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleWave1"
    )

    val rippleAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = if (isListening) 0.0f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha1"
    )

    // Outer expanding wave 2 with phase delay (Phase 3: second harmonic ripple)
    val rippleScale2 by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = if (isListening) 1.95f else 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleWave2"
    )

    val rippleAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = if (isListening) 0.0f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha2"
    )

    // Sound-reactive amplitude boost directly calculated from onRmsChanged soundLevel
    val soundReactiveScale = if (isListening) {
        (breathingScale + soundLevel * 0.28f).coerceIn(0.92f, 1.45f)
    } else {
        1.0f
    }

    Box(
        modifier = modifier
            .size(orbDiameter * 2f)
            .testTag("listening_pulse_orb"),
        contentAlignment = Alignment.Center
    ) {
        // Multi-layer concentric canvas ripples
        Canvas(modifier = Modifier.size(orbDiameter * 2f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (orbDiameter.toPx() / 2f)

            if (isListening) {
                // Outermost harmonic ripple (Phase 3)
                val r2 = baseRadius * rippleScale2 + (soundLevel * 18f)
                drawCircle(
                    color = accentColor.copy(alpha = (rippleAlpha2 * (1f + soundLevel * 0.5f)).coerceIn(0f, 0.4f)),
                    radius = r2,
                    center = center
                )

                // Secondary expanding wave (Phase 2)
                val r1 = baseRadius * rippleScale1 + (soundLevel * 24f)
                drawCircle(
                    color = activeColor.copy(alpha = (rippleAlpha1 * (1f + soundLevel * 0.6f)).coerceIn(0f, 0.55f)),
                    radius = r1,
                    center = center
                )

                // Sound-reactive vocal aura halo right outside the core orb
                val auraRadius = (baseRadius * soundReactiveScale) + 6f + (soundLevel * 16f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeColor.copy(alpha = (0.28f + soundLevel * 0.45f).coerceIn(0.1f, 0.75f)),
                            Color.Transparent
                        ),
                        center = center,
                        radius = auraRadius
                    ),
                    radius = auraRadius,
                    center = center
                )
            }
        }

        // Central tactile orb button
        Box(
            modifier = Modifier
                .size(orbDiameter)
                .scale(soundReactiveScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isListening) {
                            listOf(activeColor, activeColor.copy(alpha = 0.85f))
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    )
                )
                .border(
                    width = if (isListening) 2.5.dp else 1.dp,
                    color = if (isListening) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
                .clickable(
                    enabled = onClick != null,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = orbDiameter / 2),
                    role = Role.Button,
                    onClick = { onClick?.invoke() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop listening" else "Start listening",
                tint = if (isListening) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(orbDiameter * 0.44f)
            )
        }
    }
}

/**
 * Animated real-time equalizer spectrum bars driven by onRmsChanged audio level.
 */
@Composable
fun AudioEqualizerSpectrum(
    soundLevel: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary,
    height: Dp = 26.dp,
    barCount: Int = 24
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrumHarmonics")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spectrumPhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("audio_equalizer_spectrum")
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val totalSpacingRatio = 0.35f
        val barWidth = (totalWidth * (1f - totalSpacingRatio)) / barCount.toFloat()
        val spacing = (totalWidth * totalSpacingRatio) / (barCount - 1).coerceAtLeast(1).toFloat()

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / (barCount - 1).coerceAtLeast(1).toFloat()
            // Natural bell curve: central frequencies respond more strongly to human vocal range
            val bellWeight = sin(normalizedIndex * PI).toFloat().coerceIn(0.18f, 1.0f)
            // Harmonic wave offset creating organic breathing movement
            val harmonic = sin(phase + i * 0.42f).toFloat() * 0.18f + 0.82f

            val computedHeight = if (isListening) {
                val vocalAmplitude = (soundLevel * bellWeight * harmonic).coerceIn(0.06f, 1.0f)
                (canvasHeight * vocalAmplitude).coerceIn(4f, canvasHeight)
            } else {
                3f
            }

            val x = i * (barWidth + spacing)
            val y = (canvasHeight - computedHeight) / 2f

            // Vertical gradient from primary to accent
            val barBrush = Brush.verticalGradient(
                colors = listOf(primaryColor, accentColor),
                startY = y,
                endY = y + computedHeight
            )

            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, computedHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                alpha = if (isListening) (0.45f + soundLevel * 0.55f).coerceIn(0.4f, 1.0f) else 0.25f
            )
        }
    }
}

/**
 * Compact Inline Audio-Level Indicator Bar.
 * Ideal for composer bars or message bubble footers.
 */
@Composable
fun CompactAudioLevelIndicator(
    soundLevel: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 16.dp,
    barCount: Int = 8
) {
    val smoothedLevel by animateFloatAsState(
        targetValue = if (isListening) soundLevel.coerceIn(0.08f, 1.0f) else 0.0f,
        animationSpec = tween(durationMillis = 60, easing = LinearOutSlowInEasing),
        label = "compactSmoothedLevel"
    )

    val transition = rememberInfiniteTransition(label = "compactBarAnim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "compactPhase"
    )

    Canvas(
        modifier = modifier
            .width(36.dp)
            .height(height)
            .testTag("compact_audio_level_indicator")
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = totalWidth / (barCount * 1.5f)
        val spacing = barWidth * 0.5f

        for (i in 0 until barCount) {
            val harmonic = sin(phase + i * 0.8f).toFloat() * 0.25f + 0.75f
            val barH = if (isListening) {
                (canvasHeight * smoothedLevel * harmonic).coerceIn(3f, canvasHeight)
            } else {
                2.5f
            }

            val x = i * (barWidth + spacing)
            val y = (canvasHeight - barH) / 2f

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                alpha = if (isListening) 0.95f else 0.35f
            )
        }
    }
}

/**
 * Live audio activity badge showing normalized audio percentage or raw decibel (dB) from onRmsChanged.
 */
@Composable
private fun LiveAudioBadge(
    smoothedLevel: Float,
    rmsDb: Float?,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val text = if (rmsDb != null && rmsDb > -50f) {
        "${rmsDb.toInt()} dB"
    } else {
        "${(smoothedLevel * 100).toInt()}%"
    }

    Surface(
        modifier = modifier.testTag("live_audio_badge"),
        shape = RoundedCornerShape(8.dp),
        color = activeColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = activeColor
            )
        }
    }
}

/**
 * Pulsing status dot indicator.
 */
@Composable
private fun ListeningStatusDot(
    isListening: Boolean,
    color: Color
) {
    val transition = rememberInfiniteTransition(label = "statusDotPulse")
    val dotScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(dotScale)
            .clip(CircleShape)
            .background(color)
    )
}

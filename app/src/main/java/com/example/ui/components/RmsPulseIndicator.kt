package com.example.ui.components

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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Composable that uses audio levels provided by SpeechRecognizer's [onRmsChanged]
 * to animate a visual pulse indicator while the voice state is [VoiceState.LISTENING].
 *
 * Provides instant, zero-latency visual feedback through:
 * 1. Multi-phase concentric ripple waves that expand continuously while in LISTENING state.
 * 2. Sound-reactive amplitude amplification that stretches the pulse radius and glowing aura
 *    in direct proportion to the live RMS level from [onRmsChanged].
 * 3. 24 radial audio-level equalizer bars that radiate outward around the mic core.
 * 4. Responsive center orb scaling and color transitions (calm primary -> vibrant speech active).
 *
 * @param soundLevel Normalized audio level (0.0f to 1.0f) derived from [onRmsChanged].
 * @param voiceState Current [VoiceState] of the voice pipeline.
 * @param modifier Layout modifier.
 * @param rmsDb Optional raw decibel reading from [onRmsChanged] for display.
 * @param isListening Explicit flag (defaults to true when voiceState is LISTENING or recording).
 * @param onClick Optional click handler to toggle microphone listening.
 * @param size Outer dimension for the indicator component.
 * @param primaryColor Main color for pulse waves (defaults to MaterialTheme primary).
 * @param accentColor Accent color for vocal spikes and outer harmonics.
 * @param showRadialBars Whether to draw 24 radial frequency spokes driven by RMS amplitude.
 * @param showStatusText Whether to display the "Listening..." feedback label below the indicator.
 */
@Composable
fun RmsPulseIndicator(
    soundLevel: Float,
    modifier: Modifier = Modifier,
    voiceState: VoiceState = VoiceState.LISTENING,
    rmsDb: Float? = null,
    isListening: Boolean = voiceState == VoiceState.LISTENING ||
            voiceState == VoiceState.LISTENING_FOR_SPEECH ||
            voiceState == VoiceState.SPEECH_DETECTED,
    onClick: (() -> Unit)? = null,
    indicatorSize: Dp = 110.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary,
    showRadialBars: Boolean = true,
    showStatusText: Boolean = false
) {
    // Ultra-low latency smoothing of the onRmsChanged audio level (50ms tween ensures instant responsiveness)
    val smoothedRms by animateFloatAsState(
        targetValue = if (isListening) soundLevel.coerceIn(0.04f, 1.0f) else 0.0f,
        animationSpec = tween(durationMillis = 50, easing = LinearOutSlowInEasing),
        label = "onRmsChangedSmoothed"
    )

    // Infinite pulse animations active during LISTENING state
    val infiniteTransition = rememberInfiniteTransition(label = "rmsPulseInfinite")

    // Phase 1: Core rhythmic breathing pulse (700ms)
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = if (isListening) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    // Phase 2: Concentric expanding wave 1 (1200ms)
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.62f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveScale1"
    )

    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = if (isListening) 0.0f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha1"
    )

    // Phase 3: Concentric expanding wave 2 with harmonic offset (1550ms)
    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = if (isListening) 2.05f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1550, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveScale2"
    )

    val waveAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isListening) 0.0f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1550, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha2"
    )

    // Phase 4: Radial spoke harmonic rotation for living organic movement
    val rotationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationPhase"
    )

    // Dynamic color transition based on voice amplitude: calm listening -> active speech
    val activeColor by animateColorAsState(
        targetValue = when {
            !isListening -> primaryColor.copy(alpha = 0.7f)
            smoothedRms > 0.40f -> Color(0xFFEF4444) // Vocal peak (coral red)
            smoothedRms > 0.15f -> Color(0xFF10B981) // Speech detected (warm emerald green)
            else -> primaryColor // Calm primary listening aura
        },
        animationSpec = tween(durationMillis = 120),
        label = "activePulseColor"
    )

    // Core mic orb diameter is roughly 52% of total component size
    val coreDiameter = indicatorSize * 0.52f
    // Sound-reactive amplitude multiplier: directly expands the core orb on vocal sound
    val soundReactiveCoreScale = if (isListening) {
        (breathingPulse + smoothedRms * 0.32f).coerceIn(0.92f, 1.45f)
    } else {
        1.0f
    }

    Column(
        modifier = modifier.testTag("rms_pulse_indicator"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(indicatorSize),
            contentAlignment = Alignment.Center
        ) {
            // Custom Canvas: concentric ripple rings & 24 radial audio-level bars
            Canvas(modifier = Modifier.size(indicatorSize)) {
                val canvasCenter = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = coreDiameter.toPx() / 2f

                if (isListening) {
                    // 1. Outermost harmonic ripple ring
                    val r2 = (baseRadius * waveScale2) + (smoothedRms * 20f)
                    drawCircle(
                        color = accentColor.copy(
                            alpha = (waveAlpha2 * (1f + smoothedRms * 0.6f)).coerceIn(0f, 0.45f)
                        ),
                        radius = r2,
                        center = canvasCenter
                    )

                    // 2. Secondary expanding ripple ring
                    val r1 = (baseRadius * waveScale1) + (smoothedRms * 26f)
                    drawCircle(
                        color = activeColor.copy(
                            alpha = (waveAlpha1 * (1f + smoothedRms * 0.7f)).coerceIn(0f, 0.60f)
                        ),
                        radius = r1,
                        center = canvasCenter
                    )

                    // 3. Glowing vocal aura halo hugging the central orb
                    val auraRadius = (baseRadius * soundReactiveCoreScale) + 8f + (smoothedRms * 18f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeColor.copy(alpha = (0.35f + smoothedRms * 0.45f).coerceIn(0.1f, 0.8f)),
                                Color.Transparent
                            ),
                            center = canvasCenter,
                            radius = auraRadius
                        ),
                        radius = auraRadius,
                        center = canvasCenter
                    )

                    // 4. 24 Radial Audio-Level Equalizer Spokes driven by onRmsChanged
                    if (showRadialBars) {
                        val spokeCount = 24
                        val innerSpokeRadius = (baseRadius * soundReactiveCoreScale) + 4f
                        val maxSpokeLength = (this.size.width / 2f) - innerSpokeRadius - 2f

                        for (i in 0 until spokeCount) {
                            val angleRad = (i.toFloat() / spokeCount.toFloat()) * 2f * PI.toFloat() + rotationPhase
                            // Natural vocal frequency variation
                            val frequencyWeight = (sin(angleRad * 3f) * 0.25f + 0.75f).coerceIn(0.5f, 1.0f)
                            val spokeLength = (maxSpokeLength * smoothedRms * frequencyWeight).coerceIn(2f, maxSpokeLength)
                            val outerSpokeRadius = innerSpokeRadius + spokeLength

                            val startX = canvasCenter.x + cos(angleRad) * innerSpokeRadius
                            val startY = canvasCenter.y + sin(angleRad) * innerSpokeRadius
                            val endX = canvasCenter.x + cos(angleRad) * outerSpokeRadius
                            val endY = canvasCenter.y + sin(angleRad) * outerSpokeRadius

                            drawLine(
                                color = if (i % 2 == 0) activeColor else accentColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                alpha = (0.50f + smoothedRms * 0.50f).coerceIn(0.3f, 1.0f)
                            )
                        }
                    }
                }
            }

            // Central Tactile Mic Button Orb
            Box(
                modifier = Modifier
                    .size(coreDiameter)
                    .scale(soundReactiveCoreScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isListening) {
                                listOf(activeColor, activeColor.copy(alpha = 0.88f))
                            } else {
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                                )
                            }
                        )
                    )
                    .border(
                        width = if (isListening) 2.5.dp else 1.dp,
                        color = if (isListening) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable(
                        enabled = onClick != null,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = coreDiameter / 2),
                        role = Role.Button,
                        onClick = { onClick?.invoke() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Start listening",
                    tint = if (isListening) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(coreDiameter * 0.44f)
                )
            }
        }

        // Optional Live Status & dB Badge below the pulse indicator
        if (showStatusText) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = activeColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = when {
                        !isListening -> "Mic Idle"
                        rmsDb != null && rmsDb > -50f -> "Listening • ${rmsDb.toInt()} dB"
                        else -> "Listening • ${(smoothedRms * 100).toInt()}%"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = activeColor
                )
            }
        }
    }
}

/**
 * Convenient alias matching the descriptive name AudioLevelPulseIndicator.
 */
@Composable
fun AudioLevelPulseIndicator(
    soundLevel: Float,
    modifier: Modifier = Modifier,
    voiceState: VoiceState = VoiceState.LISTENING,
    rmsDb: Float? = null,
    isListening: Boolean = voiceState == VoiceState.LISTENING ||
            voiceState == VoiceState.LISTENING_FOR_SPEECH ||
            voiceState == VoiceState.SPEECH_DETECTED,
    onClick: (() -> Unit)? = null,
    indicatorSize: Dp = 110.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary,
    showRadialBars: Boolean = true,
    showStatusText: Boolean = false
) {
    RmsPulseIndicator(
        soundLevel = soundLevel,
        modifier = modifier,
        voiceState = voiceState,
        rmsDb = rmsDb,
        isListening = isListening,
        onClick = onClick,
        indicatorSize = indicatorSize,
        primaryColor = primaryColor,
        accentColor = accentColor,
        showRadialBars = showRadialBars,
        showStatusText = showStatusText
    )
}

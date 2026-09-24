package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.sin

/**
 * Visual presentation style for the sound wave.
 */
enum class SoundWaveStyle {
    BARS,        // Modern multi-bar frequency spectrum equalizer
    FLUID_WAVE,  // Continuous smooth bezier wave with glowing gradient fill
    DUAL         // Allows toggling between Bars and Fluid Wave
}

/**
 * Sound-wave visualization component that animates in real-time based on the
 * audio input level during voice recording.
 *
 * @param soundLevel Normalized audio input level from microphone (0.0f = silence, 1.0f = peak volume).
 * @param isRecording Whether the microphone is actively recording/listening.
 * @param modifier Layout modifier.
 * @param waveStyle Visual style (BARS or FLUID_WAVE).
 * @param barCount Number of animated frequency bars in BARS mode.
 * @param primaryColor Primary waveform color.
 * @param secondaryColor Secondary/accent waveform color for gradient blending.
 * @param height Height of the soundwave canvas.
 * @param showLevelBadge Whether to show a subtle "LIVE INPUT" / decibel activity badge.
 */
@Composable
fun SoundWaveVisualizer(
    soundLevel: Float,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    waveStyle: SoundWaveStyle = SoundWaveStyle.BARS,
    barCount: Int = 28,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    height: Dp = 64.dp,
    showLevelBadge: Boolean = true
) {
    var activeStyle by remember(waveStyle) {
        mutableStateOf(if (waveStyle == SoundWaveStyle.DUAL) SoundWaveStyle.BARS else waveStyle)
    }

    // Smooth real-time audio input level transitions using responsive spring/tween
    val smoothedLevel by animateFloatAsState(
        targetValue = if (isRecording) soundLevel.coerceIn(0.05f, 1.0f) else 0.0f,
        animationSpec = tween(durationMillis = 75, easing = LinearOutSlowInEasing),
        label = "smoothedAudioLevel"
    )

    // Continuous organic wave phase animation for breathing dynamics
    val infiniteTransition = rememberInfiniteTransition(label = "wavePhase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuousPhase"
    )

    // Subtle breathing pulse for idle / resting state
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    val currentLevel = if (isRecording) smoothedLevel else (idlePulse * 0.12f)
    val percentage = (currentLevel * 100).toInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sound_wave_visualizer")
            .semantics {
                contentDescription = if (isRecording) {
                    "Live sound wave visualizer, input level $percentage percent"
                } else {
                    "Sound wave visualizer idle"
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Optional status / live meter badge
        if (showLevelBadge) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording) {
                                    if (currentLevel > 0.4f) MaterialTheme.colorScheme.error else primaryColor
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                    )
                    Text(
                        text = if (isRecording) "LIVE AUDIO INPUT" else "AUDIO STANDBY",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRecording) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (waveStyle == SoundWaveStyle.DUAL) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(2.dp)
                    ) {
                        StyleToggleChip(
                            text = "Bars",
                            isSelected = activeStyle == SoundWaveStyle.BARS,
                            onClick = { activeStyle = SoundWaveStyle.BARS }
                        )
                        StyleToggleChip(
                            text = "Fluid",
                            isSelected = activeStyle == SoundWaveStyle.FLUID_WAVE,
                            onClick = { activeStyle = SoundWaveStyle.FLUID_WAVE }
                        )
                    }
                } else if (isRecording) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Main Waveform Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            when (activeStyle) {
                SoundWaveStyle.BARS -> {
                    LiveBarWaveform(
                        soundLevel = currentLevel,
                        phase = phase,
                        barCount = barCount,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        isRecording = isRecording
                    )
                }
                SoundWaveStyle.FLUID_WAVE, SoundWaveStyle.DUAL -> {
                    FluidCanvasWaveform(
                        soundLevel = currentLevel,
                        phase = phase,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        isRecording = isRecording
                    )
                }
            }
        }
    }
}

/**
 * Modern multi-bar frequency spectrum equalizer.
 * Bars scale symmetrically and organically from center to edges,
 * reflecting vocal pitch and sound pressure in real time.
 */
@Composable
fun LiveBarWaveform(
    soundLevel: Float,
    phase: Float,
    barCount: Int = 28,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    isRecording: Boolean = true,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("sound_wave_canvas")
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val midIndex = (barCount - 1) / 2f

        val spacing = width / barCount.toFloat()
        val barWidth = (spacing * 0.62f).coerceIn(2.5f, 10f)

        val gradient = Brush.verticalGradient(
            colors = listOf(
                secondaryColor,
                primaryColor,
                secondaryColor
            )
        )

        for (i in 0 until barCount) {
            // Normalized distance from center (0 at center, 1 at edges)
            val centerDist = abs(i - midIndex) / midIndex

            // Bell-curve sensitivity weighting: Center bars are more reactive than edges
            val bellWeight = (1.0f - (centerDist * centerDist * 0.7f)).coerceIn(0.25f, 1.0f)

            // Harmonic dynamic flutter based on continuous phase
            val harmonic = sin(phase + (i * 0.35f)) * 0.18f + 0.82f

            // Baseline resting height vs active volume height
            val minBarHeight = 4f
            val maxBarHeight = height * 0.92f

            val amplitude = if (isRecording) {
                val scaledLevel = (soundLevel * bellWeight * harmonic).coerceIn(0.04f, 1.0f)
                minBarHeight + (maxBarHeight - minBarHeight) * scaledLevel
            } else {
                // Gentle idle wave
                minBarHeight + (sin(phase + i * 0.3f) * 2f).coerceAtLeast(0f)
            }

            val x = (i * spacing) + (spacing - barWidth) / 2f
            val top = (midY - amplitude / 2f).coerceAtLeast(0f)

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, top),
                size = Size(barWidth, amplitude),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                alpha = if (isRecording) (0.6f + soundLevel * 0.4f).coerceIn(0.4f, 1.0f) else 0.45f
            )
        }
    }
}

/**
 * Continuous fluid bezier curve wave rendered on Canvas with glowing gradient fill.
 */
@Composable
fun FluidCanvasWaveform(
    soundLevel: Float,
    phase: Float,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    isRecording: Boolean = true,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("sound_wave_fluid_canvas")
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        val baseAmplitude = if (isRecording) {
            (soundLevel * (height * 0.42f)).coerceIn(3f, height * 0.45f)
        } else {
            3f
        }

        // Draw Layer 1: Background harmonic wave (semi-transparent)
        val pathBack = Path()
        pathBack.moveTo(0f, midY)
        val points = 32
        for (i in 0..points) {
            val x = (i / points.toFloat()) * width
            val normX = i / points.toFloat()
            val window = sin(normX * Math.PI.toFloat()) // Smooth envelope at edges
            val amp = baseAmplitude * 0.65f * window
            val y = midY + sin(phase * 1.3f + normX * 4 * Math.PI.toFloat()) * amp
            if (i == 0) pathBack.moveTo(x, y) else pathBack.lineTo(x, y)
        }

        drawPath(
            path = pathBack,
            color = secondaryColor.copy(alpha = 0.4f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Layer 2: Main primary foreground wave with filled glow
        val pathFront = Path()
        val fillPath = Path()
        fillPath.moveTo(0f, height)

        for (i in 0..points) {
            val x = (i / points.toFloat()) * width
            val normX = i / points.toFloat()
            val window = sin(normX * Math.PI.toFloat()) // Smooth edge tapering
            val amp = baseAmplitude * window
            val y = midY + sin(phase + normX * 3 * Math.PI.toFloat()) * amp

            if (i == 0) {
                pathFront.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                pathFront.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(width, height)
        fillPath.close()

        // Gradient liquid fill below wave
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (isRecording) 0.35f else 0.12f),
                    primaryColor.copy(alpha = 0.02f)
                ),
                startY = midY,
                endY = height
            )
        )

        // Wave crest line
        drawPath(
            path = pathFront,
            brush = Brush.horizontalGradient(
                colors = listOf(primaryColor, secondaryColor, primaryColor)
            ),
            style = Stroke(
                width = if (isRecording) 3.dp.toPx() else 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Center line glow
        drawLine(
            color = primaryColor.copy(alpha = 0.2f),
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

/**
 * Compact horizontal sound-wave bar for embedding into chat input bars,
 * active recording pills, and live transcript bubbles.
 */
@Composable
fun CompactVoiceWaveBar(
    soundLevel: Float,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 22.dp
) {
    val smoothedLevel by animateFloatAsState(
        targetValue = if (isRecording) soundLevel.coerceIn(0.08f, 1.0f) else 0f,
        animationSpec = tween(durationMillis = 60, easing = LinearOutSlowInEasing),
        label = "compactSmoothedLevel"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "compactWave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "compactPhase"
    )

    Canvas(
        modifier = modifier
            .height(height)
            .testTag("compact_voice_wave_bar")
    ) {
        val width = size.width
        val canvasHeight = size.height
        val midY = canvasHeight / 2f
        val midIndex = (barCount - 1) / 2f
        val spacing = width / barCount.toFloat()
        val barWidth = (spacing * 0.55f).coerceIn(2f, 6f)

        for (i in 0 until barCount) {
            val centerDist = abs(i - midIndex) / midIndex
            val bellWeight = (1.0f - centerDist * 0.55f).coerceIn(0.3f, 1.0f)
            val harmonic = sin(phase + i * 0.4f) * 0.2f + 0.8f

            val minH = 3f
            val maxH = canvasHeight * 0.95f
            val amplitude = if (isRecording) {
                minH + (maxH - minH) * (smoothedLevel * bellWeight * harmonic).coerceIn(0.05f, 1.0f)
            } else {
                minH
            }

            val x = (i * spacing) + (spacing - barWidth) / 2f
            val top = (midY - amplitude / 2f).coerceAtLeast(0f)

            drawRoundRect(
                color = color,
                topLeft = Offset(x, top),
                size = Size(barWidth, amplitude),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                alpha = if (isRecording) 0.9f else 0.4f
            )
        }
    }
}

@Composable
private fun StyleToggleChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

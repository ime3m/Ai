package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.VoiceState
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.VioletAccent

@Composable
fun VoiceVisualizer(
    voiceState: VoiceState,
    soundLevel: Float,
    isMuted: Boolean,
    onMicClick: () -> Unit,
    onInterruptClick: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulseTransition")

    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (voiceState == VoiceState.LISTENING) 1.25f else if (voiceState == VoiceState.SPEAKING) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Voice Status Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = when (voiceState) {
                VoiceState.LISTENING -> CyanPrimary.copy(alpha = 0.2f)
                VoiceState.PROCESSING -> VioletAccent.copy(alpha = 0.2f)
                VoiceState.SPEAKING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                VoiceState.IDLE -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (voiceState) {
                                VoiceState.LISTENING -> CyanLight
                                VoiceState.PROCESSING -> VioletAccent
                                VoiceState.SPEAKING -> MaterialTheme.colorScheme.tertiary
                                VoiceState.IDLE -> MaterialTheme.colorScheme.outline
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (voiceState) {
                        VoiceState.LISTENING -> "Listening to your speech..."
                        VoiceState.PROCESSING -> "Thinking in regional dialect..."
                        VoiceState.SPEAKING -> "Speaking (Tap Stop to interrupt)"
                        VoiceState.IDLE -> "Tap mic to talk naturally"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Animated Soundbars when speaking or listening
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(44.dp)
            ) {
                val barCount = 19
                val barWidth = size.width / (barCount * 1.8f)
                val spacing = barWidth * 0.8f
                val totalWidth = (barCount * barWidth) + ((barCount - 1) * spacing)
                var startX = (size.width - totalWidth) / 2f

                for (i in 0 until barCount) {
                    val normalizedDist = 1f - (kotlin.math.abs(i - barCount / 2f) / (barCount / 2f))
                    val heightFactor = when (voiceState) {
                        VoiceState.LISTENING -> {
                            val wave = kotlin.math.sin(wavePhase + i * 0.4f) * 0.5f + 0.5f
                            (0.2f + (soundLevel * 0.8f * wave * normalizedDist)).coerceIn(0.15f, 1f)
                        }
                        VoiceState.SPEAKING -> {
                            val wave = kotlin.math.sin(wavePhase * 2f + i * 0.5f) * 0.5f + 0.5f
                            (0.25f + 0.75f * wave * normalizedDist).coerceIn(0.2f, 1f)
                        }
                        VoiceState.PROCESSING -> {
                            val wave = kotlin.math.sin(wavePhase * 3f + i * 0.3f) * 0.5f + 0.5f
                            0.2f + 0.3f * wave
                        }
                        VoiceState.IDLE -> 0.12f
                    }

                    val barHeight = (size.height * heightFactor).coerceAtLeast(4.dp.toPx())
                    val topY = (size.height - barHeight) / 2f

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(CyanLight, VioletAccent),
                            startY = topY,
                            endY = topY + barHeight
                        ),
                        topLeft = Offset(startX, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                    startX += barWidth + spacing
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Voice Controller: Interrupt, Main Pulsing Mic, Mute
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stop / Interrupt Button
            FilledTonalIconButton(
                onClick = onInterruptClick,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("interrupt_voice_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (voiceState == VoiceState.SPEAKING) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Interrupt AI speaking",
                    tint = if (voiceState == VoiceState.SPEAKING) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Large Animated Central Microphone Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                // Outer Pulse Ring
                if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                if (voiceState == VoiceState.LISTENING) CyanLight.copy(alpha = 0.35f)
                                else VioletAccent.copy(alpha = 0.25f)
                            )
                    )
                }

                // Inner Main Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(
                            elevation = if (voiceState == VoiceState.LISTENING) 16.dp else 8.dp,
                            shape = CircleShape,
                            ambientColor = CyanPrimary,
                            spotColor = CyanLight
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = when (voiceState) {
                                    VoiceState.LISTENING -> listOf(CyanLight, CyanPrimary)
                                    VoiceState.SPEAKING -> listOf(VioletAccent, Color(0xFF6D28D9))
                                    VoiceState.PROCESSING -> listOf(VioletAccent, CyanPrimary)
                                    VoiceState.IDLE -> listOf(CyanPrimary, Color(0xFF0369A1))
                                }
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onMicClick
                        )
                        .testTag("voice_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice microphone toggle",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Mute / Speaker Toggle Button
            FilledTonalIconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("mute_voice_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isMuted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute voice responses" else "Mute voice responses",
                    tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceState
import com.example.ui.theme.AppTheme

/**
 * Voice-Reactive Visualizer Component.
 * Dynamically reacts to VoiceState (IDLE, LISTENING, PROCESSING, SPEAKING)
 * with lightweight, accessible, hardware-friendly animations consuming AppTheme tokens.
 */
@Composable
fun VoiceVisualizer(
    voiceState: VoiceState,
    soundLevel: Float,
    isMuted: Boolean,
    onMicClick: () -> Unit,
    onInterruptClick: () -> Unit,
    onToggleMute: () -> Unit,
    onReplayLast: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isListening = voiceState == VoiceState.LISTENING
    val isSpeaking = voiceState == VoiceState.SPEAKING
    val isProcessing = voiceState == VoiceState.PROCESSING
    val isIdle = voiceState == VoiceState.IDLE

    // Lightweight animations running ONLY when active to preserve CPU and battery
    val listeningTransition = rememberInfiniteTransition(label = "listeningPulse")
    val pulseScale by listeningTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.18f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listeningPulseScale"
    )

    val listeningAlpha by listeningTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (isListening) 0.1f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listeningRingAlpha"
    )

    // Speaking glow animation
    val speakingTransition = rememberInfiniteTransition(label = "speakingGlow")
    val speakingGlowAlpha by speakingTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isSpeaking) 0.70f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakingGlowAlpha"
    )

    // Processing rotation animation
    val processingTransition = rememberInfiniteTransition(label = "processingSpin")
    val processingRotation by processingTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isProcessing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "processingRotation"
    )

    // Speaking dynamic soundLevel adjustment so waveform moves naturally when speaking
    val speakingWaveFactor by speakingTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isSpeaking) 0.75f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakingWaveLevel"
    )
    val effectiveSoundLevel = when {
        isListening -> soundLevel
        isSpeaking -> if (soundLevel > 0.1f) soundLevel else speakingWaveFactor
        else -> 0.0f
    }

    val micTokens = when {
        isListening -> AppTheme.voice.listening
        isProcessing -> AppTheme.voice.processing
        isSpeaking -> AppTheme.voice.speaking
        else -> AppTheme.voice.idle
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status indicator row with animated activity badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            when {
                isListening -> {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.status.error)
                    )
                    Text(
                        text = "Listening...",
                        style = AppTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.status.error
                    )
                }
                isProcessing -> {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AppTheme.colors.accent.primary,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(processingRotation)
                    )
                    Text(
                        text = "AI Thinking...",
                        style = AppTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.accent.primary
                    )
                }
                isSpeaking -> {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.accent.primary)
                    )
                    Text(
                        text = "Speaking...",
                        style = AppTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.accent.primary
                    )
                }
                else -> {
                    Text(
                        text = "Tap microphone to speak",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.muted
                    )
                }
            }
        }

        // Sound-wave visualization component that animates in real-time
        val wavePrimary = when {
            isListening -> AppTheme.voice.waveform.listening
            isProcessing -> AppTheme.voice.waveform.processing
            isSpeaking -> AppTheme.voice.waveform.speaking
            else -> AppTheme.voice.waveform.idle
        }

        SoundWaveVisualizer(
            soundLevel = effectiveSoundLevel,
            isRecording = isListening || isSpeaking || isProcessing,
            primaryColor = wavePrimary,
            secondaryColor = AppTheme.colors.accent.secondary,
            waveStyle = SoundWaveStyle.DUAL,
            height = 46.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Large simple microphone button with state-specific reactions
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(88.dp)
        ) {
            // 1. Listening outer pulsing ring
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(micTokens.ring.copy(alpha = listeningAlpha))
                )
            }

            // 2. Speaking gentle ambient glow
            if (isSpeaking) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(micTokens.glow.copy(alpha = speakingGlowAlpha))
                )
            }

            // 3. Processing rotating arc spinner
            if (isProcessing) {
                Canvas(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(processingRotation)
                ) {
                    drawArc(
                        color = micTokens.ring,
                        startAngle = 0f,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = micTokens.ring.copy(alpha = 0.3f),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Primary interactive button
            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onMicClick)
                    .testTag("mic_toggle_button")
                    .border(
                        width = if (isListening || isSpeaking || isProcessing) 2.dp else 1.dp,
                        color = micTokens.ring,
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = micTokens.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        isListening -> {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop listening",
                                tint = micTokens.foreground,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        isProcessing -> {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI is thinking",
                                tint = micTokens.foreground,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Start talking",
                                tint = micTokens.foreground,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Minimal controls row: Stop, Replay, Interrupt, Mute
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interrupt / Pause
            if (isSpeaking) {
                IconButton(
                    onClick = onInterruptClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("interrupt_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Interrupt speaking",
                        tint = AppTheme.colors.accent.primary
                    )
                }
            }

            // Replay Last Message
            if (!isListening && onReplayLast != null) {
                IconButton(
                    onClick = onReplayLast,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("replay_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay last audio",
                        tint = AppTheme.colors.text.secondary
                    )
                }
            }

            // Mute / Unmute Toggle
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("mute_toggle_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = if (isMuted) "Unmute audio" else "Mute audio",
                    tint = if (isMuted) AppTheme.colors.status.error else AppTheme.colors.text.secondary
                )
            }
        }
    }
}

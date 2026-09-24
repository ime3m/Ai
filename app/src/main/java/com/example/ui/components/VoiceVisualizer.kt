package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceState

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

    val transition = rememberInfiniteTransition(label = "subtleBreath")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status indicator
        val statusText = when {
            isListening -> "Listening..."
            isProcessing -> "Thinking..."
            isSpeaking -> "Speaking..."
            else -> "Tap to speak"
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isListening || isSpeaking) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sound-wave visualization component that animates in real-time
        SoundWaveVisualizer(
            soundLevel = soundLevel,
            isRecording = isListening || isSpeaking,
            waveStyle = SoundWaveStyle.DUAL,
            height = 48.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Large simple microphone button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            // Subtle breathing outline when listening
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                )
            }

            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onMicClick)
                    .testTag("mic_toggle_button")
                    .border(
                        width = 1.dp,
                        color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop listening" else "Start talking",
                        tint = if (isListening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                        contentDescription = "Interrupt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Stop
            if (isListening || isSpeaking) {
                IconButton(
                    onClick = {
                        if (isListening) onMicClick() else onInterruptClick()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("stop_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Replay
            if (onReplayLast != null) {
                IconButton(
                    onClick = onReplayLast,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("replay_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Replay",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Mute / Unmute
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("mute_voice_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

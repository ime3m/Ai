package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoicePipelineLogger
import com.example.ui.VoiceViewModel

/**
 * Developer Diagnostic Screen (Requirements 129, 133, 134, 135).
 * Developer-only tool to inspect microphone access, audio frames, dB levels, VAD, and STT pipeline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceDiagnosticSheet(
    sheetState: SheetState,
    viewModel: VoiceViewModel,
    onDismiss: () -> Unit
) {
    val diagnostics by VoicePipelineLogger.liveDiagnostics.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val currentDialect by viewModel.currentDialect.collectAsState()

    var isRunningMicTest by remember { mutableStateOf(false) }
    var micTestResult by remember { mutableStateOf<String?>(null) }
    var micTestSuccess by remember { mutableStateOf<Boolean?>(null) }
    var testLiveFrames by remember { mutableStateOf(0L) }
    var testLiveDb by remember { mutableStateOf(-96f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("voice_diagnostic_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Audio Pipeline Diagnostics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Developer-only live inspection & hardware test suite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // SECTION 1: Real-Time Audio Status (Requirement 129)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "MICROPHONE PIPELINE STATUS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            DiagnosticRow(
                                label = "Session ID",
                                value = diagnostics.sessionId.ifBlank { "N/A" },
                                isHighlight = false
                            )

                            DiagnosticRow(
                                label = "State Machine",
                                value = voiceState.name,
                                isHighlight = true,
                                color = if (voiceState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )

                            DiagnosticRow(
                                label = "Permission",
                                value = if (diagnostics.permissionGranted) "GRANTED" else "CHECKING / REQUIRED",
                                isHighlight = true,
                                color = if (diagnostics.permissionGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )

                            DiagnosticRow(
                                label = "Audio Capture",
                                value = if (diagnostics.audioCaptureRunning) "RUNNING" else "IDLE",
                                isHighlight = true,
                                color = if (diagnostics.audioCaptureRunning) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            DiagnosticRow(
                                label = "Audio Frames Read",
                                value = "%,d frames".format(diagnostics.audioFrames),
                                isHighlight = false
                            )

                            DiagnosticRow(
                                label = "Non-zero Audio Samples",
                                value = "%,d samples".format(diagnostics.nonZeroSamples),
                                isHighlight = false
                            )

                            DiagnosticRow(
                                label = "Input Level (dB)",
                                value = "%.1f dB".format(diagnostics.inputLevelDb),
                                isHighlight = false
                            )

                            LinearProgressIndicator(
                                progress = { soundLevel.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            DiagnosticRow(
                                label = "Speech Detected",
                                value = if (diagnostics.speechDetected) "YES" else "NO",
                                isHighlight = true,
                                color = if (diagnostics.speechDetected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                            )

                            DiagnosticRow(
                                label = "STT Connection",
                                value = diagnostics.sttConnection,
                                isHighlight = true
                            )

                            if (diagnostics.partialTranscript.isNotBlank()) {
                                DiagnosticRow(
                                    label = "Partial Transcript",
                                    value = "\"${diagnostics.partialTranscript}\"",
                                    isHighlight = false
                                )
                            }

                            if (diagnostics.finalTranscript.isNotBlank()) {
                                DiagnosticRow(
                                    label = "Final Transcript",
                                    value = "\"${diagnostics.finalTranscript}\"",
                                    isHighlight = true,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // SECTION 2: Independent Hardware Tests (Requirements 133, 134, 135)
                item {
                    Text(
                        text = "INDEPENDENT HARDWARE & STT TESTS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Test 1: Independent Microphone Test (Requirement 134)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "1. Microphone Test (Hardware Verification)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Records 4 seconds independently of STT/AI to verify audio frames and amplitude arriving from physical mic.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isRunningMicTest) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(modifier = Modifier.weight(1f))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Recording… %.0f dB".format(testLiveDb),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        isRunningMicTest = true
                                        micTestResult = null
                                        micTestSuccess = null
                                        viewModel.runIndependentMicTest(
                                            durationSeconds = 4,
                                            onUpdate = { frames, _, db, _ ->
                                                testLiveFrames = frames
                                                testLiveDb = db
                                            },
                                            onComplete = { success, summary ->
                                                isRunningMicTest = false
                                                micTestSuccess = success
                                                micTestResult = summary
                                            }
                                        )
                                    },
                                    modifier = Modifier.testTag("run_mic_test_button")
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Start 4s Microphone Test")
                                }
                            }

                            micTestResult?.let { result ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (micTestSuccess == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (micTestSuccess == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (micTestSuccess == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = result,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (micTestSuccess == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Test 2: Raw STT Test without VAD (Requirement 133)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "2. Raw STT Test (Without VAD / Silence Cutoff)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Bypasses custom silence detection. Audio is sent directly to the speech engine without auto-cutoffs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.runRawSttTest(currentDialect.localeCode)
                                        onDismiss()
                                    },
                                    modifier = Modifier.testTag("run_raw_stt_button")
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Start Raw STT Test")
                                }
                            }
                        }
                    }
                }

                // Test 3: Known Sentence Verification (Requirement 135)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "3. Known Phrase Calibration",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap any phrase to pre-load or test speech synthesis/recognition round-trip:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val phrases = listOf(
                                "Hello, this is a microphone test." to "English",
                                "എനിക്ക് സുഖമാണ്" to "Malayalam",
                                "ഞാൻ ഇന്ന് office കഴിഞ്ഞിട്ട് വരാം." to "Mixed Malayalam-English"
                            )

                            phrases.forEach { (phrase, label) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            Text(text = "\"$phrase\"", style = MaterialTheme.typography.bodySmall)
                                        }
                                        IconButton(
                                            onClick = { viewModel.speakText(phrase) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 3: Live Pipeline Event Log (Requirement 126)
                item {
                    Text(
                        text = "RECENT PIPELINE EVENT LOG",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    val logs = remember { VoicePipelineLogger.getLogs().takeLast(15) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (logs.isEmpty()) {
                                Text(
                                    text = "No events logged yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                logs.forEach { logEntry ->
                                    Text(
                                        text = logEntry,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

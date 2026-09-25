package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceState
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.RegionalDialect
import com.example.data.model.VoicePersonality
import com.example.data.model.VoiceProfileEntity
import com.example.ui.VoiceViewModel
import com.example.ui.components.CompactVoiceWaveBar
import com.example.ui.components.ExplainSlangDialog
import com.example.ui.components.HierarchicalDialectDrillDownModal
import com.example.ui.components.RealTimeKnowledgeSheet
import com.example.ui.components.RegionalSettingsSheet
import com.example.ui.components.SoundWaveStyle
import com.example.ui.components.SoundWaveVisualizer
import com.example.ui.components.VoiceDiagnosticSheet
import androidx.compose.material.icons.filled.BugReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Minimalist, elegant ChatScreen composable featuring:
 * 1. Dynamic Header for Regional Profiles (active dialect badge, personality & pacing pills, switcher & settings).
 * 2. Shared Conversation Thread for Voice/Text (unified history, slang highlighting, playback & translate controls).
 * 3. Simple Input Composer (responsive text input, integrated voice mic with state indicator, send action).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val currentDialect by viewModel.currentDialect.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val selectedExpression by viewModel.selectedExpressionForDetails.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val activeKnowledgeDetails by viewModel.activeKnowledgeDetails.collectAsState()
    val isWebSearchEnabled by viewModel.isWebSearchEnabled.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showDialectSelector by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val dialectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val knowledgeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val diagnosticSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDiagnosticsSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleMic()
        } else {
            viewModel.showNotice("Microphone permission is required to talk.")
        }
    }

    // Automatically scroll to latest message or ongoing transcript
    LaunchedEffect(messages.size, partialTranscript, isAiThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // -------------------------------------------------------------
        // 1. DYNAMIC HEADER FOR REGIONAL PROFILES
        // -------------------------------------------------------------
        DynamicRegionalHeader(
            dialect = currentDialect,
            profile = currentProfile,
            isMuted = isMuted,
            onOpenDialectSelector = { showDialectSelector = true },
            onOpenSettings = { showSettingsSheet = true },
            onToggleMute = { viewModel.toggleMute() },
            onNewChat = { viewModel.startNewConversation() },
            onClearChat = { viewModel.clearConversationHistory() },
            onOpenDiagnostics = { showDiagnosticsSheet = true }
        )

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // -------------------------------------------------------------
        // 2. SHARED CONVERSATION THREAD (VOICE + TEXT)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && partialTranscript.isBlank()) {
                // Minimal empty state with contextual regional ice-breakers
                EmptyConversationPlaceholder(
                    dialect = currentDialect,
                    onStarterClick = { starter ->
                        viewModel.sendChatMessage(starter, speakResponse = !isMuted)
                    },
                    onPlayGreeting = {
                        viewModel.speakText(currentDialect.greeting)
                    },
                    onOpenDialectSelector = { showDialectSelector = true }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("conversation_message_list"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        SharedMessageBubble(
                            message = message,
                            dialect = currentDialect,
                            onPlay = { viewModel.speakText(message.text) },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onTranslate = { viewModel.inspectExpression(message.text) },
                            onInspectSlang = { slang -> viewModel.inspectExpression(slang) },
                            onShowKnowledgeDetails = {
                                viewModel.showKnowledgeDetailsForMessage(message)
                            },
                            onDelete = { viewModel.deleteMessage(message.id) },
                            onRegenerate = if (message == messages.lastOrNull { it.role == "assistant" }) {
                                { viewModel.regenerateLastMessage() }
                            } else null
                        )
                    }

                    // Live Speech Recognition Transcript Bubble
                    if (partialTranscript.isNotBlank()) {
                        item(key = "partial_transcript_item") {
                            LiveTranscriptBubble(
                                transcript = partialTranscript,
                                dialect = currentDialect,
                                soundLevel = soundLevel
                            )
                        }
                    }

                    // AI Processing / Thinking Indicator
                    if (isAiThinking) {
                        item(key = "ai_thinking_item") {
                            AiThinkingBubble(dialect = currentDialect)
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // -------------------------------------------------------------
        // 3. SIMPLE INPUT COMPOSER WITH REAL-TIME SOUND-WAVE VISUALIZATION & WEB TOGGLE
        // -------------------------------------------------------------
        SimpleInputComposer(
            textInput = textInput,
            onTextChanged = { textInput = it },
            voiceState = voiceState,
            soundLevel = soundLevel,
            dialect = currentDialect,
            isWebSearchEnabled = isWebSearchEnabled,
            partialTranscript = partialTranscript,
            onToggleWebSearch = { viewModel.toggleWebSearch() },
            onMicClick = {
                if (voiceState == VoiceState.SPEAKING) {
                    viewModel.interruptAi()
                } else if (voiceState.isRecording) {
                    viewModel.toggleMic()
                } else {
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasMicPermission) {
                        viewModel.toggleMic()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            onSendClick = {
                val toSend = textInput.trim()
                if (toSend.isNotBlank()) {
                    viewModel.sendChatMessage(toSend, speakResponse = !isMuted)
                    textInput = ""
                }
            },
            onSelectSamplePhrase = { phrase ->
                textInput = phrase
            }
        )
    }

    // -----------------------------------------------------------------
    // BOTTOM SHEETS & OVERLAYS
    // -----------------------------------------------------------------
    activeKnowledgeDetails?.let { knowledge ->
        RealTimeKnowledgeSheet(
            sheetState = knowledgeSheetState,
            knowledge = knowledge,
            onDismiss = { viewModel.dismissKnowledgeDetails() }
        )
    }

    if (showSettingsSheet) {
        val promptPreview = remember(currentDialect, currentProfile) {
            viewModel.getPromptContextPreview(
                targetDialect = currentDialect,
                strength = currentProfile?.regionalStrength ?: 0.75f,
                personality = VoicePersonality.entries.find { it.name == currentProfile?.personality } ?: VoicePersonality.FRIENDLY,
                slangEnabled = currentProfile?.slangEnabled ?: true,
                responseLength = currentProfile?.responseLength ?: "Balanced",
                naturalMixing = currentProfile?.naturalMixingEnabled ?: true,
                customPromptNotes = currentProfile?.customPromptNotes ?: ""
            )
        }

        RegionalSettingsSheet(
            sheetState = settingsSheetState,
            profile = currentProfile,
            currentDialect = currentDialect,
            onSelectDialect = { dialect ->
                viewModel.selectDialect(dialect)
            },
            onOpenHierarchicalDrillDown = {
                showSettingsSheet = false
                showDialectSelector = true
            },
            onStrengthChange = { viewModel.updateRegionalStrength(it) },
            onPersonalityChange = { viewModel.updatePersonality(it) },
            onSlangToggle = { viewModel.toggleSlang(it) },
            onNaturalMixingToggle = { viewModel.toggleNaturalMixing(it) },
            onResponseStyleChange = { viewModel.updateResponseStyle(it) },
            onVoiceSpeedChange = { viewModel.updateVoiceSpeed(it) },
            onCustomPromptNotesChange = { viewModel.updateCustomPromptNotes(it) },
            promptContextPreview = promptPreview,
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showDialectSelector) {
        HierarchicalDialectDrillDownModal(
            sheetState = dialectSheetState,
            selectedDialect = currentDialect,
            onSelectDialect = { dialect ->
                viewModel.selectDialect(dialect)
                showDialectSelector = false
            },
            onPreviewAudio = { viewModel.speakText(it) },
            onDismiss = { showDialectSelector = false }
        )
    }

    selectedExpression?.let { expr ->
        ExplainSlangDialog(
            expression = expr,
            onSpeak = { viewModel.speakText(it) },
            onDismiss = { viewModel.dismissExpressionDetails() }
        )
    }

    if (showDiagnosticsSheet) {
        VoiceDiagnosticSheet(
            sheetState = diagnosticSheetState,
            viewModel = viewModel,
            onDismiss = { showDiagnosticsSheet = false }
        )
    }
}

/**
 * Dynamic Header: Shows the current regional profile, tone & pacing badges,
 * and allows instant regional switching or settings adjustment.
 */
@Composable
private fun DynamicRegionalHeader(
    dialect: RegionalDialect,
    profile: VoiceProfileEntity?,
    isMuted: Boolean,
    onOpenDialectSelector: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleMute: () -> Unit,
    onNewChat: () -> Unit,
    onClearChat: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("chat_dynamic_header")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title & Tappable Regional Switcher
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenDialectSelector)
                    .padding(vertical = 2.dp)
                    .testTag("header_dialect_switcher")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${dialect.cityOrArea} Voice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Switch Region",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${dialect.language} · ${dialect.stateOrProvince}, ${dialect.country}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Top action buttons (New Chat, Hierarchy Drill-down, Mute/Unmute, Tune Settings, Overflow Menu)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // New Chat Button
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("header_new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Conversation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Regional Hierarchy Drill-down trigger
                IconButton(
                    onClick = onOpenDialectSelector,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("header_drilldown_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Explore Hierarchy (Language > Country > State > District)",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Audio Playback Mute Toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("header_mute_toggle")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Unmute voice" else "Mute voice",
                        tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Regional Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("header_open_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Regional Voice Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // More Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("header_more_options")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear conversation") },
                            onClick = {
                                showMenu = false
                                onClearChat()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.testTag("menu_clear_conversation")
                        )

                        DropdownMenuItem(
                            text = { Text("Voice diagnostics (dev)") },
                            onClick = {
                                showMenu = false
                                onOpenDiagnostics()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.testTag("menu_voice_diagnostics")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic attribute badges: Hierarchy Breadcrumb, Strength, Tone, Pacing, Slang
        val strengthPct = ((profile?.regionalStrength ?: 0.75f) * 100).toInt()
        val personality = profile?.personality ?: "Friendly"
        val pacing = profile?.voiceSpeed ?: "Natural"
        val slangActive = profile?.slangEnabled ?: true

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Interactive breadcrumb hierarchy pill
            item {
                HeaderPill(
                    text = "${dialect.flagEmoji} ${dialect.language} › ${dialect.country} › ${dialect.stateOrProvince} › ${dialect.cityOrArea}",
                    onClick = onOpenDialectSelector,
                    isHighlighted = true,
                    testTag = "header_hierarchy_breadcrumb_pill"
                )
            }
            item { HeaderPill(text = "$strengthPct% Regional") }
            item { HeaderPill(text = personality) }
            item { HeaderPill(text = "$pacing pace") }
            if (slangActive) {
                item { HeaderPill(text = "Slang active") }
            }
        }
    }
}

@Composable
private fun HeaderPill(
    text: String,
    onClick: (() -> Unit)? = null,
    isHighlighted: Boolean = false,
    testTag: String? = null
) {
    val modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier
        )
        .then(
            if (testTag != null) {
                Modifier.testTag(testTag)
            } else Modifier
        )
        .background(
            if (isHighlighted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        )
        .padding(horizontal = 8.dp, vertical = 3.dp)

    Box(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * Shared message bubble for the unified conversation thread.
 * Minimalist, typography-first, with inline slang inspection and voice replay.
 */
@Composable
private fun SharedMessageBubble(
    message: ConversationMessageEntity,
    dialect: RegionalDialect,
    onPlay: () -> Unit,
    onCopy: () -> Unit,
    onTranslate: () -> Unit,
    onInspectSlang: (String) -> Unit,
    onShowKnowledgeDetails: (() -> Unit)? = null,
    onDelete: () -> Unit,
    onRegenerate: (() -> Unit)?
) {
    val isUser = message.role == "user"
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_message_item" else "ai_message_item")
    ) {
        // Sender Label and Timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isUser) "You" else "${dialect.cityOrArea} AI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (!isUser) {
                    Text(
                        text = "· ${dialect.dialectName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Message Body
        Text(
            text = message.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
        )

        // Real-Time Knowledge & Grounding Source Badge
        if (!isUser && message.isRealTimeKnowledge) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
                    .clickable { onShowKnowledgeDetails?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("real_time_source_badge_${message.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Real-time source",
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Current information • ${message.knowledgeTimestamp.ifBlank { "September 2026" }} (Tap for sources)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Highlighted Regional Slang Pills (if detected in the AI response)
        val slangs = remember(message.highlightedSlangCsv) {
            if (message.highlightedSlangCsv.isNotBlank()) {
                message.highlightedSlangCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        }

        if (!isUser && slangs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (slang in slangs.take(3)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onInspectSlang(slang) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "💡 $slang",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Action Toolbar
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isUser) {
                // Play Voice
                ActionButton(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    label = "Play",
                    onClick = onPlay,
                    testTag = "play_message_button_${message.id}"
                )

                // Copy
                ActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy",
                    onClick = onCopy,
                    testTag = "copy_message_button_${message.id}"
                )

                // Translate / Explain
                ActionButton(
                    icon = Icons.Default.Translate,
                    label = "Translate",
                    onClick = onTranslate,
                    testTag = "translate_message_button_${message.id}"
                )

                // Regenerate
                if (onRegenerate != null) {
                    ActionButton(
                        icon = Icons.Default.Refresh,
                        label = "Regenerate",
                        onClick = onRegenerate,
                        testTag = "regenerate_message_button_${message.id}"
                    )
                }

                // Delete from Room DB
                ActionButton(
                    icon = Icons.Default.DeleteOutline,
                    label = "Delete",
                    onClick = onDelete,
                    testTag = "delete_message_button_${message.id}"
                )
            } else {
                // Copy for User
                ActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy",
                    onClick = onCopy,
                    testTag = "copy_message_button_${message.id}"
                )

                // Delete from Room DB
                ActionButton(
                    icon = Icons.Default.DeleteOutline,
                    label = "Delete",
                    onClick = onDelete,
                    testTag = "delete_message_button_${message.id}"
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Live Speech Transcript Bubble displayed while the user is actively speaking.
 */
@Composable
private fun LiveTranscriptBubble(
    transcript: String,
    dialect: RegionalDialect,
    soundLevel: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp)
            .testTag("live_transcript_bubble")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Listening (${dialect.cityOrArea} speech)...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Real-time sound-wave visualization bar during live transcription
            CompactVoiceWaveBar(
                soundLevel = soundLevel,
                isRecording = true,
                barCount = 14,
                height = 16.dp,
                modifier = Modifier.width(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = transcript,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
    }
}

/**
 * AI Thinking indicator while Gemini is generating the response.
 */
@Composable
private fun AiThinkingBubble(dialect: RegionalDialect) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${dialect.cityOrArea} AI",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Thinking in ${dialect.dialectName} rhythm...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/**
 * Minimal empty state with contextual prompt starters matching the active region.
 */
@Composable
private fun EmptyConversationPlaceholder(
    dialect: RegionalDialect,
    onStarterClick: (String) -> Unit,
    onPlayGreeting: () -> Unit,
    onOpenDialectSelector: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Speak naturally.\nYour way.",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Conversational AI tuned for ${dialect.cityOrArea}, ${dialect.region}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Sample Greeting Playable Chip & Explore Hierarchy Chip
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(onClick = onPlayGreeting)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen to greeting",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Listen greeting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(onClick = onOpenDialectSelector)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("placeholder_explore_hierarchy_button"),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Explore Hierarchy",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Explore hierarchy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contextual Prompt Starters
        val starters = when {
            dialect.id.contains("kozhikode") -> listOf(
                "സുഖല്ലേ ചങ്ങായി?",
                "നല്ലൊരു കോഴിക്കോടൻ ബിരിയാണി കഴിക്കാൻ എവിടെ പോണം?",
                "നാട്ടിൽ ഇപ്പൊ എന്താ വിശേഷം?"
            )
            dialect.id.contains("thrissur") -> listOf(
                "പിന്നെന്തൂട്ടാ ഗഡീ വിശേഷം?",
                "പൂരം വിശേഷങ്ങൾ പറയൂ",
                "ഇവിടെ എന്താ അടിപൊളി ഫുഡ്?"
            )
            dialect.id.contains("liverpool") -> listOf(
                "Alright kidda! How're you keeping?",
                "Where can I get some proper scran?",
                "Tell us a bit about Liverpool!"
            )
            dialect.id.contains("brooklyn") -> listOf(
                "Yo, what's good!",
                "Where's the best slice in town?",
                "How's the neighborhood today?"
            )
            dialect.language.equals("Malayalam", true) -> listOf(
                "ഹലോ, സുഖം തന്നെയല്ലേ?",
                "എന്തൊക്കെയുണ്ട് പുതിയ വിശേഷങ്ങൾ?",
                "നമുക്ക് സംസാരിക്കാം!"
            )
            else -> listOf(
                "Hello! How are you doing today?",
                "Tell me about local traditions here.",
                "What's good around this area?"
            )
        }

        Text(
            text = "Conversation starters:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            starters.forEach { starter ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onStarterClick(starter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = starter,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Simple, unified Input Composer with text input, voice mic, and send button.
 */
@Composable
private fun SimpleInputComposer(
    textInput: String,
    onTextChanged: (String) -> Unit,
    voiceState: VoiceState,
    soundLevel: Float,
    dialect: RegionalDialect,
    isWebSearchEnabled: Boolean,
    partialTranscript: String = "",
    onToggleWebSearch: () -> Unit,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit,
    onSelectSamplePhrase: ((String) -> Unit)? = null
) {
    val isListening = voiceState.isRecording
    val isSpeaking = voiceState.isSpeaking
    val isProcessing = voiceState.isProcessing

    // Infinite breathing scale animation during listening state
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.22f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .imePadding()
            .navigationBarsPadding()
            .testTag("chat_input_composer")
    ) {
        // Voice State and Live Transcript / Visualizer
        AnimatedVisibility(
            visible = isListening || isProcessing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isProcessing) {
                            "⏳ Processing speech with Gemini..."
                        } else {
                            "🎙 Listening in ${dialect.dialectName} (Tap stop when done)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isProcessing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (partialTranscript.isNotBlank() && isListening) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "\"$partialTranscript\"",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SoundWaveVisualizer(
                    soundLevel = soundLevel,
                    isRecording = isListening,
                    waveStyle = SoundWaveStyle.DUAL,
                    barCount = 30,
                    height = 40.dp,
                    showLevelBadge = true
                )
            }
        }

        // Quick Suggestion Chips when idle and input is empty
        if (!isListening && !isProcessing && textInput.isBlank() && dialect.samplePhrases.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(dialect.samplePhrases.take(4)) { phrase ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectSamplePhrase?.invoke(phrase) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = phrase,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Web Search Grounding Toggle Button
            IconButton(
                onClick = onToggleWebSearch,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isWebSearchEnabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                    .testTag("chat_web_search_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = if (isWebSearchEnabled) "Web Search ON" else "Web Search OFF",
                    tint = if (isWebSearchEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text Input Field
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Listening to you..." else if (isWebSearchEnabled) "Search web & ask in ${dialect.cityOrArea}..." else "Speak or write in ${dialect.cityOrArea}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() })
            )

            // Dynamic Action Button: Send if text entered, otherwise Voice Mic
            if (textInput.isNotBlank()) {
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                // Voice Mic Button with Barge-in & State Feedback
                val micBgColor = when {
                    isListening -> MaterialTheme.colorScheme.error
                    isSpeaking -> MaterialTheme.colorScheme.tertiary
                    isProcessing -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.primaryContainer
                }

                val micIconColor = when {
                    isListening -> MaterialTheme.colorScheme.onError
                    isSpeaking -> MaterialTheme.colorScheme.onTertiary
                    isProcessing -> MaterialTheme.colorScheme.onSecondary
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }

                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier
                        .size(46.dp)
                        .scale(if (isListening) pulseScale else 1.0f)
                        .clip(CircleShape)
                        .background(micBgColor)
                        .testTag("chat_voice_mic_button")
                ) {
                    Icon(
                        imageVector = when {
                            isSpeaking -> Icons.Default.Stop
                            isListening -> Icons.Default.Stop
                            isProcessing -> Icons.Default.Refresh
                            else -> Icons.Default.Mic
                        },
                        contentDescription = when {
                            isSpeaking -> "Stop AI Speech"
                            isListening -> "Stop and send speech"
                            isProcessing -> "Processing speech..."
                            else -> "Tap to speak"
                        },
                        tint = micIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

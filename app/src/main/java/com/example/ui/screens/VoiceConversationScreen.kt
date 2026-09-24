package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceState
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.RegionalDialect
import com.example.ui.InputMode
import com.example.ui.VoiceViewModel
import com.example.ui.components.DialectSelectorSheet
import com.example.ui.components.ExplainSlangDialog
import com.example.ui.components.RegionalSettingsSheet
import com.example.ui.components.VoiceVisualizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConversationScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val currentDialect by viewModel.currentDialect.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val selectedExpression by viewModel.selectedExpressionForDetails.collectAsState()
    val inputMode by viewModel.inputMode.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showDialectSelector by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val dialectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size, partialTranscript) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP APP BAR: Minimal & Calm
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Regional Voice AI",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("open_regional_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Regional Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("conversation_overflow_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
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
                                    viewModel.clearConversationHistory()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle Regional Profile Selector: Language · Region · City
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showDialectSelector = true }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("dialect_selector_header"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentDialect.language} · ${currentDialect.region} · ${currentDialect.cityOrArea}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Change region",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // MAIN CONVERSATION AREA
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && partialTranscript.isBlank()) {
                // Calm Minimal Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Talk naturally.\nYour way.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Choose a region and start a conversation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    // Subtle dialect greeting chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.speakText(currentDialect.greeting) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sample: “${currentDialect.greeting}”",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                // Unified Conversation Message List (Left-aligned minimalist layout)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("conversation_message_list"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        CleanMessageItem(
                            message = message,
                            dialect = currentDialect,
                            onPlay = { viewModel.speakText(message.text) },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onTranslate = {
                                viewModel.inspectExpression(message.text)
                            },
                            onInspectSlang = { slang ->
                                viewModel.inspectExpression(slang)
                            },
                            onRegenerate = if (message == messages.lastOrNull { it.role == "assistant" }) {
                                { viewModel.regenerateLastMessage() }
                            } else null
                        )
                    }

                    // Real-time live transcript while user is speaking
                    if (partialTranscript.isNotBlank()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "You",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = partialTranscript,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // AI processing / thinking indicator
                    if (isAiThinking) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${currentDialect.cityOrArea} AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Responding in ${currentDialect.cityOrArea} style...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // BOTTOM CONTROLS: Mode Segmented Control + Input Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .imePadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimal Segmented Control: [ Voice | Write ]
            Row(
                modifier = Modifier
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(2.dp)
                    .testTag("voice_write_mode_toggle"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Voice button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (inputMode == InputMode.VOICE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                        .clickable { viewModel.setInputMode(InputMode.VOICE) }
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                        .testTag("mode_toggle_voice"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Voice",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (inputMode == InputMode.VOICE) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (inputMode == InputMode.VOICE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Write button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (inputMode == InputMode.WRITE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                        .clickable { viewModel.setInputMode(InputMode.WRITE) }
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                        .testTag("mode_toggle_write"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Write",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (inputMode == InputMode.WRITE) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (inputMode == InputMode.WRITE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth Animated Input Area
            AnimatedContent(
                targetState = inputMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "inputModeTransition"
            ) { mode ->
                when (mode) {
                    InputMode.VOICE -> {
                        // Voice Mode: Large calm mic button + subtle controls
                        val lastAiMessage = messages.findLast { it.role == "assistant" }
                        VoiceVisualizer(
                            voiceState = voiceState,
                            soundLevel = soundLevel,
                            isMuted = isMuted,
                            onMicClick = { viewModel.toggleMic() },
                            onInterruptClick = { viewModel.interruptAi() },
                            onToggleMute = { viewModel.toggleMute() },
                            onReplayLast = if (lastAiMessage != null) {
                                { viewModel.speakText(lastAiMessage.text) }
                            } else null
                        )
                    }

                    InputMode.WRITE -> {
                        // Writing Mode: Clean text input field with send button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = {
                                    Text(
                                        text = "Write in ${currentDialect.cityOrArea} style...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_text_input"),
                                singleLine = false,
                                maxLines = 4,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (textInput.isNotBlank()) {
                                            viewModel.sendChatMessage(textInput, speakResponse = false)
                                            textInput = ""
                                        }
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.sendChatMessage(textInput, speakResponse = false)
                                        textInput = ""
                                    }
                                },
                                enabled = textInput.isNotBlank(),
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                    .testTag("send_message_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (textInput.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // REGIONAL SETTINGS SHEET
    if (showSettingsSheet) {
        RegionalSettingsSheet(
            sheetState = settingsSheetState,
            profile = currentProfile,
            onStrengthChange = { viewModel.updateRegionalStrength(it) },
            onPersonalityChange = { viewModel.updatePersonality(it) },
            onSlangToggle = { viewModel.toggleSlang(it) },
            onResponseStyleChange = { viewModel.updateResponseStyle(it) },
            onVoiceSpeedChange = { viewModel.updateVoiceSpeed(it) },
            onDismiss = { showSettingsSheet = false }
        )
    }

    // DIALECT HIERARCHY SELECTOR SHEET
    if (showDialectSelector) {
        DialectSelectorSheet(
            sheetState = dialectSheetState,
            selectedDialect = currentDialect,
            onSelectDialect = { dialect ->
                viewModel.selectDialect(dialect)
                showDialectSelector = false
            },
            onDismiss = { showDialectSelector = false }
        )
    }

    // EXPLAIN SLANG DETAIL SHEET
    selectedExpression?.let { expr ->
        ExplainSlangDialog(
            expression = expr,
            onSpeak = { viewModel.speakText(it) },
            onDismiss = { viewModel.dismissExpressionDetails() }
        )
    }
}

/**
 * Minimalist conversational message item:
 * You / AI label + message text + subtle action row (Play, Copy, Translate, More)
 */
@Composable
fun CleanMessageItem(
    message: ConversationMessageEntity,
    dialect: RegionalDialect,
    onPlay: () -> Unit,
    onCopy: () -> Unit,
    onTranslate: () -> Unit,
    onInspectSlang: (String) -> Unit,
    onRegenerate: (() -> Unit)? = null
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_message_item" else "ai_message_item")
    ) {
        // Sender label
        Text(
            text = if (isUser) "You" else "${dialect.cityOrArea} AI",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Message text
        Text(
            text = message.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 23.sp
        )

        // Actions for AI messages (Play, Copy, Translate, Regenerate)
        if (!isUser) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🔊 Play
                Row(
                    modifier = Modifier
                        .clickable(onClick = onPlay)
                        .padding(vertical = 4.dp)
                        .testTag("play_message_button_${message.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Play voice",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Copy
                Row(
                    modifier = Modifier
                        .clickable(onClick = onCopy)
                        .padding(vertical = 4.dp)
                        .testTag("copy_message_button_${message.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Translate / Explain
                Row(
                    modifier = Modifier
                        .clickable(onClick = onTranslate)
                        .padding(vertical = 4.dp)
                        .testTag("translate_message_button_${message.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Translate / Explain",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Translate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Regenerate
                if (onRegenerate != null) {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onRegenerate)
                            .padding(vertical = 4.dp)
                            .testTag("regenerate_message_button_${message.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Regenerate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

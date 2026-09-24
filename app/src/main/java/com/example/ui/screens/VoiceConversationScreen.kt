package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceState
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.ConversationMode
import com.example.ui.VoiceViewModel
import com.example.ui.components.DialectSelectorSheet
import com.example.ui.components.ExplainSlangDialog
import com.example.ui.components.RegionalStrengthSlider
import com.example.ui.components.VoiceVisualizer
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceConversationScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val currentDialect by viewModel.currentDialect.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val selectedExpression by viewModel.selectedExpressionForDetails.collectAsState()
    val slangTranslationState by viewModel.slangTranslationState.collectAsState()
    val practiceState by viewModel.practiceState.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showTextInputBar by remember { mutableStateOf(false) }
    var showDialectSheet by remember { mutableStateOf(false) }
    var showControlsDropdown by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val dialectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auto scroll to latest message
    LaunchedEffect(messages.size, partialTranscript) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showDialectSheet) {
        DialectSelectorSheet(
            sheetState = dialectSheetState,
            selectedDialect = currentDialect,
            onSelectDialect = { viewModel.selectDialect(it) },
            onDismiss = { showDialectSheet = false }
        )
    }

    if (selectedExpression != null) {
        ExplainSlangDialog(
            expression = selectedExpression!!,
            onSpeak = { viewModel.speakText(it) },
            onDismiss = { viewModel.dismissExpressionDetails() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar: Dialect Pill, Mode Pills, and Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dialect badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clickable { showDialectSheet = true }
                            .testTag("current_dialect_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = currentDialect.flagEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentDialect.dialectName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${currentDialect.cityOrArea} • ${currentDialect.language}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Quick Actions (Text toggle, Slider toggle, Clear)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showControlsDropdown = !showControlsDropdown },
                            modifier = Modifier.testTag("toggle_sliders_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Strength and Style settings",
                                tint = if (showControlsDropdown) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showTextInputBar = !showTextInputBar },
                            modifier = Modifier.testTag("toggle_text_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Toggle Keyboard",
                                tint = if (showTextInputBar) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { viewModel.clearConversationHistory() },
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Chat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Collapsible Regional Strength Slider Card
                AnimatedVisibility(visible = showControlsDropdown) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        RegionalStrengthSlider(
                            strength = currentProfile?.regionalStrength ?: 0.75f,
                            onStrengthChange = { viewModel.updateRegionalStrength(it) }
                        )
                    }
                }

                // Mode Selector Chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        ConversationMode.FREE_CONVERSATION to "Free Voice",
                        ConversationMode.DIALECT_PRACTICE to "Practice",
                        ConversationMode.SLANG_TRANSLATOR to "Translator"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = currentMode == mode,
                            onClick = { viewModel.setConversationMode(mode) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Sub-Mode Specific View or Main Conversation View
        when (currentMode) {
            ConversationMode.DIALECT_PRACTICE -> {
                DialectPracticeSection(
                    practiceState = practiceState,
                    onNextPhrase = { viewModel.nextPracticePhrase() },
                    onPlayTarget = { viewModel.playCurrentPracticePhrase() },
                    modifier = Modifier.weight(1f)
                )
            }

            ConversationMode.SLANG_TRANSLATOR -> {
                SlangTranslatorSection(
                    state = slangTranslationState,
                    onTextChange = { viewModel.updateTranslationInput(it) },
                    onTranslate = { viewModel.translateCurrentInput() },
                    onSwap = { viewModel.swapTranslationDialects() },
                    onSpeak = { viewModel.speakText(it) },
                    onInspect = { viewModel.inspectExpression(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                // Free Voice Conversation Chat Log
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty() && partialTranscript.isEmpty()) {
                        // Empty State with Conversation Suggestions
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentDialect.flagEmoji,
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Start Talking in ${currentDialect.dialectName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the large microphone below and speak naturally. The AI will respond in authentic ${currentDialect.cityOrArea} rhythm.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Try saying:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            currentDialect.samplePhrases.take(3).forEach { phrase ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.handleUserInput(phrase) }
                                ) {
                                    Text(
                                        text = "“$phrase”",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(messages) { msg ->
                                ChatMessageBubble(
                                    message = msg,
                                    onSlangClick = { slang -> viewModel.inspectExpression(slang) },
                                    onSpeak = { viewModel.speakText(msg.text) }
                                )
                            }

                            // Real-time live transcript bubble while speaking
                            if (partialTranscript.isNotBlank()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = CyanPrimary.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(CyanLight)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = partialTranscript,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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

        // Optional Text Input Bar for mixed voice/text
        AnimatedVisibility(visible = showTextInputBar) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_chat_text_input"),
                        placeholder = { Text("Type in ${currentDialect.language} or dialect...") },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.handleUserInput(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier.testTag("send_text_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = CyanPrimary
                        )
                    }
                }
            }
        }

        // Dedicated Bottom Voice Interaction Deck
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            VoiceVisualizer(
                voiceState = voiceState,
                soundLevel = soundLevel,
                isMuted = isMuted,
                onMicClick = { viewModel.toggleMic() },
                onInterruptClick = { viewModel.interruptAi() },
                onToggleMute = { viewModel.toggleMute() },
                modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatMessageBubble(
    message: ConversationMessageEntity,
    onSlangClick: (String) -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Replay message speech",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    // Clickable slang chips for regional expressions used in message
                    val slangList = message.highlightedSlangCsv.split(",").filter { it.isNotBlank() }
                    if (slangList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            slangList.forEach { slang ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isUser) Color.White.copy(alpha = 0.2f) else VioletAccent.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { onSlangClick(slang) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "Explain",
                                            tint = if (isUser) Color.White else VioletAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = slang,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) Color.White else VioletAccent
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
}

// Dialect Practice Mode UI (Voice Learning Mode)
@Composable
fun DialectPracticeSection(
    practiceState: com.example.ui.PracticeUiState,
    onNextPhrase: () -> Unit,
    onPlayTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = VioletAccent.copy(alpha = 0.15f)
        ) {
            Text(
                text = "VOICE LEARNING & PRONUNCIATION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = VioletAccent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Listen & Repeat in Local Accent",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Target Phrase Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = practiceState.targetPhrase.ifEmpty { "Select a phrase" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPlayTarget,
                        modifier = Modifier.testTag("play_target_phrase_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Listen to Native AI")
                    }

                    TextButton(onClick = onNextPhrase) {
                        Text("Next Phrase")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback Card if evaluated
        if (practiceState.feedback != null) {
            val fb = practiceState.feedback
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cadence Accuracy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${fb.accuracyPercentage}% Match",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = fb.praiseOrCorrection, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tip: ${fb.audioPracticeTip}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            Text(
                text = "Tap the mic below and pronounce the phrase above!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Slang Translator Mode UI
@Composable
fun SlangTranslatorSection(
    state: com.example.ui.SlangTranslationUiState,
    onTextChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onSwap: () -> Unit,
    onSpeak: (String) -> Unit,
    onInspect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dialect Selector Row with Swap Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "From", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(text = state.sourceDialect, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onSwap) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = "Swap")
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = "To", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(text = state.targetDialect, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CyanPrimary)
                }
            }
        }

        // Input Text Field
        OutlinedTextField(
            value = state.inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("slang_translator_input"),
            placeholder = { Text("Enter sentence to convert into ${state.targetDialect}...") },
            shape = RoundedCornerShape(14.dp)
        )

        Button(
            onClick = onTranslate,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("translate_slang_button")
        ) {
            Text(if (state.isTranslating) "Converting to Regional Slang..." else "Convert to Regional Style")
        }

        // Output Result Card
        if (state.result != null) {
            val res = state.result
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Regional Slang Conversion",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { onSpeak(res.convertedText) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen")
                        }
                    }

                    Text(
                        text = "“${res.convertedText}”",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = res.explanation, style = MaterialTheme.typography.bodySmall)

                    if (res.substitutedSlang.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Slang Substitutions:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        res.substitutedSlang.forEach { sub ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .clickable { onInspect(sub.regionalSlang) }
                            ) {
                                Text(
                                    text = "${sub.originalWord} → ${sub.regionalSlang} (${sub.meaning})",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

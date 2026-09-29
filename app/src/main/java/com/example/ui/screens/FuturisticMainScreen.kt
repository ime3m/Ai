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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.example.ui.components.AudioLevelIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.data.model.RegionalDialect
import com.example.data.model.VoicePersonality
import com.example.data.repository.DialectCatalog
import com.example.ui.VoiceViewModel
import com.example.ui.components.CompactVoiceWaveBar
import com.example.ui.components.ExplainSlangDialog
import com.example.ui.components.HierarchicalDialectDrillDownModal
import com.example.ui.components.RealTimeKnowledgeSheet
import com.example.ui.components.RegionalSettingsSheet
import com.example.ui.components.VoiceDiagnosticSheet
import com.example.ui.components.futuristic.AppHeader
import com.example.ui.components.futuristic.CategoryPill
import com.example.ui.components.futuristic.FuturisticCategory
import com.example.ui.components.futuristic.FuturisticCategoryBar
import com.example.ui.components.futuristic.HeroSection
import com.example.ui.components.futuristic.ParticleBackground
import com.example.ui.components.futuristic.PrimaryPillButton
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens
import kotlinx.coroutines.launch

/**
 * Production-Quality Futuristic Android AI Interface.
 * Implements the minimal, Google-inspired visual language:
 * - ParticleBackground with subtle floating light-blue dots & clean whitespace
 * - Lightweight AppHeader with sleek branding & dark pill menu button
 * - Centered HeroSection with large modern typography and high-contrast pill CTA
 * - Smooth category navigation (Voice, Translate, Regional, Conversation, AI Tools)
 * - Horizontally scrollable CategoryPill navigation bar near the bottom
 * - Full preservation of voice recording, Gemini AI logic, Room DB history, and tools
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuturisticMainScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val currentDialect by viewModel.currentDialect.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val selectedExpression by viewModel.selectedExpressionForDetails.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val rmsDb by viewModel.rmsDb.collectAsState()
    val activeKnowledgeDetails by viewModel.activeKnowledgeDetails.collectAsState()

    val selectedRegionProfile by viewModel.selectedRegionProfile.collectAsState()
    val showFirstLaunchWelcome by viewModel.showFirstLaunchWelcome.collectAsState()

    var selectedCategory by remember { mutableStateOf(FuturisticCategory.VOICE) }
    var showRegionSelector by remember { mutableStateOf(false) }
    var showDialectSelector by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showDiagnosticsSheet by remember { mutableStateOf(false) }

    // Modal Bottom Sheets
    val regionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dialectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val diagnosticSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val knowledgeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showRegionSelector) {
        com.example.ui.components.region.RegionAndVoiceSelectorModal(
            sheetState = regionSheetState,
            selectedRegionId = selectedRegionProfile.id,
            onSelectRegion = { regionProfile ->
                viewModel.selectRegionProfile(regionProfile)
                showRegionSelector = false
            },
            onDismiss = { showRegionSelector = false }
        )
    }

    // First Launch Onboarding Experience
    if (showFirstLaunchWelcome) {
        com.example.ui.components.region.FirstLaunchWelcomeScreen(
            selectedProfile = selectedRegionProfile,
            onSelectQuickProfile = { viewModel.selectRegionProfile(it) },
            onContinue = { viewModel.completeFirstLaunch() },
            onChangeRegion = { showRegionSelector = true }
        )
        return
    }

    // Dialog state for Slang Explanation
    if (selectedExpression != null) {
        ExplainSlangDialog(
            expression = selectedExpression!!,
            onSpeak = { viewModel.speakText(it) },
            onDismiss = { viewModel.dismissExpressionDetails() }
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
            onDismiss = { showDialectSelector = false }
        )
    }

    if (showSettingsSheet) {
        RegionalSettingsSheet(
            sheetState = settingsSheetState,
            profile = currentProfile,
            currentDialect = currentDialect,
            activeRegionProfile = selectedRegionProfile,
            onOpenRegionSelector = {
                showSettingsSheet = false
                showRegionSelector = true
            },
            onSelectDialect = { viewModel.selectDialect(it) },
            onOpenHierarchicalDrillDown = {
                showSettingsSheet = false
                showDialectSelector = true
            },
            onStrengthChange = { viewModel.updateRegionalStrength(it) },
            onPersonalityChange = { viewModel.updatePersonality(it) },
            onSlangToggle = { viewModel.toggleSlang(it) },
            onNaturalMixingToggle = { viewModel.toggleNaturalMixing(it) },
            onResponseStyleChange = { viewModel.updateResponseLength(it) },
            onVoiceSpeedChange = { viewModel.updateVoiceSpeed(it) },
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showDiagnosticsSheet) {
        VoiceDiagnosticSheet(
            sheetState = diagnosticSheetState,
            viewModel = viewModel,
            onDismiss = { showDiagnosticsSheet = false }
        )
    }

    if (activeKnowledgeDetails != null) {
        RealTimeKnowledgeSheet(
            sheetState = knowledgeSheetState,
            knowledge = activeKnowledgeDetails!!,
            onDismiss = { viewModel.dismissKnowledgeDetails() }
        )
    }

    val isVoiceActive = voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING || isAiThinking

    // Particle Background wrapping the entire minimal interface with subtle voice activity response
    ParticleBackground(
        modifier = modifier,
        soundLevel = soundLevel,
        isVoiceActive = isVoiceActive
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // -------------------------------------------------------------
            // 1. MINIMAL LIGHTWEIGHT APP HEADER
            // -------------------------------------------------------------
            AppHeader(
                appName = "Regional Voice AI",
                activeDialectName = "${currentDialect.cityOrArea} • ${currentDialect.language}",
                regionEmoji = selectedRegionProfile.flagEmoji,
                regionName = selectedRegionProfile.name,
                isMuted = isMuted,
                onToggleMute = { viewModel.toggleMute() },
                onOpenRegionVoice = { showRegionSelector = true },
                onOpenDialects = { showDialectSelector = true },
                onOpenSettings = { showSettingsSheet = true },
                onOpenDiagnostics = { showDiagnosticsSheet = true }
            )

            // -------------------------------------------------------------
            // 2. DYNAMIC CONTENT AREA BASED ON SELECTED CATEGORY
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = selectedCategory,
                    transitionSpec = {
                        fadeIn(androidx.compose.animation.core.tween(220)) togetherWith
                                fadeOut(androidx.compose.animation.core.tween(180))
                    },
                    label = "category_screen_switch"
                ) { targetCat ->
                    when (targetCat) {
                        FuturisticCategory.VOICE -> {
                            VoiceHeroContent(
                                viewModel = viewModel,
                                currentDialect = currentDialect,
                                voiceState = voiceState,
                                isAiThinking = isAiThinking,
                                soundLevel = soundLevel,
                                partialTranscript = partialTranscript,
                                messages = messages,
                                isMuted = isMuted,
                                onOpenDialects = { showDialectSelector = true },
                                onExploreVoices = { selectedCategory = FuturisticCategory.LANGUAGES }
                            )
                        }

                        FuturisticCategory.LANGUAGES -> {
                            ExploreDialectsScreen(
                                viewModel = viewModel,
                                onStartVoiceChat = { selectedCategory = FuturisticCategory.VOICE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        FuturisticCategory.ACCENTS -> {
                            FuturisticRegionalContent(
                                viewModel = viewModel,
                                currentDialect = currentDialect,
                                currentProfile = currentProfile,
                                onOpenDialectModal = { showDialectSelector = true }
                            )
                        }

                        FuturisticCategory.TRANSLATE -> {
                            FuturisticTranslateContent(
                                viewModel = viewModel,
                                currentDialect = currentDialect
                            )
                        }

                        FuturisticCategory.CONVERSATION -> {
                            FuturisticConversationContent(
                                viewModel = viewModel,
                                currentDialect = currentDialect,
                                messages = messages,
                                partialTranscript = partialTranscript,
                                soundLevel = soundLevel,
                                isMuted = isMuted
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. HORIZONTALLY SCROLLABLE BOTTOM CATEGORY BAR
            // -------------------------------------------------------------
            FuturisticCategoryBar(
                categories = FuturisticCategory.entries,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Hero Content for VOICE Category.
 * Features the large headline, description, primary CTA pill button,
 * active sound visualizer, live partial transcript, and quick-prompt suggestions.
 */
@Composable
private fun VoiceHeroContent(
    viewModel: VoiceViewModel,
    currentDialect: RegionalDialect,
    voiceState: VoiceState,
    isAiThinking: Boolean,
    soundLevel: Float,
    partialTranscript: String,
    messages: List<com.example.data.model.ConversationMessageEntity>,
    isMuted: Boolean,
    onOpenDialects: () -> Unit,
    onExploreVoices: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val userNotice by viewModel.userFeedbackNotice.collectAsState()
    val rmsDb by viewModel.rmsDb.collectAsState()

    val isVoiceActive = voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING || isAiThinking

    LaunchedEffect(messages.size, isAiThinking, partialTranscript) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Content Area: Empty Hero or Message Stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    HeroSection(
                        headline = "Speak Naturally.",
                        description = "AI voice conversations with authentic ${viewModel.selectedRegionProfile.value.name} regional dialect, slang, and cultural warmth.",
                        buttonText = if (voiceState == VoiceState.LISTENING) "Stop Listening" else "Start Speaking",
                        secondaryButtonText = "Explore Regions",
                        onButtonClick = { viewModel.toggleMic() },
                        onSecondaryClick = onExploreVoices,
                        voiceState = voiceState,
                        isThinking = isAiThinking,
                        soundLevel = soundLevel,
                        dialectBadgeText = "${viewModel.selectedRegionProfile.value.flagEmoji} ${viewModel.selectedRegionProfile.value.name} • ${currentDialect.dialectName}",
                        onBadgeClick = onOpenDialects
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Suggested Prompts
                    Text(
                        text = "Suggested Inquiries",
                        color = FuturisticTheme.secondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val promptList = listOf(
                        "ഇന്ന് കുവൈത്തിലെ കാലാവസ്ഥ എങ്ങനെയാണ്?",
                        "ഇന്നത്തെ തീയതി എത്രയാണ്?",
                        "എന്താ വിശേഷം?",
                        "What is the weather in Dubai?",
                        "കേരളത്തിന്റെ തലസ്ഥാനം ഏതാണ്?",
                        "1000 രൂപയുടെ 15% എത്രയാണ്?"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        promptList.forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .clip(FuturisticTokens.CornerRadius.pillShape)
                                    .background(FuturisticTheme.surface)
                                    .border(1.dp, FuturisticTheme.border.copy(alpha = 0.6f), FuturisticTokens.CornerRadius.pillShape)
                                    .clickable {
                                        viewModel.sendChatMessage(prompt, speakResponse = !isMuted)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("prompt_chip_$prompt")
                            ) {
                                Text(
                                    text = prompt,
                                    color = FuturisticTheme.primaryText,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.role == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 320.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 20.dp,
                                            topEnd = 20.dp,
                                            bottomStart = if (isUser) 20.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 20.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) FuturisticTheme.primaryButton else FuturisticTheme.surfaceElevated
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isUser) Color.Transparent else FuturisticTheme.border,
                                        shape = RoundedCornerShape(
                                            topStart = 20.dp,
                                            topEnd = 20.dp,
                                            bottomStart = if (isUser) 20.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 20.dp
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.text,
                                        color = if (isUser) FuturisticTheme.primaryButtonText else FuturisticTheme.primaryText,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp
                                    )
                                    if (!isUser) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.speakText(msg.text) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "Speak",
                                                    tint = FuturisticTheme.accent,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(msg.text))
                                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = FuturisticTheme.secondaryText,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            if (msg.isRealTimeKnowledge) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(FuturisticTokens.CornerRadius.pillShape)
                                                        .background(FuturisticTheme.surface)
                                                        .clickable { viewModel.showKnowledgeDetailsForMessage(msg) }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color(0xFF16A34A),
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Verified",
                                                            fontSize = 10.sp,
                                                            color = FuturisticTheme.secondaryText
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
            }
        }

        // Live Voice Visualizer & Audio-Level Indicator with Pulse Animation
        AnimatedVisibility(
            visible = isVoiceActive || partialTranscript.isNotBlank(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (voiceState == VoiceState.LISTENING) {
                    AudioLevelIndicator(
                        soundLevel = soundLevel,
                        voiceState = voiceState,
                        rmsDb = rmsDb,
                        isListening = true,
                        onMicClick = { viewModel.toggleMic() },
                        primaryColor = FuturisticTheme.accent,
                        accentColor = Color(0xFF10B981),
                        showWaveform = true,
                        showStatusText = true,
                        showDecibelBadge = true
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(FuturisticTokens.CornerRadius.pillShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.4f), FuturisticTokens.CornerRadius.pillShape)
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isAiThinking -> FuturisticTheme.accent
                                            voiceState == VoiceState.SPEAKING -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    partialTranscript.isNotBlank() -> "\"$partialTranscript\""
                                    isAiThinking -> "Thinking & Reasoning..."
                                    voiceState == VoiceState.SPEAKING -> "AI Speaking"
                                    else -> "Ready"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = FuturisticTheme.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            CompactVoiceWaveBar(
                                soundLevel = soundLevel,
                                isRecording = voiceState == VoiceState.SPEAKING,
                                height = 18.dp,
                                color = FuturisticTheme.accent
                            )
                        }
                    }
                }
            }
        }

        // Error State Banner with Retry Option
        if (userNotice != null && userNotice!!.contains("error", ignoreCase = true)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(FuturisticTokens.CornerRadius.smallShape)
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFCA5A5), FuturisticTokens.CornerRadius.smallShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = userNotice ?: "Voice input error occurred",
                        color = Color(0xFF991B1B),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.toggleMic() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = Color(0xFF991B1B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Bottom Unified Dual-Input Composer Bar (Text + Mic)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FuturisticTokens.CornerRadius.pillShape)
                    .background(FuturisticTheme.surfaceElevated)
                    .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Ask anything or tap mic...",
                            color = FuturisticTheme.tertiaryText,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("conversation_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = FuturisticTheme.primaryText,
                        unfocusedTextColor = FuturisticTheme.primaryText
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput, speakResponse = !isMuted)
                            textInput = ""
                        }
                    })
                )

                // Microphone Button (Listening/Stop/Start)
                IconButton(
                    onClick = { viewModel.toggleMic() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceState == VoiceState.LISTENING) Color(0xFFEF4444) else FuturisticTheme.surface
                        )
                        .testTag("composer_mic_btn")
                ) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (voiceState == VoiceState.LISTENING) "Stop listening" else "Start listening",
                        tint = if (voiceState == VoiceState.LISTENING) Color.White else FuturisticTheme.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput, speakResponse = !isMuted)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank(),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (textInput.isNotBlank()) FuturisticTheme.primaryButton else FuturisticTheme.surface
                        )
                        .testTag("composer_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send text",
                        tint = if (textInput.isNotBlank()) FuturisticTheme.primaryButtonText else FuturisticTheme.secondaryText.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * TRANSLATE Category Content.
 * Regional Slang & Dialect Translator with clean, minimal cards.
 */
@Composable
private fun FuturisticTranslateContent(
    viewModel: VoiceViewModel,
    currentDialect: RegionalDialect
) {
    val scrollState = rememberScrollState()
    var sourceText by remember { mutableStateOf("") }
    val translationState by viewModel.slangTranslationState.collectAsState()
    val translationResult = translationState.result
    val isTranslating = translationState.isTranslating

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Regional Slang Translator",
            color = FuturisticTheme.primaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Convert text seamlessly across Kerala regional dialects & slang",
            color = FuturisticTheme.secondaryText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Input card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FuturisticTokens.CornerRadius.largeShape)
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.largeShape)
                .padding(16.dp)
        ) {
            Column {
                OutlinedTextField(
                    value = sourceText,
                    onValueChange = { sourceText = it },
                    placeholder = { Text("Enter sentence to translate...", color = FuturisticTheme.tertiaryText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("translate_input_field"),
                    shape = FuturisticTokens.CornerRadius.mediumShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FuturisticTheme.accent,
                        unfocusedBorderColor = FuturisticTheme.border,
                        focusedContainerColor = FuturisticTheme.surface,
                        unfocusedContainerColor = FuturisticTheme.surface,
                        focusedTextColor = FuturisticTheme.primaryText,
                        unfocusedTextColor = FuturisticTheme.primaryText
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick sample phrases
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("സുഹൃത്തേ, നമുക്ക് ഒരു ചായ കുടിക്കാം", "അത് വളരെ നല്ലൊരു കാര്യമാണ്", "സുഖമാണോ കൂട്ടുകാരാ").forEach { sample ->
                        Box(
                            modifier = Modifier
                                .clip(FuturisticTokens.CornerRadius.pillShape)
                                .background(FuturisticTheme.surface)
                                .clickable { sourceText = sample }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(sample, fontSize = 12.sp, color = FuturisticTheme.secondaryText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryPillButton(
                    text = if (isTranslating) "Translating..." else "Translate to ${currentDialect.cityOrArea} Style",
                    icon = Icons.Default.Translate,
                    isLoading = isTranslating,
                    onClick = {
                        if (sourceText.isNotBlank()) {
                            viewModel.updateTranslationInput(sourceText)
                            viewModel.setTranslationDialects("Standard Malayalam", "${currentDialect.cityOrArea} (${currentDialect.dialectName})")
                            viewModel.translateCurrentInput()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_translate_slang"
                )
            }
        }

        // Translation Result Card
        if (translationResult != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FuturisticTokens.CornerRadius.largeShape)
                    .background(FuturisticTheme.surfaceElevated)
                    .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.largeShape)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Converted Result:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.secondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = translationResult.convertedText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = translationResult.explanation,
                        fontSize = 13.sp,
                        color = FuturisticTheme.secondaryText
                    )

                    if (translationResult.substitutedSlang.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Slang Substitutions:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FuturisticTheme.primaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        translationResult.substitutedSlang.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${sub.originalWord} → ${sub.regionalSlang}", fontWeight = FontWeight.Medium, color = FuturisticTheme.accent, fontSize = 13.sp)
                                Text(sub.meaning, color = FuturisticTheme.secondaryText, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * REGIONAL Category Content.
 * Regional Dialect selector, Regional strength slider, and Voice personality options.
 */
@Composable
private fun FuturisticRegionalContent(
    viewModel: VoiceViewModel,
    currentDialect: RegionalDialect,
    currentProfile: com.example.data.model.VoiceProfileEntity?,
    onOpenDialectModal: () -> Unit
) {
    val scrollState = rememberScrollState()
    val strength = currentProfile?.regionalStrength ?: 0.75f
    val currentPersonalityName = currentProfile?.personality ?: VoicePersonality.FRIENDLY.name

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dialect & Regional Accent",
            color = FuturisticTheme.primaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Fine-tune local cadence, regional slang strength, and conversational style",
            color = FuturisticTheme.secondaryText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Active Dialect Showcase Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FuturisticTokens.CornerRadius.largeShape)
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.largeShape)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentDialect.dialectName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FuturisticTheme.primaryText
                        )
                        Text(
                            text = "${currentDialect.cityOrArea}, ${currentDialect.region}, ${currentDialect.country}",
                            fontSize = 13.sp,
                            color = FuturisticTheme.secondaryText
                        )
                    }

                    PrimaryPillButton(
                        text = "Change",
                        onClick = onOpenDialectModal,
                        height = 42.dp,
                        testTag = "btn_change_dialect"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FuturisticTokens.CornerRadius.mediumShape)
                        .background(FuturisticTheme.surface)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "\"${currentDialect.greeting}\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = FuturisticTheme.primaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Regional Strength Slider Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FuturisticTokens.CornerRadius.largeShape)
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.largeShape)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Regional Slang Strength",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText
                    )
                    Text(
                        text = "${(strength * 100).toInt()}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.accent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = strength,
                    onValueChange = { viewModel.updateRegionalStrength(it) },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = FuturisticTheme.accent,
                        activeTrackColor = FuturisticTheme.accent,
                        inactiveTrackColor = FuturisticTheme.border
                    ),
                    modifier = Modifier.testTag("regional_strength_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Standard / Formal", fontSize = 11.sp, color = FuturisticTheme.secondaryText)
                    Text("Deep Authentic Vernacular", fontSize = 11.sp, color = FuturisticTheme.secondaryText)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Personality Options
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FuturisticTokens.CornerRadius.largeShape)
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.largeShape)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Conversational Voice Personality",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FuturisticTheme.primaryText
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoicePersonality.entries.forEach { personality ->
                        val isSelected = personality.name == currentPersonalityName
                        Box(
                            modifier = Modifier
                                .clip(FuturisticTokens.CornerRadius.pillShape)
                                .background(if (isSelected) FuturisticTheme.primaryButton else FuturisticTheme.surface)
                                .clickable { viewModel.updatePersonality(personality) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("personality_${personality.name}")
                        ) {
                            Text(
                                text = personality.title,
                                color = if (isSelected) FuturisticTheme.primaryButtonText else FuturisticTheme.primaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * CONVERSATION Category Content.
 * Full scrollable conversation thread from Room database with input composer.
 */
@Composable
private fun FuturisticConversationContent(
    viewModel: VoiceViewModel,
    currentDialect: RegionalDialect,
    messages: List<com.example.data.model.ConversationMessageEntity>,
    partialTranscript: String,
    soundLevel: Float,
    isMuted: Boolean
) {
    val listState = rememberLazyListState()
    var textInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(messages.size, partialTranscript) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Message thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = if (isUser) 20.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 20.dp
                                )
                            )
                            .background(
                                if (isUser) FuturisticTheme.primaryButton else FuturisticTheme.surfaceElevated
                            )
                            .border(
                                width = 1.dp,
                                color = if (isUser) Color.Transparent else FuturisticTheme.border,
                                shape = RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = if (isUser) 20.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 20.dp
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                color = if (isUser) FuturisticTheme.primaryButtonText else FuturisticTheme.primaryText,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )

                            if (!isUser) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.speakText(msg.text) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Speak",
                                            tint = FuturisticTheme.accent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(msg.text))
                                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = FuturisticTheme.secondaryText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Composer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FuturisticTokens.CornerRadius.pillShape)
                    .background(FuturisticTheme.surfaceElevated)
                    .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask anything or tap mic...", color = FuturisticTheme.tertiaryText, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("conversation_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = FuturisticTheme.primaryText,
                        unfocusedTextColor = FuturisticTheme.primaryText
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput, speakResponse = !isMuted)
                            textInput = ""
                        }
                    })
                )

                IconButton(
                    onClick = { viewModel.toggleMic() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.surface)
                        .testTag("composer_mic_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = FuturisticTheme.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput, speakResponse = !isMuted)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.primaryButton)
                        .testTag("composer_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = FuturisticTheme.primaryButtonText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * AI TOOLS Category Content.
 * Quick access to Real-Time Knowledge, Personal Speaking Style, and Diagnostics.
 */
@Composable
private fun FuturisticToolsContent(
    viewModel: VoiceViewModel,
    currentDialect: RegionalDialect,
    onOpenDiagnostics: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Capabilities & Tools",
            color = FuturisticTheme.primaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Explore real-time knowledge, response trace diagnostics, and personal style",
            color = FuturisticTheme.secondaryText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tool 1: Real-Time Knowledge Lookups
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FuturisticTokens.CornerRadius.largeShape)
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.largeShape)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Real-Time Knowledge Engine",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FuturisticTheme.primaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Accurate time-sensitive retrieval (September 2026 data: Kerala CM, Weather, Gold Price, KWD exchange rate)",
                    fontSize = 13.sp,
                    color = FuturisticTheme.secondaryText
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("മുഖ്യമന്ത്രി", "കാലാവസ്ഥ", "സ്വർണ്ണവില", "ദിനാർ").forEach { topic ->
                        Box(
                            modifier = Modifier
                                .clip(FuturisticTokens.CornerRadius.pillShape)
                                .background(FuturisticTheme.surface)
                                .clickable {
                                    val q = when (topic) {
                                        "മുഖ്യമന്ത്രി" -> "ഇപ്പോഴത്തെ കേരള മുഖ്യമന്ത്രി ആരാണ്?"
                                        "കാലാവസ്ഥ" -> "ഇന്നത്തെ കുവൈത്തിലെ കാലാവസ്ഥ എങ്ങനെയാണ്?"
                                        "സ്വർണ്ണവില" -> "ഇന്നത്തെ കേരളത്തിലെ സ്വർണ്ണവില എത്രയാണ്?"
                                        else -> "ഒരു കുവൈറ്റ് ദിനാറിന്റെ ഇന്ത്യൻ രൂപ എത്രയാണ്?"
                                    }
                                    viewModel.sendChatMessage(q)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(topic, fontSize = 12.sp, color = FuturisticTheme.primaryText, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tool 2: Developer AI Response Trace
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FuturisticTokens.CornerRadius.largeShape)
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.largeShape)
                .clickable(onClick = onOpenDiagnostics)
                .padding(20.dp)
                .testTag("tool_card_diagnostics")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Response Trace & Diagnostics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View detected user intent, relevance check (PASS/FAIL), loop detector, and system trace",
                        fontSize = 13.sp,
                        color = FuturisticTheme.secondaryText
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = FuturisticTheme.secondaryText
                )
            }
        }
    }
}

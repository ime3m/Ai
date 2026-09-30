package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.FontStyle
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
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
import com.example.ui.components.image.AttachmentOptionButton
import com.example.ui.components.image.DocumentAttachmentPreviewChip
import com.example.ui.components.image.GeneratedImageMessageCard
import com.example.ui.components.image.ImageAttachmentPreviewChip
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
    val streamingAiText by viewModel.streamingAiText.collectAsState()
    val requestError by viewModel.requestError.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val rmsDb by viewModel.rmsDb.collectAsState()
    val activeKnowledgeDetails by viewModel.activeKnowledgeDetails.collectAsState()
    val isChatWindowOpen by viewModel.isChatWindowOpen.collectAsState()
    val chatSuggestions by viewModel.chatSuggestions.collectAsState()

    val selectedRegionProfile by viewModel.selectedRegionProfile.collectAsState()
    val showFirstLaunchWelcome by viewModel.showFirstLaunchWelcome.collectAsState()

    var selectedCategory by remember { mutableStateOf(FuturisticCategory.VOICE) }

    BackHandler(enabled = isChatWindowOpen && selectedCategory == FuturisticCategory.VOICE) {
        viewModel.closeChatWindow()
    }
    BackHandler(enabled = selectedCategory != FuturisticCategory.VOICE && !isChatWindowOpen) {
        selectedCategory = FuturisticCategory.VOICE
    }
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

    // Slang Explanation Dialog
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
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onToggleMute = { viewModel.toggleMute() },
                onOpenRegionVoice = { showRegionSelector = true },
                onOpenDialects = { showDialectSelector = true },
                onOpenSettings = { showSettingsSheet = true },
                onOpenDiagnostics = null
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
                                streamingAiText = streamingAiText,
                                requestError = requestError,
                                soundLevel = soundLevel,
                                partialTranscript = partialTranscript,
                                messages = messages,
                                isMuted = isMuted,
                                onOpenDialects = { showDialectSelector = true },
                                onExploreVoices = { selectedCategory = FuturisticCategory.LANGUAGES }
                            )
                        }

                        FuturisticCategory.INSIGHTS -> {
                            FuturisticInsightsContent(
                                viewModel = viewModel,
                                currentDialect = currentDialect,
                                currentProfile = currentProfile,
                                onOpenDialects = { showDialectSelector = true },
                                onOpenDiagnostics = { showDiagnosticsSheet = true }
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
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateToChat = {
                                    selectedCategory = FuturisticCategory.VOICE
                                    viewModel.openChatWindow()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        FuturisticCategory.CREATE -> {
                            CreateHubScreen(
                                viewModel = viewModel,
                                onNavigateCategory = { selectedCategory = it },
                                onNavigateToChat = {
                                    selectedCategory = FuturisticCategory.VOICE
                                    viewModel.openChatWindow()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        FuturisticCategory.SAVED -> {
                            SavedItemsScreen(
                                viewModel = viewModel,
                                onNavigateToChat = {
                                    selectedCategory = FuturisticCategory.VOICE
                                    viewModel.openChatWindow()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        FuturisticCategory.WRITING -> {
                            WritingStudioScreen(
                                viewModel = viewModel,
                                onNavigateToChat = {
                                    selectedCategory = FuturisticCategory.VOICE
                                    viewModel.openChatWindow()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        FuturisticCategory.DOCUMENTS -> {
                            DocumentScreen(
                                viewModel = viewModel,
                                onNavigateCategory = { selectedCategory = it },
                                onNavigateToChat = {
                                    selectedCategory = FuturisticCategory.VOICE
                                    viewModel.openChatWindow()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        FuturisticCategory.MEMORY -> {
                            MemoryScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class RegionalGreeting(
    val title: String,
    val subtitle: String,
    val hint: String,
    val quickPrompts: List<String>
)

private fun getRegionalGreeting(dialect: RegionalDialect): RegionalGreeting {
    return when (dialect.language.lowercase()) {
        "malayalam" -> RegionalGreeting(
            title = "നമസ്കാരം 👋",
            subtitle = "എന്താണ് അറിയേണ്ടത്?",
            hint = "സംസാരിക്കുകയോ ടൈപ്പ് ചെയ്യുകയോ ചെയ്യാം • Speak or type",
            quickPrompts = listOf("ഇന്നത്തെ കാലാവസ്ഥ?", "എന്താ വിശേഷം?", "കേരളത്തെക്കുറിച്ച് പറയൂ")
        )
        "tamil" -> RegionalGreeting(
            title = "வணக்கம் 👋",
            subtitle = "என்ன தெரிந்து கொள்ள வேண்டும்?",
            hint = "பேசலாம் அல்லது தட்டச்சு செய்யலாம் • Speak or type",
            quickPrompts = listOf("இன்றைய வானிலை?", "என்ன செய்தி?", "உதவி தேவை")
        )
        "kannada" -> RegionalGreeting(
            title = "ನಮಸ್ಕಾರ 👋",
            subtitle = "ಏನು ತಿಳಿಯಬೇಕು?",
            hint = "ಮಾತನಾಡಿ ಅಥವಾ ಟೈಪ್ ಮಾಡಿ • Speak or type",
            quickPrompts = listOf("ಇಂದಿನ ಹವಾಮಾನ?", "ಏನು ಸಮಾಚಾರ?", "ಸಹಾಯ ಬೇಕು")
        )
        "telugu" -> RegionalGreeting(
            title = "నమస్కారం 👋",
            subtitle = "మీకు ఏమి కావాలి?",
            hint = "మాట్లాడండి లేదా టైప్ చేయండి • Speak or type",
            quickPrompts = listOf("వాతావరణం ఎలా ఉంది?", "విశేషాలు ఏమిటి?", "సహాయం చేయగలరా?")
        )
        "hindi" -> RegionalGreeting(
            title = "नमस्ते 👋",
            subtitle = "मैं आपकी क्या मदद कर सकता हूँ?",
            hint = "बोलें या टाइप करें • Speak or type",
            quickPrompts = listOf("आज का मौसम कैसा है?", "क्या हाल है?", "कुछ नया बताओ")
        )
        else -> RegionalGreeting(
            title = "Hello 👋",
            subtitle = "What would you like to know?",
            hint = "You can speak or type your message",
            quickPrompts = listOf("What is the weather?", "How are you doing?", "Tell me about Kerala")
        )
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
    streamingAiText: String?,
    requestError: String?,
    soundLevel: Float,
    partialTranscript: String,
    messages: List<com.example.data.model.ConversationMessageEntity>,
    isMuted: Boolean,
    onOpenDialects: () -> Unit,
    onExploreVoices: () -> Unit
) {
    val rmsDb by viewModel.rmsDb.collectAsState()
    val isChatWindowOpen by viewModel.isChatWindowOpen.collectAsState()
    val chatSuggestions by viewModel.chatSuggestions.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 1. CLEAN HOME SCREEN: Minimal, spacious, uncluttered start screen
        CleanHomeScreen(
            viewModel = viewModel,
            currentDialect = currentDialect,
            voiceState = voiceState,
            soundLevel = soundLevel,
            rmsDb = rmsDb,
            isAiThinking = isAiThinking,
            isMuted = isMuted,
            partialTranscript = partialTranscript,
            hasActiveConversation = messages.isNotEmpty() || !streamingAiText.isNullOrBlank(),
            onOpenChat = { viewModel.openChatWindow() }
        )

        // 2. FLOATING CHAT WINDOW / PANEL: Modern AI Assistant floating card above Home screen
        AnimatedVisibility(
            visible = isChatWindowOpen,
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
                animationSpec = tween(240),
                initialOffsetY = { fullHeight -> (fullHeight * 0.12f).toInt() }
            ),
            exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(
                animationSpec = tween(180),
                targetOffsetY = { fullHeight -> (fullHeight * 0.12f).toInt() }
            )
        ) {
            FloatingChatWindow(
                viewModel = viewModel,
                currentDialect = currentDialect,
                messages = messages,
                voiceState = voiceState,
                isAiThinking = isAiThinking,
                streamingAiText = streamingAiText,
                requestError = requestError,
                soundLevel = soundLevel,
                rmsDb = rmsDb,
                partialTranscript = partialTranscript,
                chatSuggestions = chatSuggestions,
                isMuted = isMuted,
                onClose = { viewModel.closeChatWindow() },
                onNewChat = { viewModel.startNewConversation() }
            )
        }
    }
}

/**
 * CLEAN HOME SCREEN:
 * Spacious, modern, uncluttered Home screen to start a conversation.
 * Center shows:
 * - Friendly waving hand emoji (👋)
 * - Regional greeting (നമസ്കാരം!)
 * - Subtitle (എന്താണ് അറിയേണ്ടത്?)
 * - 3–4 compact contextual suggestion chips
 * Bottom shows:
 * - Main floating AI input bar: [ Ask anything...  🎙️ ➤ ]
 */
@Composable
private fun CleanHomeScreen(
    viewModel: VoiceViewModel,
    currentDialect: RegionalDialect,
    voiceState: VoiceState,
    soundLevel: Float,
    rmsDb: Float,
    isAiThinking: Boolean,
    isMuted: Boolean,
    partialTranscript: String,
    hasActiveConversation: Boolean,
    onOpenChat: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val greeting = remember(currentDialect.language) {
        getRegionalGreeting(currentDialect)
    }

    val suggestions = remember(currentDialect.language) {
        when (currentDialect.language.lowercase()) {
            "malayalam" -> listOf(
                "ഇന്നത്തെ കാലാവസ്ഥ",
                "ഒരു കാര്യം ചോദിക്കാം",
                "എന്നെ സഹായിക്കൂ",
                "കേരള വിശേഷങ്ങൾ"
            )
            "tamil" -> listOf(
                "இன்றைய வானிலை",
                "ஒரு கேள்வி கேட்கலாம்",
                "எனக்கு உதவுங்கள்",
                "செய்திகள் என்ன"
            )
            "kannada" -> listOf(
                "ಇಂದಿನ ಹವಾಮಾನ",
                "ಪ್ರಶ್ನೆ ಕೇಳಿ",
                "ಸಹಾಯ ಮಾಡಿ",
                "ಸಮಾಚಾರ ಏನು"
            )
            "telugu" -> listOf(
                "ఈరోజు వాతావరణం",
                "ఒక ప్రశ్న అడగండి",
                "సహాయం కావాలి",
                "విశేషాలు ఏమిటి"
            )
            "hindi" -> listOf(
                "आज का मौसम",
                "एक सवाल पूछें",
                "मेरी मदद करो",
                "कुछ नया बताओ"
            )
            else -> listOf(
                "Today's weather",
                "Ask anything",
                "Help me out",
                "About Kerala"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // =========================================================
        // CENTER: Clean Greeting + Compact Contextual Suggestions
        // =========================================================
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 👋 Big Friendly Waving Hand Emoji
            Text(
                text = "👋",
                fontSize = 46.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // നമസ്കാരം! (Large, bold, friendly greeting)
            Text(
                text = greeting.title.replace("👋", "").trim().let { if (it.endsWith("!")) it else "$it!" },
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = FuturisticTheme.primaryText,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // എന്താണ് അറിയേണ്ടത്? (Subtitle)
            Text(
                text = greeting.subtitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = FuturisticTheme.secondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 3–4 Compact Contextual Suggestion Chips (2x2 centered layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                suggestions.take(2).forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .clip(FuturisticTokens.CornerRadius.pillShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                            .clickable(enabled = !isAiThinking) {
                                viewModel.sendChatMessage(suggestion, speakResponse = !isMuted)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("home_suggestion_$suggestion")
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = FuturisticTheme.primaryText
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                suggestions.drop(2).take(2).forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .clip(FuturisticTokens.CornerRadius.pillShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                            .clickable(enabled = !isAiThinking) {
                                viewModel.sendChatMessage(suggestion, speakResponse = !isMuted)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("home_suggestion_$suggestion")
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = FuturisticTheme.primaryText
                        )
                    }
                }
            }

            // Subtle Resume conversation chip if conversation exists
            if (hasActiveConversation) {
                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .clip(FuturisticTokens.CornerRadius.pillShape)
                        .background(FuturisticTheme.surface)
                        .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.5f), FuturisticTokens.CornerRadius.pillShape)
                        .clickable { onOpenChat() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("resume_chat_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = FuturisticTheme.accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Resume conversation ↗",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FuturisticTheme.accent
                        )
                    }
                }
            }
        }

        // =========================================================
        // BOTTOM: Main Floating AI Input Bar (Clean, above gesture insets)
        // =========================================================
        val attachedImageUri by viewModel.attachedImageUri.collectAsState()
        val attachedDoc by viewModel.attachedDocument.collectAsState()

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(bottom = 12.dp)
        ) {
            // Attached Image Preview chip if selected
            ImageAttachmentPreviewChip(
                attachedImageUri = attachedImageUri,
                onRemove = { viewModel.clearAttachedImage() }
            )

            // Attached Document Preview chip if selected
            DocumentAttachmentPreviewChip(
                attachedDocument = attachedDoc,
                onRemove = { viewModel.clearAttachedDocument() }
            )

            // Live Voice Active Status / Partial Transcript indicator when listening
            if (voiceState == VoiceState.LISTENING || partialTranscript.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(FuturisticTokens.CornerRadius.pillShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.4f), FuturisticTokens.CornerRadius.pillShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (partialTranscript.isNotBlank()) "\"$partialTranscript\"" else "Listening... സംസാരിച്ചോളൂ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = FuturisticTheme.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CompactVoiceWaveBar(
                                soundLevel = soundLevel,
                                isRecording = true,
                                height = 12.dp,
                                color = FuturisticTheme.accent
                            )
                        }
                    }
                }
            }

            // Main Floating AI Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 10.dp, shape = FuturisticTokens.CornerRadius.pillShape)
                    .clip(FuturisticTokens.CornerRadius.pillShape)
                    .background(FuturisticTheme.surfaceElevated)
                    .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // [+] Attachment Option Button (Generate / Upload / Camera)
                    AttachmentOptionButton(
                        viewModel = viewModel,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = if (attachedDoc != null) "Ask about ${attachedDoc?.fileName}..."
                                else if (!attachedImageUri.isNullOrBlank()) "Ask about this image..."
                                else "Ask anything... • എന്തും ചോദിക്കാം...",
                                color = FuturisticTheme.tertiaryText,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clean_home_text_input"),
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
                            if ((textInput.isNotBlank() || !attachedImageUri.isNullOrBlank() || attachedDoc != null) && !isAiThinking) {
                                val prompt = if (textInput.isNotBlank()) textInput
                                else if (attachedDoc != null) "Summarize this document"
                                else "Explain this image"
                                viewModel.sendChatMessage(prompt, speakResponse = !isMuted)
                                textInput = ""
                            }
                        })
                    )

                    // Microphone Button (🎙️)
                    IconButton(
                        onClick = { viewModel.toggleMic() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (voiceState == VoiceState.LISTENING) Color(0xFFEF4444)
                                else FuturisticTheme.surface
                            )
                            .testTag("clean_home_mic_button")
                    ) {
                        Icon(
                            imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (voiceState == VoiceState.LISTENING) "Stop listening" else "Tap to speak",
                            tint = if (voiceState == VoiceState.LISTENING) Color.White else FuturisticTheme.accent,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Send Button (➤)
                    val canSend = (textInput.isNotBlank() || !attachedImageUri.isNullOrBlank() || attachedDoc != null) && !isAiThinking
                    IconButton(
                        onClick = {
                            if (canSend) {
                                val prompt = if (textInput.isNotBlank()) textInput
                                else if (attachedDoc != null) "Summarize this document"
                                else "Explain this image"
                                viewModel.sendChatMessage(prompt, speakResponse = !isMuted)
                                textInput = ""
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (canSend) FuturisticTheme.primaryButton
                                else FuturisticTheme.surface
                            )
                            .testTag("clean_home_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (canSend) FuturisticTheme.primaryButtonText else FuturisticTheme.tertiaryText,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * FLOATING CHAT WINDOW:
 * Modern floating assistant panel above the Home screen.
 * Contains:
 * - Rounded corners & subtle elevation shadow
 * - Top header with AI Assistant title, regional dialect indicator, New Chat button, and Close/Minimize button (✕)
 * - Scrollable message thread with message timestamps
 * - Always-available AI suggestions row right above the composer
 * - Bottom input composer with text input, microphone, and send button
 */
@Composable
private fun FloatingChatWindow(
    viewModel: VoiceViewModel,
    currentDialect: RegionalDialect,
    messages: List<com.example.data.model.ConversationMessageEntity>,
    voiceState: VoiceState,
    isAiThinking: Boolean,
    streamingAiText: String?,
    requestError: String?,
    soundLevel: Float,
    rmsDb: Float,
    partialTranscript: String,
    chatSuggestions: List<String>,
    isMuted: Boolean,
    onClose: () -> Unit,
    onNewChat: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val attachedImageUri by viewModel.attachedImageUri.collectAsState()
    val attachedDoc by viewModel.attachedDocument.collectAsState()
    val isImageGenerating by viewModel.isImageGenerating.collectAsState()
    val imageLoadingMessage by viewModel.imageLoadingMessage.collectAsState()

    val isVoiceActive = voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING || isAiThinking || isImageGenerating

    LaunchedEffect(messages.size, isAiThinking, partialTranscript, streamingAiText, isImageGenerating) {
        val totalItems = messages.size + (if (isAiThinking || !streamingAiText.isNullOrBlank() || isImageGenerating) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, RoundedCornerShape(24.dp))
        ) {
            // 1. FLOATING CHAT HEADER: Title, Active Dialect, New Chat, Close/Minimize
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FuturisticTheme.surface)
                    .border(
                        width = 1.dp,
                        color = FuturisticTheme.border.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FuturisticTheme.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = FuturisticTheme.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Assistant",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FuturisticTheme.primaryText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        }
                        Text(
                            text = "${currentDialect.cityOrArea} • ${currentDialect.language}",
                            fontSize = 11.sp,
                            color = FuturisticTheme.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // New Chat Action
                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .testTag("floating_new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = FuturisticTheme.primaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close / Minimize Action (✕)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .testTag("floating_chat_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close chat window",
                            tint = FuturisticTheme.primaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. SCROLLABLE MESSAGE THREAD
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isAiThinking && streamingAiText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Start the conversation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FuturisticTheme.primaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Type below or choose a suggestion to begin",
                                fontSize = 13.sp,
                                color = FuturisticTheme.secondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            val isUser = msg.role == "user"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 310.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 18.dp,
                                                topEnd = 18.dp,
                                                bottomStart = if (isUser) 18.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 18.dp
                                            )
                                        )
                                        .background(
                                            if (isUser) FuturisticTheme.primaryButton else FuturisticTheme.surface
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isUser) Color.Transparent else FuturisticTheme.border,
                                            shape = RoundedCornerShape(
                                                topStart = 18.dp,
                                                topEnd = 18.dp,
                                                bottomStart = if (isUser) 18.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 18.dp
                                            )
                                        )
                                        .padding(12.dp)
                                ) {
                                    val timeFormatted = remember(msg.timestamp) {
                                        com.example.data.datetime.DateTimeService.formatMessageTime(context, msg.timestamp)
                                    }
                                    Column {
                                        // 0. Attached document badge if present
                                        if (!msg.documentName.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(bottom = 6.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isUser) Color.White.copy(alpha = 0.2f) else FuturisticTheme.surface)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Description,
                                                        contentDescription = null,
                                                        tint = if (isUser) Color.White else FuturisticTheme.accent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = msg.documentName,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isUser) Color.White else FuturisticTheme.primaryText
                                                    )
                                                }
                                            }
                                        }

                                        // 1. Attached image preview for user message
                                        if (isUser && !msg.attachedImageUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(File(msg.attachedImageUri))
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "User attached image",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                            if (msg.text.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                            }
                                        }

                                        // 2. Generated image card for assistant message
                                        if (!isUser && msg.isImageGeneration && !msg.imageUri.isNullOrBlank()) {
                                            GeneratedImageMessageCard(
                                                msg = msg,
                                                viewModel = viewModel
                                            )
                                            if (msg.text.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                            }
                                        }

                                        if (msg.text.isNotBlank()) {
                                            Text(
                                                text = msg.text,
                                                color = if (isUser) FuturisticTheme.primaryButtonText else FuturisticTheme.primaryText,
                                                fontSize = 14.sp,
                                                lineHeight = 21.sp
                                            )
                                        }

                                        if (isUser) {
                                            if (timeFormatted.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = timeFormatted,
                                                    fontSize = 10.sp,
                                                    color = FuturisticTheme.primaryButtonText.copy(alpha = 0.75f),
                                                    modifier = Modifier.align(Alignment.End)
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = { viewModel.speakText(msg.text) },
                                                        modifier = Modifier.size(22.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                            contentDescription = "Speak",
                                                            tint = FuturisticTheme.accent,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString(msg.text))
                                                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(22.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ContentCopy,
                                                            contentDescription = "Copy",
                                                            tint = FuturisticTheme.secondaryText,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                    if (msg.isRealTimeKnowledge) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(FuturisticTokens.CornerRadius.pillShape)
                                                                .background(Color(0xFFDCFCE7))
                                                                .clickable { viewModel.showKnowledgeDetailsForMessage(msg) }
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFF16A34A),
                                                                    modifier = Modifier.size(9.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                Text(
                                                                    text = "Verified",
                                                                    fontSize = 9.sp,
                                                                    color = Color(0xFF16A34A),
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                if (timeFormatted.isNotBlank()) {
                                                    Text(
                                                        text = timeFormatted,
                                                        fontSize = 10.sp,
                                                        color = FuturisticTheme.secondaryText.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Image Generating indicator
                        if (isImageGenerating) {
                            item(key = "ai_image_generating_state") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 300.dp)
                                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                                            .background(FuturisticTheme.surface)
                                            .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.5f), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = FuturisticTheme.accent
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = imageLoadingMessage,
                                                    color = FuturisticTheme.primaryText,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Creating visual artwork with AI...",
                                                    color = FuturisticTheme.secondaryText,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Thinking indicator
                        if (isAiThinking && streamingAiText.isNullOrBlank() && !isImageGenerating) {
                            item(key = "ai_thinking_state") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                                            .background(FuturisticTheme.surface)
                                            .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.4f), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(15.dp),
                                                strokeWidth = 2.dp,
                                                color = FuturisticTheme.accent
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Thinking...",
                                                color = FuturisticTheme.secondaryText,
                                                fontSize = 13.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Real-time Streaming bubble
                        if (!streamingAiText.isNullOrBlank()) {
                            item(key = "ai_streaming_state") {
                                val streamTimeFormatted = remember {
                                    com.example.data.datetime.DateTimeService.formatMessageTime(context, System.currentTimeMillis())
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 310.dp)
                                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                                            .background(FuturisticTheme.surface)
                                            .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.6f), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = streamingAiText ?: "",
                                                color = FuturisticTheme.primaryText,
                                                fontSize = 14.sp,
                                                lineHeight = 21.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(FuturisticTheme.accent)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Streaming...",
                                                        fontSize = 10.sp,
                                                        color = FuturisticTheme.accent,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                if (streamTimeFormatted.isNotBlank()) {
                                                    Text(
                                                        text = streamTimeFormatted,
                                                        fontSize = 10.sp,
                                                        color = FuturisticTheme.secondaryText.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Error Banner with Retry
                        if (requestError != null) {
                            item(key = "ai_request_error") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                                            text = requestError ?: "Error occurred",
                                            color = Color(0xFF991B1B),
                                            fontSize = 12.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(FuturisticTokens.CornerRadius.pillShape)
                                                .background(Color(0xFFDC2626))
                                                .clickable(enabled = !isAiThinking) {
                                                    viewModel.retryLastFailedMessage()
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "Retry",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live voice active status bar (when listening/speaking)
            if (isVoiceActive || partialTranscript.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(FuturisticTokens.CornerRadius.pillShape)
                            .background(FuturisticTheme.surface)
                            .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.4f), FuturisticTokens.CornerRadius.pillShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = FuturisticTheme.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CompactVoiceWaveBar(
                                soundLevel = soundLevel,
                                isRecording = voiceState == VoiceState.SPEAKING || voiceState == VoiceState.LISTENING,
                                height = 14.dp,
                                color = FuturisticTheme.accent
                            )
                        }
                    }
                }
            }

            // 3. ALWAYS-AVAILABLE SUGGESTIONS ROW (Directly above composer)
            if (chatSuggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FuturisticTheme.surfaceElevated)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    chatSuggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(FuturisticTokens.CornerRadius.pillShape)
                                .background(FuturisticTheme.surface)
                                .border(1.dp, FuturisticTheme.border.copy(alpha = 0.8f), FuturisticTokens.CornerRadius.pillShape)
                            .clickable(enabled = !isAiThinking) {
                                viewModel.onSuggestionClicked(suggestion)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("chat_suggestion_$suggestion")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = FuturisticTheme.accent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = suggestion,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FuturisticTheme.primaryText
                                )
                            }
                        }
                    }
                }
            }

            // 4. FLOATING CHAT COMPOSER: Attachment, Text input, Mic, Send
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FuturisticTheme.surface)
                    .border(
                        width = 1.dp,
                        color = FuturisticTheme.border.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    // Image Attachment Preview chip if selected
                    ImageAttachmentPreviewChip(
                        attachedImageUri = attachedImageUri,
                        onRemove = { viewModel.clearAttachedImage() }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FuturisticTokens.CornerRadius.pillShape)
                            .background(FuturisticTheme.surfaceElevated)
                            .border(1.dp, FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // [+] Attachment Option Button (Generate / Upload / Camera)
                        AttachmentOptionButton(
                            viewModel = viewModel,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        val canSend = (textInput.isNotBlank() || !attachedImageUri.isNullOrBlank()) && !isAiThinking && !isImageGenerating

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = if (!attachedImageUri.isNullOrBlank()) "Ask about this image..." else "Type a message...",
                                    color = FuturisticTheme.tertiaryText,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("floating_chat_text_input"),
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
                                if (canSend) {
                                    viewModel.sendChatMessage(if (textInput.isNotBlank()) textInput else "Explain this image", speakResponse = !isMuted)
                                    textInput = ""
                                }
                            })
                        )

                        // Microphone Button
                        IconButton(
                            onClick = { viewModel.toggleMic() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (voiceState == VoiceState.LISTENING) Color(0xFFEF4444) else FuturisticTheme.surface
                                )
                                .testTag("floating_composer_mic_btn")
                        ) {
                            Icon(
                                imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (voiceState == VoiceState.LISTENING) "Stop listening" else "Start listening",
                                tint = if (voiceState == VoiceState.LISTENING) Color.White else FuturisticTheme.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Send Button
                        IconButton(
                            onClick = {
                                if (canSend) {
                                    viewModel.sendChatMessage(if (textInput.isNotBlank()) textInput else "Explain this image", speakResponse = !isMuted)
                                    textInput = ""
                                }
                            },
                            enabled = canSend,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSend) FuturisticTheme.primaryButton else FuturisticTheme.surface
                                )
                                .testTag("floating_composer_send_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) FuturisticTheme.primaryButtonText else FuturisticTheme.secondaryText.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * INSIGHTS Category Content.
 * Relocated regional analytics, vocabulary statistics, cadence slider, and acoustic diagnostics.
 */
@Composable
private fun FuturisticInsightsContent(
    viewModel: VoiceViewModel,
    currentDialect: RegionalDialect,
    currentProfile: com.example.data.model.VoiceProfileEntity?,
    onOpenDialects: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val scrollState = rememberScrollState()
    val strength = currentProfile?.regionalStrength ?: 0.75f
    val messages by viewModel.messages.collectAsState()
    val rmsDb by viewModel.rmsDb.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section Header
        Text(
            text = "Dialect & Voice Insights",
            color = FuturisticTheme.primaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Linguistic analytics, vocabulary metrics & speech cadence for ${currentDialect.dialectName}",
            color = FuturisticTheme.secondaryText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Regional Analytics & Fluency Metrics Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentDialect.flagEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentDialect.dialectName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FuturisticTheme.primaryText
                            )
                            Text(
                                text = "${currentDialect.cityOrArea} • ${currentDialect.country}",
                                fontSize = 13.sp,
                                color = FuturisticTheme.secondaryText
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(FuturisticTokens.CornerRadius.pillShape)
                            .background(FuturisticTheme.accent.copy(alpha = 0.15f))
                            .clickable(onClick = onOpenDialects)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Change",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FuturisticTheme.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Key metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricPill(title = "Vocabulary", value = "1,450+ words", subtitle = "Local lexicon")
                    MetricPill(title = "Slang Depth", value = "${(strength * 100).toInt()}%", subtitle = "Dialect intensity")
                    MetricPill(title = "Saved Messages", value = "${messages.size}", subtitle = "Local DB")
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Cadence & Regional Slang Strength Tuning
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
                    text = "Speaking Style Cadence",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FuturisticTheme.primaryText
                )
                Text(
                    text = "Current strength: ${(strength * 100).toInt()}%",
                    fontSize = 13.sp,
                    color = FuturisticTheme.accent
                )
                Spacer(modifier = Modifier.height(10.dp))
                Slider(
                    value = strength,
                    onValueChange = { viewModel.updateRegionalStrength(it) },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = FuturisticTheme.accent,
                        activeTrackColor = FuturisticTheme.accent,
                        inactiveTrackColor = FuturisticTheme.border
                    )
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

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Featured Regional Slang & Expressions
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
                    Text(
                        text = "Authentic Slang Expressions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText
                    )
                    Text(
                        text = "Tap to speak",
                        fontSize = 12.sp,
                        color = FuturisticTheme.secondaryText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentDialect.typicalExpressions.forEach { expr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(FuturisticTokens.CornerRadius.smallShape)
                                .background(FuturisticTheme.surface)
                                .clickable {
                                    viewModel.speakText(expr.expression)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = expr.expression,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FuturisticTheme.primaryText
                                )
                                Text(
                                    text = expr.meaning,
                                    fontSize = 12.sp,
                                    color = FuturisticTheme.secondaryText
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Hear expression",
                                tint = FuturisticTheme.accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Acoustic Telemetry & Diagnostics
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
                    Text(
                        text = "Acoustic Telemetry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText
                    )
                    IconButton(
                        onClick = onOpenDiagnostics,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Deep diagnostics",
                            tint = FuturisticTheme.accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("RMS Decibel Level", fontSize = 13.sp, color = FuturisticTheme.secondaryText)
                    Text(String.format(java.util.Locale.US, "%.1f dB", rmsDb), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FuturisticTheme.primaryText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Model Engine", fontSize = 13.sp, color = FuturisticTheme.secondaryText)
                    Text("Android Native + Gemini", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricPill(title: String, value: String, subtitle: String) {
    Column(
        modifier = Modifier
            .clip(FuturisticTokens.CornerRadius.mediumShape)
            .background(FuturisticTheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = title, fontSize = 11.sp, color = FuturisticTheme.secondaryText)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FuturisticTheme.primaryText)
        Text(text = subtitle, fontSize = 10.sp, color = FuturisticTheme.tertiaryText)
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
                        val timeFormatted = remember(msg.timestamp) {
                            com.example.data.datetime.DateTimeService.formatMessageTime(context, msg.timestamp)
                        }
                        Column {
                            Text(
                                text = msg.text,
                                color = if (isUser) FuturisticTheme.primaryButtonText else FuturisticTheme.primaryText,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )

                            if (isUser) {
                                if (timeFormatted.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = timeFormatted,
                                        fontSize = 11.sp,
                                        color = FuturisticTheme.primaryButtonText.copy(alpha = 0.75f),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                    if (timeFormatted.isNotBlank()) {
                                        Text(
                                            text = timeFormatted,
                                            fontSize = 11.sp,
                                            color = FuturisticTheme.secondaryText.copy(alpha = 0.8f)
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

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationMessageEntity
import com.example.data.model.ConversationSessionEntity
import com.example.data.repository.DialectCatalog
import com.example.ui.VoiceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class RoleFilter(val label: String) {
    ALL("All Messages"),
    USER("You"),
    ASSISTANT("Voice AI")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: VoiceViewModel,
    onNavigateToChat: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val allSessions by viewModel.allSessions.collectAsState()
    val archivedSessions by viewModel.archivedSessions.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val allMessages by viewModel.allPersistedMessages.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Messages (Room DB), 1: Conversations (Sessions)
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf(RoleFilter.ALL) }
    var showArchivedSessionsTab by remember { mutableStateOf(false) }

    var sessionToRename by remember { mutableStateOf<ConversationSessionEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<ConversationSessionEntity?>(null) }
    var showClearAllMessagesDialog by remember { mutableStateOf(false) }

    // Filter messages for Tab 0
    val filteredMessages = remember(allMessages, searchQuery, selectedRoleFilter) {
        allMessages.filter { msg ->
            val matchesRole = when (selectedRoleFilter) {
                RoleFilter.ALL -> true
                RoleFilter.USER -> msg.role == "user"
                RoleFilter.ASSISTANT -> msg.role == "assistant"
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                msg.text.contains(searchQuery, ignoreCase = true) ||
                        msg.dialectId.contains(searchQuery, ignoreCase = true) ||
                        msg.highlightedSlangCsv.contains(searchQuery, ignoreCase = true)
            }
            matchesRole && matchesQuery
        }
    }

    // Filter sessions for Tab 1
    val currentSourceList = if (showArchivedSessionsTab) archivedSessions else allSessions
    val filteredSessions = remember(currentSourceList, searchQuery) {
        if (searchQuery.isBlank()) {
            currentSourceList
        } else {
            currentSourceList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessagePreview.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val pinnedSessions = remember(filteredSessions, showArchivedSessionsTab) {
        if (showArchivedSessionsTab) emptyList() else filteredSessions.filter { it.isPinned }
    }

    val unpinnedSessions = remember(filteredSessions, showArchivedSessionsTab) {
        if (showArchivedSessionsTab) emptyList() else filteredSessions.filter { !it.isPinned }
    }

    val dateGroups = remember(unpinnedSessions) {
        groupSessionsByDate(unpinnedSessions)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Chat History",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Local Room Database (${allMessages.size} messages)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (selectedTab == 0 && allMessages.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearAllMessagesDialog = true },
                            modifier = Modifier.testTag("history_clear_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear All Messages",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.startNewConversation()
                            onNavigateToChat()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("history_new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.startNewConversation()
                    onNavigateToChat()
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "New Conversation") },
                text = { Text("Start New Chat") },
                modifier = Modifier.testTag("history_fab_new_chat")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // View Mode Tab Row: "All Messages (Room DB)" vs "Conversations"
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        searchQuery = ""
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All Messages (${allMessages.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_all_messages")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        searchQuery = ""
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Conversations (${allSessions.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_conversations")
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("history_search_input"),
                placeholder = {
                    Text(
                        if (selectedTab == 0) "Search all messages in Room database..."
                        else "Search conversations..."
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (selectedTab == 0) {
                // =========================================================
                // TAB 0: ALL PERSISTED MESSAGES SCROLLABLE LIST VIEW
                // =========================================================

                // Role Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleFilter.entries.forEach { role ->
                        FilterChip(
                            selected = selectedRoleFilter == role,
                            onClick = { selectedRoleFilter = role },
                            label = { Text(role.label) },
                            modifier = Modifier.testTag("filter_role_${role.name.lowercase()}")
                        )
                    }
                }

                if (filteredMessages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "No messages match '$searchQuery'"
                                } else if (allMessages.isEmpty()) {
                                    "No messages in Room database yet"
                                } else {
                                    "No messages for this filter"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (allMessages.isEmpty()) {
                                    "Talk or type to your regional AI. All messages will be automatically stored locally in SQLite via Room."
                                } else {
                                    "Try changing search terms or switching the role filter above."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Scrollable list view of persisted messages
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("all_messages_history_list"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredMessages, key = { it.id }) { msg ->
                            PersistedMessageHistoryCard(
                                message = msg,
                                onPlay = { viewModel.speakText(msg.text) },
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(msg.text))
                                    Toast.makeText(context, "Copied message", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = { viewModel.deleteMessage(msg.id) },
                                onOpenInChat = {
                                    // Switch active session / dialect and navigate to chat
                                    if (msg.conversationId.isNotBlank()) {
                                        val session = allSessions.find { it.id == msg.conversationId }
                                            ?: archivedSessions.find { it.id == msg.conversationId }
                                        if (session != null) {
                                            viewModel.selectConversation(session)
                                        } else {
                                            viewModel.loadMessagesForSession(msg.conversationId)
                                        }
                                    }
                                    onNavigateToChat()
                                }
                            )
                        }
                    }
                }
            } else {
                // =========================================================
                // TAB 1: CONVERSATION SESSIONS LIST
                // =========================================================

                // Category Filter Chips (Recent vs Archived)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showArchivedSessionsTab,
                        onClick = { showArchivedSessionsTab = false },
                        label = { Text("Recent (${allSessions.size})") },
                        modifier = Modifier.testTag("tab_recent_conversations")
                    )
                    FilterChip(
                        selected = showArchivedSessionsTab,
                        onClick = { showArchivedSessionsTab = true },
                        label = { Text("Archived (${archivedSessions.size})") },
                        modifier = Modifier.testTag("tab_archived_conversations")
                    )
                }

                if (filteredSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "No matching conversations found"
                                } else if (showArchivedSessionsTab) {
                                    "No archived conversations"
                                } else {
                                    "No conversations yet"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "Try searching different words or titles"
                                } else if (showArchivedSessionsTab) {
                                    "Conversations you archive will appear here"
                                } else {
                                    "Tap 'Start New Chat' to begin speaking with your regional AI"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("conversation_sessions_list"),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        if (showArchivedSessionsTab) {
                            // Archived List
                            item {
                                SectionHeader(title = "Archived Conversations", icon = Icons.Default.Archive)
                            }
                            items(filteredSessions, key = { it.id }) { session ->
                                ConversationHistoryItem(
                                    session = session,
                                    isActive = activeSession?.id == session.id,
                                    onClick = {
                                        viewModel.selectConversation(session)
                                        onNavigateToChat()
                                    },
                                    onTogglePin = { viewModel.togglePinConversation(session) },
                                    onRename = {
                                        sessionToRename = session
                                        renameText = session.title
                                    },
                                    onArchive = { viewModel.archiveConversation(session) },
                                    onUnarchive = { viewModel.unarchiveConversation(session) },
                                    onDelete = { sessionToDelete = session }
                                )
                            }
                        } else {
                            // Pinned Section
                            if (pinnedSessions.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "Pinned", icon = Icons.Default.PushPin)
                                }
                                items(pinnedSessions, key = { it.id }) { session ->
                                    ConversationHistoryItem(
                                        session = session,
                                        isActive = activeSession?.id == session.id,
                                        onClick = {
                                            viewModel.selectConversation(session)
                                            onNavigateToChat()
                                        },
                                        onTogglePin = { viewModel.togglePinConversation(session) },
                                        onRename = {
                                            sessionToRename = session
                                            renameText = session.title
                                        },
                                        onArchive = { viewModel.archiveConversation(session) },
                                        onUnarchive = { viewModel.unarchiveConversation(session) },
                                        onDelete = { sessionToDelete = session }
                                    )
                                }
                            }

                            // Date Groups (Today, Yesterday, Previous 7 Days, Older)
                            dateGroups.forEach { (header, sessions) ->
                                if (sessions.isNotEmpty()) {
                                    item {
                                        SectionHeader(title = header)
                                    }
                                    items(sessions, key = { it.id }) { session ->
                                        ConversationHistoryItem(
                                            session = session,
                                            isActive = activeSession?.id == session.id,
                                            onClick = {
                                                viewModel.selectConversation(session)
                                                onNavigateToChat()
                                            },
                                            onTogglePin = { viewModel.togglePinConversation(session) },
                                            onRename = {
                                                sessionToRename = session
                                                renameText = session.title
                                            },
                                            onArchive = { viewModel.archiveConversation(session) },
                                            onUnarchive = { viewModel.unarchiveConversation(session) },
                                            onDelete = { sessionToDelete = session }
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

    // Rename Dialog
    sessionToRename?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text("Rename Conversation") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Conversation Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input_field")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameConversation(session.id, renameText.trim())
                        }
                        sessionToRename = null
                    },
                    modifier = Modifier.testTag("confirm_rename_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Session Confirmation Dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Conversation?") },
            text = { Text("This will permanently remove '${session.title}' and all its persisted messages from the Room database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConversation(session.id)
                        sessionToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Messages Confirmation Dialog
    if (showClearAllMessagesDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllMessagesDialog = false },
            title = { Text("Clear All Messages?") },
            text = { Text("This will permanently remove all ${allMessages.size} chat messages stored in the local Room database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllChatHistory()
                        showClearAllMessagesDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_all_messages_button")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllMessagesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Card displaying an individual persisted chat message in the scrollable History list.
 */
@Composable
private fun PersistedMessageHistoryCard(
    message: ConversationMessageEntity,
    onPlay: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onOpenInChat: () -> Unit
) {
    val isUser = message.role == "user"
    val dialect = remember(message.dialectId) {
        DialectCatalog.getDialectById(message.dialectId)
    }
    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("persisted_message_card_${message.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Role Badge + Dialect Badge + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUser) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUser) Icons.Default.Person else Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = if (isUser) "You" else "${dialect.cityOrArea} AI",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = dialect.dialectName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message Text
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Real-Time Grounding source indicator if present
            if (!isUser && message.isRealTimeKnowledge) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified Knowledge • ${message.knowledgeTimestamp.ifBlank { "Real-time" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action row
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Play Audio
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("play_history_message_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play audio",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Copy
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_history_message_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Delete from Room
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_history_message_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete from database",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }

                // Open in Chat button
                TextButton(
                    onClick = onOpenInChat,
                    modifier = Modifier.testTag("open_in_chat_${message.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Open in Chat",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ConversationHistoryItem(
    session: ConversationSessionEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dialect = DialectCatalog.getDialectById(session.dialectId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("session_item_${session.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (session.isPinned && !session.isArchived) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (session.lastMessagePreview.isNotBlank()) {
                    Text(
                        text = session.lastMessagePreview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = dialect.dialectName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (session.messageCount > 0) {
                        Text(
                            text = "${session.messageCount} msgs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Text(
                        text = formatRelativeTime(session.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options"
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (!session.isArchived) {
                        DropdownMenuItem(
                            text = { Text(if (session.isPinned) "Unpin" else "Pin") },
                            leadingIcon = {
                                Icon(
                                    if (session.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onTogglePin()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    if (session.isArchived) {
                        DropdownMenuItem(
                            text = { Text("Unarchive") },
                            leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onUnarchive()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onArchive()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

private fun groupSessionsByDate(sessions: List<ConversationSessionEntity>): Map<String, List<ConversationSessionEntity>> {
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterdayStart = todayStart - (24 * 60 * 60 * 1000L)
    val past7DaysStart = todayStart - (7 * 24 * 60 * 60 * 1000L)

    val today = mutableListOf<ConversationSessionEntity>()
    val yesterday = mutableListOf<ConversationSessionEntity>()
    val past7Days = mutableListOf<ConversationSessionEntity>()
    val older = mutableListOf<ConversationSessionEntity>()

    sessions.forEach { s ->
        when {
            s.updatedAt >= todayStart -> today.add(s)
            s.updatedAt >= yesterdayStart -> yesterday.add(s)
            s.updatedAt >= past7DaysStart -> past7Days.add(s)
            else -> older.add(s)
        }
    }

    return mapOf(
        "Today" to today,
        "Yesterday" to yesterday,
        "Previous 7 Days" to past7Days,
        "Older" to older
    )
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / (60 * 1000L)
    val hours = diff / (60 * 60 * 1000L)
    val days = diff / (24 * 60 * 60 * 1000L)

    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "${mins}m ago"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.datetime.DateTimeService
import com.example.data.model.SavedItemEntity
import com.example.ui.VoiceViewModel
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens
import java.io.File

@Composable
fun SavedItemsScreen(
    viewModel: VoiceViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedItems by viewModel.allSavedItems.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf(
        "ALL" to "⭐ All",
        "ANSWER" to "💡 Answers",
        "IMAGE" to "🖼️ Images",
        "WRITING" to "✍️ Writing",
        "DOCUMENT" to "📄 Documents"
    )

    val filteredItems = remember(savedItems, selectedFilter) {
        if (selectedFilter == "ALL") savedItems
        else savedItems.filter { it.itemType.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FuturisticTheme.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FuturisticTheme.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = FuturisticTheme.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Saved Items",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FuturisticTheme.primaryText
                )
                Text(
                    text = "Quick access to your saved answers, images, and notes",
                    fontSize = 12.sp,
                    color = FuturisticTheme.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { (typeKey, label) ->
                val isSelected = selectedFilter == typeKey
                Box(
                    modifier = Modifier
                        .clip(FuturisticTokens.CornerRadius.pillShape)
                        .background(if (isSelected) FuturisticTheme.accent else FuturisticTheme.surfaceElevated)
                        .border(1.dp, if (isSelected) FuturisticTheme.accent else FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                        .clickable { selectedFilter = typeKey }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("saved_filter_$typeKey")
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else FuturisticTheme.primaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = FuturisticTheme.border,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No saved items yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = FuturisticTheme.secondaryText
                    )
                    Text(
                        text = "Tap the star icon on any AI answer or image to save it here",
                        fontSize = 12.sp,
                        color = FuturisticTheme.tertiaryText
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    SavedItemCard(
                        item = item,
                        onDelete = { viewModel.deleteSavedItem(item.id) },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, item.content)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Saved Item"))
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Saved Item", item.content))
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onOpenChat = {
                            viewModel.sendChatMessage("Let's continue on this:\n\n${item.content}")
                            onNavigateToChat()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedItemCard(
    item: SavedItemEntity,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formattedDate = remember(item.timestamp) {
        DateTimeService.formatDateMalayalamOrEnglish(item.timestamp, false)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FuturisticTheme.surfaceElevated)
            .border(1.dp, FuturisticTheme.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header with badge and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FuturisticTheme.accent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.itemType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuturisticTheme.accent
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = FuturisticTheme.tertiaryText
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = FuturisticTheme.secondaryText, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = FuturisticTheme.secondaryText, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onOpenChat, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = FuturisticTheme.accent, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FuturisticTheme.secondaryText, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Image if present
            if (!item.mediaUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(item.mediaUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Title
            if (item.title.isNotBlank()) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FuturisticTheme.primaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Content preview
            if (item.content.isNotBlank()) {
                Text(
                    text = item.content,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = FuturisticTheme.secondaryText,
                    maxLines = 6
                )
            }
        }
    }
}

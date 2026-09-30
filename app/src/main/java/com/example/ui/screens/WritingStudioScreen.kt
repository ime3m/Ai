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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.writing.WritingResult
import com.example.data.writing.WritingTone
import com.example.data.writing.WritingType
import com.example.ui.VoiceViewModel
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens
import kotlinx.coroutines.launch

@Composable
fun WritingStudioScreen(
    viewModel: VoiceViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dialect by viewModel.currentDialect.collectAsState()

    var selectedType by remember { mutableStateOf(WritingType.EMAIL) }
    var selectedTone by remember { mutableStateOf(WritingTone.FORMAL) }
    var userPrompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf<String?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FuturisticTheme.surface)
            .verticalScroll(scrollState)
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
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = FuturisticTheme.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Writing Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FuturisticTheme.primaryText
                )
                Text(
                    text = "Craft high-quality emails, messages & documents effortlessly",
                    fontSize = 12.sp,
                    color = FuturisticTheme.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 1. Template Selector (Horizontal Pills)
        Text(
            text = "Select Writing Type",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = FuturisticTheme.primaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WritingType.entries.forEach { type ->
                val isSelected = type == selectedType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) FuturisticTheme.accent else FuturisticTheme.surfaceElevated)
                        .border(1.dp, if (isSelected) FuturisticTheme.accent else FuturisticTheme.border, RoundedCornerShape(12.dp))
                        .clickable { selectedType = type }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("writing_type_${type.name}")
                ) {
                    Text(
                        text = "${type.iconEmoji} ${type.title}",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else FuturisticTheme.primaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Desired Tone (Horizontal Pills)
        Text(
            text = "Desired Tone",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = FuturisticTheme.primaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WritingTone.entries.forEach { tone ->
                val isSelected = tone == selectedTone
                Box(
                    modifier = Modifier
                        .clip(FuturisticTokens.CornerRadius.pillShape)
                        .background(if (isSelected) FuturisticTheme.accent.copy(alpha = 0.2f) else FuturisticTheme.surfaceElevated)
                        .border(1.dp, if (isSelected) FuturisticTheme.accent else FuturisticTheme.border, FuturisticTokens.CornerRadius.pillShape)
                        .clickable { selectedTone = tone }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("writing_tone_${tone.name}")
                ) {
                    Text(
                        text = tone.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) FuturisticTheme.accent else FuturisticTheme.secondaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. User Requirements / Topic
        Text(
            text = "Your Request / Details",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = FuturisticTheme.primaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = userPrompt,
            onValueChange = { userPrompt = it },
            placeholder = {
                Text(
                    text = selectedType.placeholder,
                    fontSize = 13.sp,
                    color = FuturisticTheme.tertiaryText
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("writing_prompt_input"),
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FuturisticTheme.accent,
                unfocusedBorderColor = FuturisticTheme.border,
                focusedTextColor = FuturisticTheme.primaryText,
                unfocusedTextColor = FuturisticTheme.primaryText,
                focusedContainerColor = FuturisticTheme.surfaceElevated,
                unfocusedContainerColor = FuturisticTheme.surfaceElevated
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Generate Button
        Button(
            onClick = {
                if (userPrompt.isNotBlank() && !isGenerating) {
                    isGenerating = true
                    coroutineScope.launch {
                        val result = viewModel.writingService.generateWriting(
                            type = selectedType,
                            tone = selectedTone,
                            userPrompt = userPrompt,
                            dialect = dialect
                        )
                        when (result) {
                            is WritingResult.Success -> {
                                generatedResult = result.outputText
                                isSaved = false
                            }
                            is WritingResult.Error -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                        isGenerating = false
                    }
                }
            },
            enabled = userPrompt.isNotBlank() && !isGenerating,
            colors = ButtonDefaults.buttonColors(
                containerColor = FuturisticTheme.accent,
                contentColor = Color.White
            ),
            shape = FuturisticTokens.CornerRadius.pillShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_generate_writing")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crafting your writing...", fontSize = 14.sp)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Writing", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 5. Result Section
        if (generatedResult != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FuturisticTheme.surfaceElevated)
                    .border(1.dp, FuturisticTheme.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedType.iconEmoji} Result",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuturisticTheme.accent
                        )

                        // Action Buttons: Copy, Share, Save, Chat
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Copy
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("AI Writing", generatedResult))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp).testTag("btn_copy_writing")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = FuturisticTheme.secondaryText, modifier = Modifier.size(18.dp))
                            }

                            // Share
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, generatedResult)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Writing"))
                                },
                                modifier = Modifier.size(32.dp).testTag("btn_share_writing")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = FuturisticTheme.secondaryText, modifier = Modifier.size(18.dp))
                            }

                            // Save
                            IconButton(
                                onClick = {
                                    viewModel.saveItem(
                                        title = "${selectedType.title}: ${userPrompt.take(30)}",
                                        content = generatedResult ?: "",
                                        itemType = "WRITING",
                                        prompt = userPrompt
                                    )
                                    isSaved = true
                                    Toast.makeText(context, "Saved to ⭐ Saved Items!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp).testTag("btn_save_writing")
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Save",
                                    tint = if (isSaved) FuturisticTheme.accent else FuturisticTheme.secondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Open in Chat
                            IconButton(
                                onClick = {
                                    viewModel.sendChatMessage("Continue working on this ${selectedType.title}:\n\n$generatedResult")
                                    onNavigateToChat()
                                },
                                modifier = Modifier.size(32.dp).testTag("btn_chat_writing")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Refine in Chat", tint = FuturisticTheme.accent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = generatedResult!!,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = FuturisticTheme.primaryText
                    )
                }
            }
        }
    }
}

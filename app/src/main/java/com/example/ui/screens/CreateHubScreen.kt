package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.document.DocumentProcessingManager
import com.example.data.image.ImageStorageManager
import com.example.ui.VoiceViewModel
import com.example.ui.components.futuristic.FuturisticCategory
import com.example.ui.components.image.GenerateImagePromptDialog
import com.example.ui.theme.FuturisticTheme
import kotlinx.coroutines.launch

@Composable
fun CreateHubScreen(
    viewModel: VoiceViewModel,
    onNavigateCategory: (FuturisticCategory) -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showGenerateDialog by remember { mutableStateOf(false) }

    // Image Picker for image editing or vision
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val cached = ImageStorageManager.processAndCacheImage(context, uri)
                if (cached != null) {
                    viewModel.attachImage(cached)
                    onNavigateCategory(FuturisticCategory.VOICE)
                    viewModel.openChatWindow()
                }
            }
        }
    }

    // Document Picker
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val doc = DocumentProcessingManager.processDocumentUri(context, uri)
                if (doc != null) {
                    viewModel.attachDocument(doc)
                    onNavigateCategory(FuturisticCategory.VOICE)
                    viewModel.openChatWindow()
                } else {
                    Toast.makeText(context, "Could not open document", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showGenerateDialog) {
        GenerateImagePromptDialog(
            onDismiss = { showGenerateDialog = false },
            onGenerate = { prompt ->
                showGenerateDialog = false
                viewModel.sendChatMessage(prompt, speakResponse = false)
                onNavigateCategory(FuturisticCategory.VOICE)
                viewModel.openChatWindow()
            }
        )
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
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = FuturisticTheme.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Creation Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FuturisticTheme.primaryText
                )
                Text(
                    text = "Generate, edit, write, and analyze multimodal content",
                    fontSize = 12.sp,
                    color = FuturisticTheme.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grid of Creation Tiles
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            // 1. Generate Image
            item {
                CreateTile(
                    title = "Generate Image",
                    subtitle = "Create art, scenes & portraits from text",
                    icon = Icons.Default.Image,
                    tag = "tile_generate_image",
                    onClick = { showGenerateDialog = true }
                )
            }

            // 2. Edit Image
            item {
                CreateTile(
                    title = "Edit Image",
                    subtitle = "Transform, stylize, or alter photos",
                    icon = Icons.Default.Tune,
                    tag = "tile_edit_image",
                    onClick = {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }

            // 3. Analyze Image
            item {
                CreateTile(
                    title = "Analyze Image",
                    subtitle = "Inspect, describe & extract text from photos",
                    icon = Icons.Default.CameraAlt,
                    tag = "tile_analyze_image",
                    onClick = {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }

            // 4. AI Writing Studio
            item {
                CreateTile(
                    title = "Writing Studio",
                    subtitle = "Emails, letters, CVs, and social captions",
                    icon = Icons.Default.Edit,
                    tag = "tile_writing_studio",
                    onClick = { onNavigateCategory(FuturisticCategory.WRITING) }
                )
            }

            // 5. Work with Document
            item {
                CreateTile(
                    title = "Document AI",
                    subtitle = "Summarize & query PDFs or text files",
                    icon = Icons.Default.Description,
                    tag = "tile_document_ai",
                    onClick = {
                        docPickerLauncher.launch(arrayOf("application/pdf", "text/plain", "text/*"))
                    }
                )
            }
        }
    }
}

@Composable
fun CreateTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(FuturisticTheme.surfaceElevated)
            .border(1.dp, FuturisticTheme.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(tag)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FuturisticTheme.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FuturisticTheme.accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = FuturisticTheme.primaryText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = FuturisticTheme.secondaryText
            )
        }
    }
}

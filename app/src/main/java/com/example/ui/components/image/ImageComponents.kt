package com.example.ui.components.image

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.image.ImageStorageManager
import com.example.data.model.ConversationMessageEntity
import com.example.ui.VoiceViewModel
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens
import kotlinx.coroutines.launch
import java.io.File

/**
 * Modern attachment button [ + ] with Gallery, Camera, and Image Generation options.
 */
@Composable
fun AttachmentOptionButton(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }
    var showPromptDialog by remember { mutableStateOf(false) }
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }

    // Modern Android Photo Picker (zero permissions needed)
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val cachedPath = ImageStorageManager.processAndCacheImage(context, uri)
                if (cachedPath != null) {
                    viewModel.attachImage(cachedPath)
                }
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraTempUri != null) {
            coroutineScope.launch {
                val cachedPath = ImageStorageManager.processAndCacheImage(context, cameraTempUri!!)
                if (cachedPath != null) {
                    viewModel.attachImage(cachedPath)
                }
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = ImageStorageManager.createCameraImageUri(context)
            cameraTempUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    // Document launcher
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val doc = com.example.data.document.DocumentProcessingManager.processDocumentUri(context, uri)
                if (doc != null) {
                    viewModel.attachDocument(doc)
                } else {
                    Toast.makeText(context, "Could not open document", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showPromptDialog) {
        GenerateImagePromptDialog(
            onDismiss = { showPromptDialog = false },
            onGenerate = { prompt ->
                showPromptDialog = false
                viewModel.sendChatMessage(prompt, speakResponse = false)
            }
        )
    }

    Box(modifier = modifier) {
        IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, CircleShape)
                .testTag("chat_attachment_plus_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Attach or Generate Image",
                tint = FuturisticTheme.primaryText,
                modifier = Modifier.size(19.dp)
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier
                .background(FuturisticTheme.surfaceElevated)
                .border(1.dp, FuturisticTheme.border, RoundedCornerShape(16.dp))
        ) {
            // 1. Generate Image
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Generate Image", fontSize = 14.sp, color = FuturisticTheme.primaryText, fontWeight = FontWeight.Medium)
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = FuturisticTheme.accent, modifier = Modifier.size(18.dp))
                },
                onClick = {
                    menuExpanded = false
                    showPromptDialog = true
                },
                modifier = Modifier.testTag("action_generate_image")
            )

            // 2. Upload from Gallery
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Upload Image", fontSize = 14.sp, color = FuturisticTheme.primaryText, fontWeight = FontWeight.Medium)
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Image, contentDescription = null, tint = FuturisticTheme.accent, modifier = Modifier.size(18.dp))
                },
                onClick = {
                    menuExpanded = false
                    galleryPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.testTag("action_upload_gallery")
            )

            // 3. Take Photo (Camera)
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Take Photo", fontSize = 14.sp, color = FuturisticTheme.primaryText, fontWeight = FontWeight.Medium)
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = FuturisticTheme.accent, modifier = Modifier.size(18.dp))
                },
                onClick = {
                    menuExpanded = false
                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        val uri = ImageStorageManager.createCameraImageUri(context)
                        cameraTempUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.testTag("action_camera")
            )

            // 4. Upload Document (PDF / Text)
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Upload Document", fontSize = 14.sp, color = FuturisticTheme.primaryText, fontWeight = FontWeight.Medium)
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null, tint = FuturisticTheme.accent, modifier = Modifier.size(18.dp))
                },
                onClick = {
                    menuExpanded = false
                    docPickerLauncher.launch(arrayOf("application/pdf", "text/plain", "text/*"))
                },
                modifier = Modifier.testTag("action_upload_document")
            )
        }
    }
}

/**
 * Preview chip shown above composer when a document is attached.
 */
@Composable
fun DocumentAttachmentPreviewChip(
    attachedDocument: com.example.data.document.DocumentAttachment?,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachedDocument == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FuturisticTheme.surfaceElevated)
            .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FuturisticTheme.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = FuturisticTheme.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = attachedDocument.fileName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText,
                        maxLines = 1
                    )
                    Text(
                        text = "${attachedDocument.fileSizeFormatted} • Ask to summarize or extract points",
                        fontSize = 10.sp,
                        color = FuturisticTheme.secondaryText
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(FuturisticTheme.surface)
                    .testTag("remove_attached_doc_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove attachment",
                    tint = FuturisticTheme.tertiaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Preview chip shown above composer when an image is attached.
 */
@Composable
fun ImageAttachmentPreviewChip(
    attachedImageUri: String?,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachedImageUri.isNullOrBlank()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FuturisticTheme.surfaceElevated)
            .border(1.dp, FuturisticTheme.accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(attachedImageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Attached image thumbnail",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Image attached",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuturisticTheme.primaryText
                    )
                    Text(
                        text = "Ask anything or tap 🎙️ to talk with AI",
                        fontSize = 10.sp,
                        color = FuturisticTheme.secondaryText
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(FuturisticTheme.surface)
                    .testTag("remove_attached_image_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove attachment",
                    tint = FuturisticTheme.tertiaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Dialog for generating an image from a custom prompt.
 */
@Composable
fun GenerateImagePromptDialog(
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    val presets = listOf(
        "Kerala backwaters houseboat at sunset",
        "A cute cat delivering pizza in rain",
        "Cinematic portrait with natural lighting",
        "Traditional Kathakali artistic illustration"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = FuturisticTheme.accent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate AI Image", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FuturisticTheme.primaryText)
            }
        },
        text = {
            Column {
                Text(
                    text = "Describe the image you want to create:",
                    fontSize = 13.sp,
                    color = FuturisticTheme.secondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("e.g. A serene Kerala tea plantation in morning mist", fontSize = 13.sp, color = FuturisticTheme.tertiaryText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_image_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FuturisticTheme.accent,
                        unfocusedBorderColor = FuturisticTheme.border,
                        focusedTextColor = FuturisticTheme.primaryText,
                        unfocusedTextColor = FuturisticTheme.primaryText
                    ),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Quick ideas:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = FuturisticTheme.tertiaryText)
                Spacer(modifier = Modifier.height(6.dp))
                presets.forEach { idea ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FuturisticTheme.surfaceElevated)
                            .clickable { prompt = idea }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(text = "✨ $idea", fontSize = 11.sp, color = FuturisticTheme.primaryText)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (prompt.isNotBlank()) onGenerate("Create an image of $prompt") },
                enabled = prompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FuturisticTheme.accent,
                    contentColor = Color.White
                ),
                shape = FuturisticTokens.CornerRadius.pillShape,
                modifier = Modifier.testTag("dialog_generate_btn")
            ) {
                Text("Generate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FuturisticTheme.secondaryText)
            }
        },
        containerColor = FuturisticTheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Message Card for Generated AI Image inside Chat.
 */
@Composable
fun GeneratedImageMessageCard(
    msg: ConversationMessageEntity,
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (!msg.imageUri.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FuturisticTheme.surface)
                    .border(1.dp, FuturisticTheme.border, RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(msg.imageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = msg.imagePrompt.ifBlank { "Generated Image" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Save | Share | Regenerate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save to Gallery
                OutlinedButton(
                    onClick = {
                        isSaving = true
                        viewModel.saveImageToGallery(msg.imageUri) { success ->
                            isSaving = false
                            Toast.makeText(
                                context,
                                if (success) "Saved to Pictures/RegionalVoiceAI!" else "Could not save image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.testTag("btn_save_image_${msg.id}"),
                    shape = FuturisticTokens.CornerRadius.pillShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FuturisticTheme.primaryText)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = FuturisticTheme.accent)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = "Save Image", modifier = Modifier.size(14.dp), tint = FuturisticTheme.accent)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                // Share
                OutlinedButton(
                    onClick = { viewModel.shareImage(context, msg.imageUri) },
                    modifier = Modifier.testTag("btn_share_image_${msg.id}"),
                    shape = FuturisticTokens.CornerRadius.pillShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FuturisticTheme.primaryText)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Image", modifier = Modifier.size(14.dp), tint = FuturisticTheme.accent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                // Save to Saved Items (Section 11)
                var isStarred by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = {
                        viewModel.saveItem(
                            title = msg.imagePrompt.ifBlank { "Generated Image" },
                            content = "Generated image: ${msg.imagePrompt}",
                            itemType = "IMAGE",
                            mediaUri = msg.imageUri,
                            prompt = msg.imagePrompt
                        )
                        isStarred = true
                        Toast.makeText(context, "Saved to ⭐ Saved Items!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("btn_star_image_${msg.id}"),
                    shape = FuturisticTokens.CornerRadius.pillShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FuturisticTheme.primaryText)
                ) {
                    Icon(
                        imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star",
                        modifier = Modifier.size(14.dp),
                        tint = if (isStarred) FuturisticTheme.accent else FuturisticTheme.secondaryText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isStarred) "Saved" else "Save", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                // Regenerate
                if (msg.imagePrompt.isNotBlank()) {
                    OutlinedButton(
                        onClick = { viewModel.regenerateImage("Create an image of ${msg.imagePrompt}") },
                        modifier = Modifier.testTag("btn_regenerate_image_${msg.id}"),
                        shape = FuturisticTokens.CornerRadius.pillShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FuturisticTheme.primaryText)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate Image", modifier = Modifier.size(14.dp), tint = FuturisticTheme.accent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerate", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

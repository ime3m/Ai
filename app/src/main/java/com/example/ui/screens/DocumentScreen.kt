package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.document.DocumentProcessingManager
import com.example.ui.VoiceViewModel
import com.example.ui.components.futuristic.FuturisticCategory
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens
import kotlinx.coroutines.launch

@Composable
fun DocumentScreen(
    viewModel: VoiceViewModel,
    onNavigateCategory: (FuturisticCategory) -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val attachedDoc by viewModel.attachedDocument.collectAsState()

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
                    Toast.makeText(context, "Could not open selected document", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = FuturisticTheme.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Document & PDF AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FuturisticTheme.primaryText
                )
                Text(
                    text = "Summarize, extract insights, and ask questions about your documents",
                    fontSize = 12.sp,
                    color = FuturisticTheme.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upload Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(FuturisticTheme.surfaceElevated)
                .border(1.5.dp, FuturisticTheme.accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .clickable {
                    docPickerLauncher.launch(arrayOf("application/pdf", "text/plain", "text/*"))
                }
                .padding(28.dp)
                .testTag("upload_document_box"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(FuturisticTheme.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = FuturisticTheme.accent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Tap to Upload PDF or Document",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FuturisticTheme.primaryText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Supports PDF, TXT, Markdown, CSV & JSON files",
                    fontSize = 12.sp,
                    color = FuturisticTheme.secondaryText
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        docPickerLauncher.launch(arrayOf("application/pdf", "text/plain", "text/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FuturisticTheme.accent),
                    shape = FuturisticTokens.CornerRadius.pillShape
                ) {
                    Text("Select Document", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "What you can ask the AI:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = FuturisticTheme.primaryText
        )

        Spacer(modifier = Modifier.height(12.dp))

        val capabilities = listOf(
            "📄 Summarize long contracts, agreements, or articles",
            "💡 Explain complex terms in simple Malayalam or English",
            "🔍 Find key points, dates, and total amounts",
            "🌐 Translate document contents into regional language",
            "❓ Answer any specific questions about the document"
        )

        capabilities.forEach { cap ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FuturisticTheme.surfaceElevated)
                    .border(1.dp, FuturisticTheme.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = cap,
                    fontSize = 13.sp,
                    color = FuturisticTheme.primaryText,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

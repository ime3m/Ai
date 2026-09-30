package com.example.data.document

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class DocumentAttachment(
    val localPath: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeFormatted: String,
    val textContent: String? = null,
    val isPdf: Boolean = false,
    val isTextFile: Boolean = false
)

object DocumentProcessingManager {

    private const val TAG = "DocumentProcessing"
    private const val MAX_PDF_BYTES = 12 * 1024 * 1024 // 12 MB safe limit for inline_data

    /**
     * Resolves document URI, caches locally, and inspects content.
     */
    suspend fun processDocumentUri(context: Context, uri: Uri): DocumentAttachment? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var fileName = "Document_${System.currentTimeMillis()}"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            val docDir = File(context.cacheDir, "documents").apply { if (!exists()) mkdirs() }
            val sanitizedName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(docDir, "${System.currentTimeMillis()}_$sanitizedName")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            val actualSize = if (fileSize > 0) fileSize else targetFile.length()
            val sizeFormatted = formatFileSize(actualSize)

            val mimeType = contentResolver.getType(uri) ?: when {
                sanitizedName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                sanitizedName.endsWith(".txt", ignoreCase = true) -> "text/plain"
                sanitizedName.endsWith(".md", ignoreCase = true) -> "text/markdown"
                sanitizedName.endsWith(".csv", ignoreCase = true) -> "text/csv"
                sanitizedName.endsWith(".json", ignoreCase = true) -> "application/json"
                else -> "application/octet-stream"
            }

            val isPdf = mimeType.equals("application/pdf", ignoreCase = true) || sanitizedName.endsWith(".pdf", ignoreCase = true)
            val isTextFile = mimeType.startsWith("text/") ||
                    sanitizedName.endsWith(".txt", ignoreCase = true) ||
                    sanitizedName.endsWith(".md", ignoreCase = true) ||
                    sanitizedName.endsWith(".csv", ignoreCase = true) ||
                    sanitizedName.endsWith(".json", ignoreCase = true)

            var extractedText: String? = null
            if (isTextFile && actualSize < 1024 * 1024) { // Read text files up to 1MB
                try {
                    extractedText = targetFile.readText(Charsets.UTF_8)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not read text file as UTF-8", e)
                }
            }

            DocumentAttachment(
                localPath = targetFile.absolutePath,
                fileName = fileName,
                mimeType = mimeType,
                fileSizeFormatted = sizeFormatted,
                textContent = extractedText,
                isPdf = isPdf,
                isTextFile = isTextFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing document URI: $uri", e)
            null
        }
    }

    /**
     * Reads document bytes encoded as Base64 (for PDF / binary Gemini payloads).
     */
    fun getDocumentBase64(localPath: String): Pair<String, String>? {
        return try {
            val file = File(localPath)
            if (!file.exists() || file.length() > MAX_PDF_BYTES) return null

            val bytes = file.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val mimeType = if (file.name.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "text/plain"
            Pair(base64, mimeType)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode document to Base64", e)
            null
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
        }
    }
}

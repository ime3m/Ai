package com.example.data.image

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * High-performance, memory-safe Image Storage and Processing Manager.
 *
 * Capabilities:
 * - Efficient downsampling and compression to prevent OOM
 * - Safe internal caching and file persistence
 * - MediaStore saving to "Pictures/RegionalVoiceAI" (scoped storage, zero broad permissions)
 * - FileProvider sharing and camera photo capture URI creation
 */
object ImageStorageManager {

    private const val TAG = "ImageStorageManager"
    private const val MAX_IMAGE_DIMENSION = 1280
    private const val COMPRESSION_QUALITY = 85

    /**
     * Prepares a file Uri for Camera capture via FileProvider.
     */
    fun createCameraImageUri(context: Context): Uri {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val tempFile = File(imagesDir, "camera_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Efficiently reads an image from Uri, downsamples to max dimension,
     * and saves to a safe local cache file. Returns local file Uri string.
     */
    suspend fun processAndCacheImage(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > MAX_IMAGE_DIMENSION || options.outWidth > MAX_IMAGE_DIMENSION) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= MAX_IMAGE_DIMENSION || (halfWidth / inSampleSize) >= MAX_IMAGE_DIMENSION) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val rawBitmap = contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }
            if (rawBitmap == null) return@withContext null

            val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
            val destFile = File(imagesDir, "upload_${System.currentTimeMillis()}.jpg")

            FileOutputStream(destFile).use { out ->
                rawBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
            }
            rawBitmap.recycle()

            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process and cache image: ${e.message}", e)
            null
        }
    }

    /**
     * Converts a local image file path or Uri into Base64 and MIME type for Gemini API.
     */
    suspend fun getBase64ImageData(context: Context, filePathOrUri: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePathOrUri)
            val bytes = if (file.exists()) {
                file.readBytes()
            } else {
                val uri = Uri.parse(filePathOrUri)
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            }

            val mimeType = if (filePathOrUri.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            Pair(base64, mimeType)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode image to Base64", e)
            null
        }
    }

    /**
     * Saves a generated image (Base64 string) to internal app storage.
     * Returns absolute file path.
     */
    suspend fun saveGeneratedImageLocally(context: Context, base64Data: String, mimeType: String = "image/png"): String? = withContext(Dispatchers.IO) {
        try {
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val ext = if (mimeType.contains("jpeg") || mimeType.contains("jpg")) "jpg" else "png"
            val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
            val imageFile = File(imagesDir, "gen_${System.currentTimeMillis()}.$ext")

            FileOutputStream(imageFile).use { fos ->
                fos.write(bytes)
                fos.flush()
            }
            imageFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save generated image locally", e)
            null
        }
    }

    /**
     * Saves image to device MediaStore (Gallery/Photos album "RegionalVoiceAI")
     * without requiring broad storage permissions on modern Android (10+).
     */
    suspend fun saveImageToPublicGallery(context: Context, imagePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(imagePath)
            if (!sourceFile.exists()) return@withContext false

            val filename = "RegionalAI_${System.currentTimeMillis()}.png"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/RegionalVoiceAI")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val contentResolver = context.contentResolver
            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext false

            contentResolver.openOutputStream(imageUri)?.use { out ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, contentValues, null, null)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image to public gallery", e)
            false
        }
    }

    /**
     * Creates an Android Share Sheet Intent for the specified image file.
     */
    fun createShareImageIntent(context: Context, imagePath: String): Intent? {
        return try {
            val file = File(imagePath)
            if (!file.exists()) return null
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create share intent", e)
            null
        }
    }
}

package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageCompressionUtil {

    /**
     * Compresses an image picked via Android Photo Picker into internal storage (< ~120 KB)
     * so portfolio items work 100% offline and upload cleanly on 2G/3G networks in RCA.
     */
    suspend fun compressAndSavePortfolioImage(context: Context, uri: Uri): String? =
        compressAndSave(
            context = context,
            uri = uri,
            folderName = "portfolio",
            filePrefix = "work",
            maxDim = 720,
            quality = 72
        )

    suspend fun compressAndSaveProfilePhoto(context: Context, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            // Remove older avatar files so only the latest compressed avatar stays on disk
            deleteAllProfileFiles(context)
            compressAndSave(
                context = context,
                uri = uri,
                folderName = "profile",
                filePrefix = "avatar",
                maxDim = 360,
                quality = 75
            )
        }

    private suspend fun compressAndSave(
        context: Context,
        uri: Uri,
        folderName: String,
        filePrefix: String,
        maxDim: Int,
        quality: Int
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val original = input.use { BitmapFactory.decodeStream(it) } ?: return@withContext null

            val scale = minOf(
                maxDim.toFloat() / original.width.coerceAtLeast(1),
                maxDim.toFloat() / original.height.coerceAtLeast(1),
                1f
            )
            val targetW = (original.width * scale).toInt().coerceAtLeast(1)
            val targetH = (original.height * scale).toInt().coerceAtLeast(1)
            val resized = if (scale < 1f) {
                Bitmap.createScaledBitmap(original, targetW, targetH, true)
            } else {
                original
            }

            val dir = File(context.filesDir, folderName).apply { mkdirs() }
            val outFile = File(dir, "${filePrefix}_${UUID.randomUUID().toString().take(8)}.jpg")
            FileOutputStream(outFile).use { fos ->
                resized.compress(Bitmap.CompressFormat.JPEG, quality, fos)
            }
            if (resized !== original) resized.recycle()
            original.recycle()
            outFile.absolutePath
        }.getOrNull()
    }

    fun deleteAllPortfolioFiles(context: Context) {
        runCatching {
            File(context.filesDir, "portfolio").deleteRecursively()
        }
    }

    fun deleteAllProfileFiles(context: Context) {
        runCatching {
            File(context.filesDir, "profile").deleteRecursively()
        }
    }
}

package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoCaptureHelper {

    /**
     * Creates an offline private file for storing a student's photographed notes
     * and returns its FileProvider content URI.
     */
    fun createPhotoUriForNotes(context: Context): Pair<File, Uri> {
        val notesDir = File(context.filesDir, "notes").apply {
            if (!exists()) mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(notesDir, "note_${timestamp}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    /**
     * Reads and compresses image bytes from a Uri for offline processing and AI analysis.
     * Keeps max dimension around 1280px and quality ~85 to save space on-device.
     */
    fun readAndCompressImage(context: Context, uri: Uri, maxDimension: Int = 1280): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val targetW: Int
                val targetH: Int
                if (ratio > 1f) {
                    targetW = maxDimension
                    targetH = (maxDimension / ratio).toInt()
                } else {
                    targetH = maxDimension
                    targetW = (maxDimension * ratio).toInt()
                }
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves raw image bytes to an offline permanent file in notes directory
     */
    fun saveBytesToOfflineNotes(context: Context, bytes: ByteArray): String? {
        return try {
            val notesDir = File(context.filesDir, "notes").apply {
                if (!exists()) mkdirs()
            }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(notesDir, "note_saved_${timestamp}.jpg")
            FileOutputStream(file).use { fos ->
                fos.write(bytes)
                fos.flush()
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

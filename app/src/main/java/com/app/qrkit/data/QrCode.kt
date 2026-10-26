package com.app.qrkit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "qr_codes")
data class QrCode(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val content: String,
    val type: String, // "TEXT", "URL", "WIFI"
    val categoryName: String? = null,
    val imagePath: String, // Path to the generated/saved QR image
    val isGenerated: Boolean = false,
    val templateId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val bgColor: Long = 0xFFFFFFFFL,
    val bgRadius: Float = 0f
)

private val isFramedCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

fun QrCode.isFramed(context: android.content.Context): Boolean {
    if (this.templateId?.startsWith("frame_") == true || this.bgRadius < 0f) {
        return true
    }
    isFramedCache[this.id]?.let { return it }
    try {
        val file = if (this.imagePath.startsWith("/")) java.io.File(this.imagePath) else java.io.File(context.filesDir, this.imagePath)
        if (file.exists()) {
            val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
            val result = options.outHeight > options.outWidth * 1.08f
            isFramedCache[this.id] = result
            return result
        }
    } catch (_: Exception) {}
    isFramedCache[this.id] = false
    return false
}


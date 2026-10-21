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

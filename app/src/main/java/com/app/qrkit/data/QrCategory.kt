package com.app.qrkit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_categories")
data class QrCategory(
    @PrimaryKey val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

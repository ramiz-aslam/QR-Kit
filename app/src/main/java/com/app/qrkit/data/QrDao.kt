package com.app.qrkit.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDao {
    @Query("SELECT * FROM qr_codes ORDER BY createdAt DESC")
    fun getAllQrCodes(): Flow<List<QrCode>>

    @Query("SELECT * FROM qr_codes WHERE categoryName = :categoryName ORDER BY createdAt DESC")
    fun getQrCodesByCategory(categoryName: String): Flow<List<QrCode>>

    @Query("SELECT * FROM qr_codes WHERE id = :id")
    fun getQrCodeById(id: String): QrCode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQrCode(qrCode: QrCode)

    @Delete
    fun deleteQrCode(qrCode: QrCode)

    @Delete
    fun deleteQrCodes(qrCodes: List<QrCode>)

    @Query("DELETE FROM qr_codes")
    fun deleteAllQrCodes()

    @Query("SELECT * FROM qr_categories ORDER BY createdAt DESC")
    fun getAllCategories(): Flow<List<QrCategory>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCategory(category: QrCategory)
    
    @Delete
    fun deleteCategory(category: QrCategory)
}



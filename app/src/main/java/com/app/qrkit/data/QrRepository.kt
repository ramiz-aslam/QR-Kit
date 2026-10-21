package com.app.qrkit.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val qrDao: QrDao) {
    val allQrCodes: Flow<List<QrCode>> = qrDao.getAllQrCodes()
    val allCategories: Flow<List<QrCategory>> = qrDao.getAllCategories()

    fun getQrCodesByCategory(name: String) = qrDao.getQrCodesByCategory(name)
    
    fun getQrCodeById(id: String) = qrDao.getQrCodeById(id)

    fun insertQrCode(qrCode: QrCode) {
        qrDao.insertQrCode(qrCode)
    }

    fun deleteQrCode(qrCode: QrCode) {
        qrDao.deleteQrCode(qrCode)
    }

    fun deleteQrCodes(qrCodes: List<QrCode>) {
        qrDao.deleteQrCodes(qrCodes)
    }

    fun deleteAllQrCodes() {
        qrDao.deleteAllQrCodes()
    }

    fun insertCategory(category: QrCategory) {
        qrDao.insertCategory(category)
    }

    fun deleteCategory(category: QrCategory) {
        qrDao.deleteCategory(category)
    }
}




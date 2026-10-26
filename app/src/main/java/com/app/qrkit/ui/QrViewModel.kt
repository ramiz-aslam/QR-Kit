package com.app.qrkit.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.qrkit.data.QrCategory
import com.app.qrkit.data.QrCode
import com.app.qrkit.data.QrDatabase
import com.app.qrkit.data.QrRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class QrViewModel(application: Application) : AndroidViewModel(application) {
    private val db = QrDatabase.getDatabase(application)
    private val repository = QrRepository(db.qrDao())

    val allQrCodes: StateFlow<List<QrCode>> = repository.allQrCodes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allCategories: StateFlow<List<QrCategory>> = repository.allCategories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun getQrCodesByCategory(name: String) = repository.getQrCodesByCategory(name)
    
    suspend fun getQrCodeById(id: String): QrCode? {
        return withContext(Dispatchers.IO) { repository.getQrCodeById(id) }
    }

    fun insertQrCode(qrCode: QrCode) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.insertQrCode(qrCode) }
        }
    }

    fun deleteQrCode(qrCode: QrCode) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteQrCode(qrCode) }
        }
    }

    fun deleteQrCodes(qrCodes: List<QrCode>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteQrCodes(qrCodes) }
        }
    }

    fun deleteAllQrCodes() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteAllQrCodes() }
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.insertCategory(QrCategory(name)) }
        }
    }
}




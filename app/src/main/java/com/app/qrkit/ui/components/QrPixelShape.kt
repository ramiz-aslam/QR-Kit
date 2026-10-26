package com.app.qrkit.ui.components

enum class QrPixelShape(val id: String, val title: String) {
    DEFAULT("default", "Default"),
    DIAMOND("diamond", "Diamond"),
    STAR("star", "Star"),
    LEAF("leaf", "Leaf");

    companion object {
        fun fromId(id: String?): QrPixelShape {
            return values().firstOrNull { 
                it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) 
            } ?: DEFAULT
        }
    }
}

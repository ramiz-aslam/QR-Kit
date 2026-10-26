package com.app.qrkit.ui.components

enum class QrEyeShape(val id: String, val title: String) {
    DEFAULT("default", "Default"),
    DIAMOND("diamond", "Diamond"),
    LEAF("leaf", "Leaf"),
    SHIELD("shield", "Shield");

    companion object {
        fun fromId(id: String?): QrEyeShape {
            return values().firstOrNull { 
                it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) 
            } ?: DEFAULT
        }
    }
}

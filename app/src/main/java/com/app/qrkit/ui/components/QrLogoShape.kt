package com.app.qrkit.ui.components

enum class QrLogoShape(val title: String) {
    SHARP("Sharp"),
    ROUNDED("Rounded"),
    CIRCLE("Circle");

    companion object {
        fun fromId(nameOrTitle: String?): QrLogoShape {
            return values().firstOrNull { 
                it.name.equals(nameOrTitle, ignoreCase = true) || it.title.equals(nameOrTitle, ignoreCase = true) 
            } ?: ROUNDED
        }
    }
}

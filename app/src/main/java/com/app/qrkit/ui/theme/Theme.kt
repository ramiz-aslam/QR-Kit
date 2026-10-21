package com.app.qrkit.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val AccentRed = Color(0xFFE60023)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1E1E1E), 
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    tertiary = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF353535),
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    tertiary = Color.Black
)

val LocalAppDynamicColor = compositionLocalOf { false }

@Composable
fun QRKitTheme(
    darkTheme: Boolean = com.app.qrkit.ui.theme.isAppInDarkTheme(),
    dynamicColor: Boolean = com.app.qrkit.ui.theme.isAppDynamicColor(),
    content: @Composable () -> Unit
) {
    val isDark = darkTheme
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamicScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isDark) dynamicScheme.copy(
                background = Color(0xFF000000), 
                surface = Color(0xFF1E1E1E),
                surfaceVariant = Color(0xFF353535)
            )
            else dynamicScheme.copy(background = Color(0xFFF2F2F4), surface = Color(0xFFF2F2F4))
        }
        dynamicColor -> {
            val fallbackPrimary = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)
            val fallbackOnPrimary = if (isDark) Color.Black else Color.White
            if (isDark) DarkColorScheme.copy(
                primary = fallbackPrimary,
                onPrimary = fallbackOnPrimary,
                background = Color(0xFF000000),
                surface = Color(0xFF1E1E1E),
                surfaceVariant = Color(0xFF353535)
            ) else LightColorScheme.copy(
                primary = fallbackPrimary,
                onPrimary = fallbackOnPrimary,
                background = Color(0xFFF2F2F4),
                surface = Color(0xFFF2F2F4)
            )
        }
        isDark -> DarkColorScheme.copy(
            background = Color(0xFF000000), 
            surface = Color(0xFF1E1E1E)
        )
        else -> LightColorScheme.copy(
            background = Color(0xFFF2F2F4), 
            surface = Color(0xFFF2F2F4)
        )
    }

    UpdateSystemBars(
        darkStatusIcons = !isDark,
        darkNavigationIcons = !isDark
    )

    CompositionLocalProvider(
        LocalAppDarkTheme provides isDark,
        LocalAppDynamicColor provides dynamicColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun isAppInDarkTheme(): Boolean {
    val localDark = LocalAppDarkTheme.current
    if (localDark != null) return localDark

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var themePref by remember { mutableStateOf(sharedPrefs.getString("theme", "System") ?: "System") }
    
    DisposableEffect(sharedPrefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "theme") {
                themePref = prefs.getString("theme", "System") ?: "System"
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    
    return when (themePref) {
        "Light" -> false
        "Dark" -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
}



@Composable
fun isAppDynamicColor(): Boolean {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var dynamicColors by remember { mutableStateOf(sharedPrefs.getBoolean("dynamic_colors", false)) }
    
    DisposableEffect(sharedPrefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "dynamic_colors") {
                dynamicColors = prefs.getBoolean("dynamic_colors", false)
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return dynamicColors
}



package com.app.qrkit.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

val LocalAppDarkTheme = compositionLocalOf<Boolean?> { null }

fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Robust, flicker-free status and navigation bar styling.
 *
 * @param darkStatusIcons true = dark/black icons for light background; false = light/white icons for dark background
 * @param darkNavigationIcons true = dark/black icons for light background; false = light/white icons for dark background
 */
fun Activity.setSystemBars(
    darkStatusIcons: Boolean,
    darkNavigationIcons: Boolean = darkStatusIcons
) {
    if (this is ComponentActivity) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ) { !darkStatusIcons },
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ) { !darkNavigationIcons }
        )
    }

    WindowCompat.setDecorFitsSystemWindows(window, false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
        @Suppress("DEPRECATION")
        window.isStatusBarContrastEnforced = false
    }
    @Suppress("DEPRECATION")
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    @Suppress("DEPRECATION")
    window.navigationBarColor = android.graphics.Color.TRANSPARENT

    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.isAppearanceLightStatusBars = darkStatusIcons
    insetsController.isAppearanceLightNavigationBars = darkNavigationIcons
}

@Composable
fun UpdateSystemBars(
    darkStatusIcons: Boolean,
    darkNavigationIcons: Boolean = darkStatusIcons
) {
    val context = LocalContext.current
    DisposableEffect(darkStatusIcons, darkNavigationIcons) {
        val activity = context.findActivity()
        activity?.setSystemBars(darkStatusIcons, darkNavigationIcons)
        onDispose { }
    }
}

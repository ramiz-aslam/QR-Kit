package com.app.qrkit

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.qrkit.ui.QrViewModel
import com.app.qrkit.ui.navigation.AppNavGraph
import com.app.qrkit.ui.theme.QRKitTheme
import com.app.qrkit.ui.theme.setSystemBars

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            @Suppress("DEPRECATION")
            window.isStatusBarContrastEnforced = false
        }
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        
        var keepSplashScreen = true
        // Keep splash screen visible for 700ms
        Handler(Looper.getMainLooper()).postDelayed({
            keepSplashScreen = false
        }, 700L)
        
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val themePref = sharedPrefs.getString("theme", "System") ?: "System"
        val isSystemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDark = when (themePref) {
            "Light" -> false
            "Dark" -> true
            else -> isSystemDark
        }
        setSystemBars(darkStatusIcons = !isDark, darkNavigationIcons = !isDark)

        setContent {
            QRKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: QrViewModel = viewModel()
                    AppNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}

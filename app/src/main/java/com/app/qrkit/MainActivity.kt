package com.app.qrkit

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.qrkit.ui.QrViewModel
import com.app.qrkit.ui.navigation.AppNavGraph
import com.app.qrkit.ui.theme.QRKitTheme
import com.app.qrkit.ui.theme.setSystemBars

class MainActivity : ComponentActivity() {
    private val shortcutAction = mutableStateOf<String?>(null)

    private fun extractShortcutAction(intent: Intent?): String? {
        if (intent == null) return null
        val action = intent.action
        val extra = intent.getStringExtra("shortcut_action")
        val dataString = intent.dataString
        val dataHost = intent.data?.host
        val dataPath = intent.data?.path
        return when {
            action == "com.app.qrkit.ACTION_SCAN_QR" ||
            extra == "scan" ||
            dataHost == "scan" ||
            dataPath?.contains("scan") == true ||
            dataString?.contains("scan") == true -> "scan"

            action == "com.app.qrkit.ACTION_CREATE_QR" ||
            extra == "create" ||
            dataHost == "create" ||
            dataPath?.contains("create") == true ||
            dataString?.contains("create") == true -> "create"

            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableHighRefreshRate()
        val action = extractShortcutAction(intent)
        shortcutAction.value = action

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
        
        var keepSplashScreen = action == null
        if (action == null) {
            Handler(Looper.getMainLooper()).postDelayed({
                keepSplashScreen = false
            }, 700L)
        }
        
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
                    AppNavGraph(
                        viewModel = viewModel,
                        shortcutAction = shortcutAction.value,
                        onShortcutHandled = { shortcutAction.value = null }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableHighRefreshRate()
    }

    private fun enableHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    window.setPreferMinimalPostProcessing(true)
                } catch (_: Exception) {}

                val display = display ?: (getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager)?.getDisplay(android.view.Display.DEFAULT_DISPLAY)
                display?.let { d ->
                    val maxMode = d.supportedModes.maxByOrNull { it.refreshRate }
                    if (maxMode != null && maxMode.refreshRate > 60f) {
                        val params = window.attributes
                        params.preferredDisplayModeId = maxMode.modeId
                        params.preferredRefreshRate = maxMode.refreshRate
                        window.attributes = params
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val modes = window.windowManager.defaultDisplay?.supportedModes
                val maxMode = modes?.maxByOrNull { it.refreshRate }
                if (maxMode != null && maxMode.refreshRate > 60f) {
                    val params = window.attributes
                    params.preferredDisplayModeId = maxMode.modeId
                    params.preferredRefreshRate = maxMode.refreshRate
                    window.attributes = params
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val action = extractShortcutAction(intent)
        if (action != null) {
            shortcutAction.value = action
        }
    }
}

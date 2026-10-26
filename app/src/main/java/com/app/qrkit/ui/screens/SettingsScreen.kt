package com.app.qrkit.ui.screens

import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.qrkit.ui.QrViewModel
import com.app.qrkit.ui.icons.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ThemeIcon(theme: String) {
    val borderColor = if (com.app.qrkit.ui.theme.isAppInDarkTheme()) Color.White.copy(alpha=0.3f) else Color.Black.copy(alpha=0.1f)
    Box(
        modifier = Modifier
            .size(24.dp)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
    ) {
        when(theme) {
            "Light" -> Box(modifier = Modifier.fillMaxSize().background(Color.White))
            "Dark" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)))
            "System" -> Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxHeight().weight(1f).background(Color.White))
                Box(modifier = Modifier.fillMaxHeight().weight(1f).background(Color(0xFF121212)))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: QrViewModel, onBackClick: () -> Unit, onNavigate: (String) -> Unit = {}) {
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val containerColor = if (isDark) Color.Black else Color.White
    val screenBgColor = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showTileGuideDialog by remember { mutableStateOf(false) }

    if (showTileGuideDialog) {
        AlertDialog(
            onDismissRequest = { showTileGuideDialog = false },
            title = {
                Text(
                    text = "Add 'Scan QR' Tile",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Text(
                    text = "1. Swipe down twice from the top of your screen to open Quick Settings.\n\n" +
                            "2. Tap the pencil / edit icon (✏️).\n\n" +
                            "3. Scroll down to find 'Scan QR' and drag it up into your active tiles.\n\n" +
                            "Now you can launch the scanner from anywhere on your phone with one tap!",
                    color = textColor.copy(alpha = 0.85f),
                    lineHeight = 22.sp,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showTileGuideDialog = false }) {
                    Text("Got It", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White
        )
    }

    var lastNavClickTime by remember { mutableStateOf(0L) }
    val safeNavigate = { route: String ->
        val now = System.currentTimeMillis()
        if (now - lastNavClickTime >= 500L) {
            lastNavClickTime = now
            onNavigate(route)
        }
    }

    if (showClearDialog) {
        com.app.qrkit.ui.components.CustomAlertDialog(
            title = "Clear All",
            message = "Are you sure you want to delete all saved QR codes? This action cannot be undone.",
            confirmText = "Clear All",
            onConfirm = {
                viewModel.deleteAllQrCodes()
                coroutineScope.launch(Dispatchers.IO) {
                    val files = context.filesDir.listFiles()
                    files?.forEach { if (it.name.startsWith("qr_") && it.name.endsWith(".png")) it.delete() }
                }
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false },
            isDestructive = true,
            isDark = isDark
        )
    }
    
    val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var themePref by remember { mutableStateOf(sharedPrefs.getString("theme", "System") ?: "System") }
    var dynamicColors by remember { mutableStateOf(sharedPrefs.getBoolean("dynamic_colors", false)) }
    
    var showThemeDropdown by remember { mutableStateOf(false) }

    val qrs by viewModel.allQrCodes.collectAsState(initial = emptyList())
    var vaultSizeMb by remember(qrs) { mutableStateOf(0f) }
    
    LaunchedEffect(qrs) {
        withContext(Dispatchers.IO) {
            var totalBytes = 0L
            val files = context.filesDir.listFiles()
            files?.forEach { file ->
                if (file.name.startsWith("qr_") && file.name.endsWith(".png")) {
                    totalBytes += file.length()
                }
            }
            vaultSizeMb = totalBytes / (1024f * 1024f)
        }
    }

    val version = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName
    } catch (e: Exception) {
        "1.0.0"
    }

    Scaffold(
        containerColor = screenBgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontFamily = com.app.qrkit.ui.theme.InterFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            ArrowBackIcon, 
                            contentDescription = "Back", 
                            tint = textColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBgColor,
                    scrolledContainerColor = screenBgColor
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsSectionTitle("Appearance")
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    SettingsItem(
                        icon = PaletteIcon,
                        title = "Theme", 
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeIcon(themePref)
                                Text(themePref, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        onClick = { showThemeDropdown = true }
                    )
                    DropdownMenu(
                        expanded = showThemeDropdown,
                        onDismissRequest = { showThemeDropdown = false },
                        containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White,
                        modifier = Modifier.background(if (isDark) Color(0xFF1E1E1E) else Color.White)
                    ) {
                        listOf("System", "Light", "Dark").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = textColor) },
                                leadingIcon = { ThemeIcon(option) },
                                onClick = {
                                    themePref = option
                                    sharedPrefs.edit().putString("theme", option).apply()
                                    showThemeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                SettingsItem(
                    icon = ColorsIcon,
                    title = "Dynamic Color", 
                    trailing = {
                        Switch(
                            modifier = Modifier.scale(0.85f),
                            checked = dynamicColors,
                            onCheckedChange = { 
                                dynamicColors = it
                                sharedPrefs.edit().putBoolean("dynamic_colors", it).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                )
            }
            
            item {
                SettingsDivider()
                SettingsSectionTitle("Quick Access")
                SettingsItem(
                    icon = QrCodeScannerIcon,
                    title = "Quick Settings Tile", 
                    subtitle = "Scan instantly from the notification panel",
                    trailing = {
                        TextButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val sbm = context.getSystemService(android.app.StatusBarManager::class.java)
                                    val comp = android.content.ComponentName(context, com.app.qrkit.service.QuickScanTileService::class.java)
                                    sbm?.requestAddTileService(
                                        comp,
                                        context.getString(com.app.qrkit.R.string.quick_scan_tile_label),
                                        android.graphics.drawable.Icon.createWithResource(context, com.app.qrkit.R.drawable.ic_shortcut_scan),
                                        context.mainExecutor
                                    ) { _ -> }
                                } else {
                                    showTileGuideDialog = true
                                }
                            }
                        ) {
                            Text("Add", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val sbm = context.getSystemService(android.app.StatusBarManager::class.java)
                            val comp = android.content.ComponentName(context, com.app.qrkit.service.QuickScanTileService::class.java)
                            sbm?.requestAddTileService(
                                comp,
                                context.getString(com.app.qrkit.R.string.quick_scan_tile_label),
                                android.graphics.drawable.Icon.createWithResource(context, com.app.qrkit.R.drawable.ic_shortcut_scan),
                                context.mainExecutor
                            ) { _ -> }
                        } else {
                            showTileGuideDialog = true
                        }
                    }
                )
            }

            item {
                SettingsDivider()
                SettingsSectionTitle("Data & Privacy")
                SettingsItem(
                    icon = ClearAllIcon,
                    title = "Clear Vault", 
                    subtitle = String.format("%.2f MB", vaultSizeMb),
                    trailing = {
                        TextButton(
                            onClick = { showClearDialog = true }
                        ) {
                            Text("Clear", color = Color(0xFFCE4235), fontWeight = FontWeight.Bold)
                        }
                    },
                    onClick = { showClearDialog = true }
                )
            }
            
            item {
                SettingsDivider()
                SettingsSectionTitle("Support")
                SettingsItem(
                    icon = FeedbackIcon,
                    title = "Feedback",
                    onClick = { safeNavigate("feedback") }
                )
                SettingsItem(
                    icon = HelpIcon,
                    title = "Help",
                    onClick = { safeNavigate("help") }
                )
                SettingsItem(
                    icon = PolicyIcon,
                    title = "Privacy Policy",
                    onClick = { safeNavigate("privacy_policy") }
                )
                SettingsItem(
                    icon = DescriptionIcon,
                    title = "Terms of Service",
                    onClick = { safeNavigate("terms") }
                )
            }
            
            item {
                SettingsDivider()
                SettingsSectionTitle("About")
                SettingsItem(
                    icon = HelpIcon,
                    title = "App Tour & Onboarding",
                    onClick = { safeNavigate("onboarding") }
                )
                SettingsItem(
                    icon = InfoIcon,
                    title = "Version",
                    trailing = {
                        Text(version ?: "1.0.0", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        ),
        color = textColor,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsDivider() {
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        thickness = 1.dp,
        color = dividerColor
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector? = null,
    title: String, 
    subtitle: String? = null, 
    trailing: @Composable (() -> Unit)? = null, 
    onClick: () -> Unit = {}
) {
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                ), 
                color = textColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }
}

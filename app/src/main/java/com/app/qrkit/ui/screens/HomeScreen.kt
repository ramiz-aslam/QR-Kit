package com.app.qrkit.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.qrkit.ui.QrViewModel
import com.app.qrkit.ui.icons.AddIcon
import com.app.qrkit.ui.icons.SettingsIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    viewModel: QrViewModel,
    onFabClick: () -> Unit,
    onVaultClick: () -> Unit,
    onQrClick: (String) -> Unit,
    onGenerateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPremiumClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val bgColor = if (isDark) MaterialTheme.colorScheme.background else Color.White

    var lastClickTime by remember { mutableStateOf(0L) }
    val safeGenerateClick = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= 500L) {
            lastClickTime = now
            onGenerateClick()
        }
    }
    val safeSettingsClick = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= 500L) {
            lastClickTime = now
            onSettingsClick()
        }
    }

    Scaffold(
        containerColor = bgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor, scrolledContainerColor = bgColor),
                title = { 
                    Text(
                        "QR KIT", 
                        fontFamily = com.app.qrkit.ui.theme.InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        letterSpacing = 0.sp
                    ) 
                },
                actions = {
                    val starsSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isStarsPressed by starsSource.collectIsPressedAsState()
                    val animatedStarsScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isStarsPressed) 0.90f else 1.0f,
                        label = "stars_press_anim"
                    )

                    IconButton(
                        onClick = onPremiumClick,
                        interactionSource = starsSource,
                        modifier = Modifier.graphicsLayer {
                            scaleX = animatedStarsScale
                            scaleY = animatedStarsScale
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Premium",
                            modifier = Modifier.size(24.dp),
                            tint = if (isDark) Color(0xFFFFD54F) else Color(0xFFD97706)
                        )
                    }

                    IconButton(onClick = safeSettingsClick) {
                        Icon(
                            SettingsIcon,
                            contentDescription = "Settings",
                            modifier = Modifier.size(28.dp),
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                val generateInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val isGeneratePressed by generateInteractionSource.collectIsPressedAsState()
                val generateScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isGeneratePressed) 0.95f else 1.0f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    ),
                    label = "generateScale"
                )

                // Big Generate QR Code Button
                Surface(
                    onClick = safeGenerateClick,
                    interactionSource = generateInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .graphicsLayer {
                            scaleX = generateScale
                            scaleY = generateScale
                        }
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color.Black.copy(alpha = 0.2f),
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Transparent
                ) {
                    val isDynamic = com.app.qrkit.ui.theme.LocalAppDynamicColor.current
                    val createButtonBg = if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFF333333) else Color.Black)
                    val createButtonContent = if (isDynamic) MaterialTheme.colorScheme.onPrimary else Color.White

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = createButtonBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = AddIcon,
                                contentDescription = "Create",
                                tint = createButtonContent,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Create",
                                style = MaterialTheme.typography.titleLarge,
                                color = createButtonContent,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

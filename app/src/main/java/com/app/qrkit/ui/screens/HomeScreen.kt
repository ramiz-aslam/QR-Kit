package com.app.qrkit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource


import com.app.qrkit.ui.components.scaleOnPress

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.qrkit.ui.icons.SettingsIcon
import com.app.qrkit.ui.icons.AddIcon
import com.app.qrkit.ui.QrViewModel
import com.app.qrkit.data.QrCode
import java.io.File
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope

private val QrCardShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    viewModel: QrViewModel,
    onFabClick: () -> Unit,
    onVaultClick: () -> Unit,
    onQrClick: (String) -> Unit,
    onGenerateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val qrs by viewModel.allQrCodes.collectAsState()

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
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 26.sp,
                        letterSpacing = 0.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = safeSettingsClick) {
                        Icon(SettingsIcon, contentDescription = "Settings", modifier = Modifier.size(28.dp), tint = if (isDark) Color.White else Color.Black)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
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
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Create",
                            style = MaterialTheme.typography.titleLarge,
                            color = createButtonContent,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            
            
            
        } // Close Column
            
        } // Close Box
}

}
























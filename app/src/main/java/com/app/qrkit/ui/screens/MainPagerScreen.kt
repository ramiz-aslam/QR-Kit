package com.app.qrkit.ui.screens

import com.app.qrkit.ui.components.modernShadow

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.app.qrkit.ui.QrViewModel
import kotlinx.coroutines.launch
import com.app.qrkit.ui.components.scaleOnPress

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MainPagerScreen(
    viewModel: QrViewModel,
    onFabClick: () -> Unit,
    onQrClick: (String) -> Unit,
    onGenerateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // HorizontalPager handles 1:1 swipe tracking and native snapping
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onFabClick = onFabClick,
                    onVaultClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    onQrClick = onQrClick,
                    onGenerateClick = onGenerateClick,
                    onSettingsClick = onSettingsClick,
                    animatedVisibilityScope = animatedVisibilityScope
                )
                1 -> VaultScreen(
                    viewModel = viewModel,
                    onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    onQrClick = onQrClick,
                    onGenerateClick = onGenerateClick,
                    onFabClick = onFabClick,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }

        // Floating Bottom Bar Overlay inside Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp)
            ) {
                com.app.qrkit.ui.components.FloatingNavBar(
                    currentRoute = if (pagerState.targetPage == 0) "home" else "vault",
                    onHomeClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    onVaultClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                )
            }
            
            val fabInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val isDynamic = com.app.qrkit.ui.theme.LocalAppDynamicColor.current
            val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
            FloatingActionButton(
                onClick = onFabClick, 
                interactionSource = fabInteractionSource,
                containerColor = if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) androidx.compose.ui.graphics.Color(0xFF1E1E1E) else androidx.compose.ui.graphics.Color.White),
                contentColor = if (isDynamic) MaterialTheme.colorScheme.onPrimary else (if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black), 
                shape = androidx.compose.foundation.shape.CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp)
                    .size(64.dp)
                    .scaleOnPress(fabInteractionSource)
                    .modernShadow(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.12f),
                        borderRadius = 32.dp,
                        blurRadius = 32.dp,
                        offsetY = 0.dp,
                        spread = 0.dp
                    )
            ) {
                Icon(
                    com.app.qrkit.ui.icons.QrCodeScannerIcon, 
                    contentDescription = "Scan QR",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}






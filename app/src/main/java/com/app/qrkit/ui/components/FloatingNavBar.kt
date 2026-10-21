package com.app.qrkit.ui.components

import com.app.qrkit.ui.components.modernShadow

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.app.qrkit.ui.icons.HomeIcon
import com.app.qrkit.ui.icons.VaultIcon
import kotlin.math.roundToInt

@Composable
fun FloatingNavBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onVaultClick: () -> Unit
) {
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White

    val interactionSource = remember { MutableInteractionSource() }
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navScale"
    )

    var touchX by remember { mutableFloatStateOf(0f) }
    var rowWidth by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val paddingPx = with(density) { 4.dp.toPx() }
    val indicatorWidthPx = with(density) { 88.dp.toPx() }
    val spacingPx = with(density) { 4.dp.toPx() }

    val homeX = paddingPx
    val vaultX = paddingPx + indicatorWidthPx + spacingPx
    val maxX = vaultX

    val targetOffsetX = if (isDragging) {
        (touchX - indicatorWidthPx / 2f).coerceIn(homeX, maxX)
    } else {
        if (currentRoute == "home") homeX else vaultX
    }

    val animatedOffsetX by animateFloatAsState(
        targetValue = targetOffsetX,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorOffset"
    )

    Box(
        modifier = Modifier
            .height(64.dp).wrapContentWidth()
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale 
            }
            .modernShadow(
                color = Color.Black.copy(alpha = 0.12f),
                borderRadius = 32.dp,
                blurRadius = 32.dp,
                offsetY = 0.dp,
                spread = 0.dp
            )
            .background(containerColor, CircleShape)
            .onSizeChanged { rowWidth = it.width }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    isDragging = true
                    touchX = down.position.x
                    
                    val press = PressInteraction.Press(down.position)
                    interactionSource.tryEmit(press)

                    var releasedNormally = false
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change != null) {
                            if (change.pressed) {
                                touchX = change.position.x
                                change.consume()
                            } else {
                                releasedNormally = true
                                touchX = change.position.x
                                change.consume()
                            }
                        }
                    } while (!releasedNormally && event.changes.any { it.pressed })

                    isPressed = false
                    isDragging = false

                    if (releasedNormally) {
                        interactionSource.tryEmit(PressInteraction.Release(press))
                        if (touchX < size.width / 2f) {
                            onHomeClick()
                        } else {
                            onVaultClick()
                        }
                    } else {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val isDynamic = com.app.qrkit.ui.theme.LocalAppDynamicColor.current
        val indicatorBgColor = if (isDynamic) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            if (isDark) Color(0xFF353535) else Color(0xFFF2F2F2)
        }

        // Sliding Indicator Background
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .width(88.dp)
                .height(56.dp)
                .clip(CircleShape)
                .background(indicatorBgColor)
        )

        // Icons Row
        Row(
            modifier = Modifier
                .wrapContentWidth().fillMaxHeight()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NavBarItem(
                icon = if (currentRoute == "home") com.app.qrkit.ui.icons.HomeFilledIcon else HomeIcon,
                isSelected = currentRoute == "home"
            )
            NavBarItem(
                icon = if (currentRoute == "vault") com.app.qrkit.ui.icons.VaultFilledIcon else VaultIcon,
                isSelected = currentRoute == "vault"
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean
) {
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val isDynamic = com.app.qrkit.ui.theme.LocalAppDynamicColor.current
    val targetIconColor = if (isSelected) {
        if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color.Black)
    } else {
        if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)
    }
    val iconColor by animateColorAsState(
        targetValue = targetIconColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .height(56.dp)
            .width(88.dp)
            .clip(CircleShape)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}



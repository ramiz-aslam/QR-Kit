package com.app.qrkit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun DynamicSquircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    icon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadiusPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
        label = "corner_radius"
    )

    val currentContainerColor by animateColorAsState(
        targetValue = if (isPressed) containerColor.copy(alpha = 0.8f) else containerColor,
        label = "container_color"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(percent = cornerRadiusPercent))
            .background(currentContainerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick,
                role = Role.Button
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            icon()
        }
    }
}

@Composable
fun DynamicSquircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    icon: (@Composable () -> Unit)? = null,
    text: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadiusPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
        label = "corner_radius"
    )
    
    val currentContainerColor by animateColorAsState(
        targetValue = if (isPressed) containerColor.copy(alpha = 0.8f) else containerColor,
        label = "container_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = cornerRadiusPercent))
            .background(currentContainerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick,
                role = Role.Button
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                ProvideTextStyle(value = MaterialTheme.typography.labelLarge) {
                    text()
                }
            }
        }
    }
}

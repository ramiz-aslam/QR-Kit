package com.app.qrkit.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shape

fun Modifier.scaleOnPress(interactionSource: MutableInteractionSource): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scaleOnPress"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaleOnPressFilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.filledShape,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        FilledIconButton(
            onClick = onClick,
            modifier = modifier.scaleOnPress(interactionSource),
            enabled = enabled,
            shape = shape,
            colors = colors,
            interactionSource = interactionSource,
            content = content
        )
    }
}

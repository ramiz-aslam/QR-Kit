package com.app.qrkit.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.modernShadow(
    color: Color = Color.Black.copy(alpha = 0.08f),
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 24.dp,
    offsetY: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    spread: Dp = 0.dp
) = this.drawBehind {
    val transparent = color.copy(alpha = 0f).toArgb()
    val shadowColor = color.toArgb()
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparent
        val blurRadiusPx = blurRadius.toPx()
        frameworkPaint.setShadowLayer(
            blurRadiusPx,
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        
        val spreadPx = spread.toPx()
        val left = -spreadPx
        val top = -spreadPx
        val right = size.width + spreadPx
        val bottom = size.height + spreadPx
        
        it.drawRoundRect(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}

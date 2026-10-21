package com.app.qrkit.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PinIcon: ImageVector
    get() = ImageVector.Builder("Pin", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16f, 12f)
            verticalLineTo(4f)
            horizontalLineToRelative(1f)
            curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
            reflectiveCurveToRelative(-0.45f, -1f, -1f, -1f)
            horizontalLineTo(7f)
            curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
            reflectiveCurveToRelative(0.45f, 1f, 1f, 1f)
            horizontalLineToRelative(1f)
            verticalLineToRelative(8f)
            lineToRelative(-2f, 2f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(5.2f)
            verticalLineToRelative(5.8f)
            lineToRelative(0.8f, 1.2f)
            lineToRelative(0.8f, -1.2f)
            verticalLineTo(16f)
            horizontalLineTo(18f)
            verticalLineToRelative(-2f)
            lineTo(16f, 12f)
            close()
        }
    }.build()

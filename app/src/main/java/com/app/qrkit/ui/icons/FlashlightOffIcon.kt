package com.app.qrkit.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val FlashlightOffIcon: ImageVector
  get() {
    if (_FlashlightOffIcon != null) {
      return _FlashlightOffIcon!!
    }
    _FlashlightOffIcon =
      ImageVector.Builder(
          name = "flashlight_off",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(19.1f, 21.9f)
            lineTo(16f, 18.8f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(14f, 22f)
            horizontalLineTo(10f)
            quadTo(9.18f, 22f, 8.59f, 21.41f)
            reflectiveQuadTo(8f, 20f)
            verticalLineTo(10.8f)
            lineTo(2.1f, 4.9f)
            quadTo(1.83f, 4.63f, 1.83f, 4.2f)
            reflectiveQuadTo(2.1f, 3.5f)
            quadTo(2.38f, 3.22f, 2.8f, 3.22f)
            reflectiveQuadTo(3.5f, 3.5f)
            lineToRelative(17f, 17f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
            reflectiveQuadTo(20.5f, 21.9f)
            quadToRelative(-0.27f, 0.28f, -0.7f, 0.28f)
            reflectiveQuadTo(19.1f, 21.9f)
            close()
            moveTo(10f, 20f)
            horizontalLineToRelative(4f)
            verticalLineTo(16.8f)
            lineToRelative(-4f, -4f)
            verticalLineTo(20f)
            close()
            moveTo(18f, 4f)
            verticalLineTo(7.4f)
            quadToRelative(0f, 0.3f, -0.07f, 0.57f)
            reflectiveQuadTo(17.68f, 8.5f)
            lineToRelative(-1.85f, 2.82f)
            quadToRelative(-0.28f, 0.43f, -0.66f, 0.45f)
            reflectiveQuadTo(14.45f, 11.6f)
            quadTo(14.3f, 11.5f, 14.19f, 11.35f)
            reflectiveQuadTo(14.03f, 11.01f)
            reflectiveQuadTo(14.01f, 10.63f)
            reflectiveQuadToRelative(0.16f, -0.4f)
            lineTo(16f, 7.4f)
            verticalLineTo(7f)
            horizontalLineTo(10.25f)
            quadTo(9.83f, 7f, 9.55f, 6.71f)
            quadTo(9.28f, 6.43f, 9.28f, 6f)
            reflectiveQuadTo(9.56f, 5.29f)
            reflectiveQuadTo(10.28f, 5f)
            horizontalLineTo(16f)
            verticalLineTo(4f)
            horizontalLineTo(7.23f)
            quadTo(6.8f, 4f, 6.53f, 3.71f)
            reflectiveQuadTo(6.25f, 3f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(7.25f, 2f)
            horizontalLineTo(16f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(18f, 4f)
            close()
            moveTo(12f, 14.8f)
            close()
            moveTo(10.25f, 7f)
            close()
          }
        }
        .build()
    return _FlashlightOffIcon!!
  }

private var _FlashlightOffIcon: ImageVector? = null


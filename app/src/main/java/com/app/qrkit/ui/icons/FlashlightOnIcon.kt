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
public val FlashlightOnIcon: ImageVector
  get() {
    if (_FlashlightOnIcon != null) {
      return _FlashlightOnIcon!!
    }
    _FlashlightOnIcon =
      ImageVector.Builder(
          name = "flashlight_on",
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
            moveTo(8f, 20f)
            verticalLineTo(11f)
            lineTo(6.33f, 8.5f)
            quadTo(6.15f, 8.25f, 6.08f, 7.97f)
            reflectiveQuadTo(6f, 7.4f)
            verticalLineTo(4f)
            quadTo(6f, 3.17f, 6.59f, 2.59f)
            reflectiveQuadTo(8f, 2f)
            horizontalLineToRelative(8f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(18f, 4f)
            verticalLineTo(7.4f)
            quadToRelative(0f, 0.3f, -0.07f, 0.57f)
            reflectiveQuadTo(17.68f, 8.5f)
            lineTo(16f, 11f)
            verticalLineToRelative(9f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(14f, 22f)
            horizontalLineTo(10f)
            quadTo(9.18f, 22f, 8.59f, 21.41f)
            reflectiveQuadTo(8f, 20f)
            close()
            moveToRelative(2.94f, -4.94f)
            quadTo(10.5f, 14.63f, 10.5f, 14f)
            reflectiveQuadToRelative(0.44f, -1.06f)
            reflectiveQuadTo(12f, 12.5f)
            reflectiveQuadToRelative(1.06f, 0.44f)
            reflectiveQuadTo(13.5f, 14f)
            reflectiveQuadToRelative(-0.44f, 1.06f)
            reflectiveQuadTo(12f, 15.5f)
            reflectiveQuadTo(10.94f, 15.06f)
            close()
            moveTo(8f, 5f)
            horizontalLineToRelative(8f)
            verticalLineTo(4f)
            horizontalLineTo(8f)
            verticalLineTo(5f)
            close()
            moveToRelative(8f, 2f)
            horizontalLineTo(8f)
            verticalLineTo(7.4f)
            lineToRelative(2f, 3f)
            verticalLineTo(20f)
            horizontalLineToRelative(4f)
            verticalLineTo(10.4f)
            lineToRelative(2f, -3f)
            verticalLineTo(7f)
            close()
            moveToRelative(-4f, 5f)
            close()
          }
        }
        .build()
    return _FlashlightOnIcon!!
  }

private var _FlashlightOnIcon: ImageVector? = null


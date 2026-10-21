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
public val TextAdIcon: ImageVector
  get() {
    if (_TextAdIcon != null) {
      return _TextAdIcon!!
    }
    _TextAdIcon =
      ImageVector.Builder(
          name = "text_ad",
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
            moveTo(4f, 20f)
            quadTo(3.18f, 20f, 2.59f, 19.41f)
            reflectiveQuadTo(2f, 18f)
            verticalLineTo(6f)
            quadTo(2f, 5.18f, 2.59f, 4.59f)
            reflectiveQuadTo(4f, 4f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 5.18f, 22f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 20f)
            horizontalLineTo(4f)
            close()
            moveTo(4f, 18f)
            horizontalLineTo(20f)
            verticalLineTo(6f)
            horizontalLineTo(4f)
            verticalLineTo(18f)
            close()
            moveToRelative(0f, 0f)
            verticalLineTo(6f)
            verticalLineTo(18f)
            close()
            moveTo(6f, 17f)
            horizontalLineTo(18f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(19f, 16.43f, 19f, 16f)
            reflectiveQuadTo(18.71f, 15.29f)
            reflectiveQuadTo(18f, 15f)
            horizontalLineTo(6f)
            quadTo(5.58f, 15f, 5.29f, 15.29f)
            reflectiveQuadTo(5f, 16f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(6f, 17f)
            close()
            moveTo(6f, 13f)
            horizontalLineTo(18f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(19f, 12.43f, 19f, 12f)
            reflectiveQuadTo(18.71f, 11.29f)
            reflectiveQuadTo(18f, 11f)
            horizontalLineTo(6f)
            quadTo(5.58f, 11f, 5.29f, 11.29f)
            reflectiveQuadTo(5f, 12f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(6f, 13f)
            close()
            moveTo(6f, 9f)
            horizontalLineToRelative(8f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            reflectiveQuadTo(15f, 8f)
            quadTo(15f, 7.57f, 14.71f, 7.29f)
            reflectiveQuadTo(14f, 7f)
            horizontalLineTo(6f)
            quadTo(5.58f, 7f, 5.29f, 7.29f)
            reflectiveQuadTo(5f, 8f)
            quadTo(5f, 8.42f, 5.29f, 8.71f)
            reflectiveQuadTo(6f, 9f)
            close()
          }
        }
        .build()
    return _TextAdIcon!!
  }

private var _TextAdIcon: ImageVector? = null


public val text_ad: ImageVector
    get() = TextAdIcon

public val TextIcon: ImageVector
    get() = TextAdIcon

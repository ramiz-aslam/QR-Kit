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
public val PolicyIcon: ImageVector
  get() {
    if (_PolicyIcon != null) {
      return _PolicyIcon!!
    }
    _PolicyIcon =
      ImageVector.Builder(
          name = "policy",
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
            moveTo(11.68f, 21.88f)
            quadTo(11.53f, 21.85f, 11.38f, 21.8f)
            quadTo(8f, 20.68f, 6f, 17.64f)
            reflectiveQuadTo(4f, 11.1f)
            verticalLineTo(6.38f)
            quadTo(4f, 5.75f, 4.36f, 5.25f)
            quadTo(4.73f, 4.75f, 5.3f, 4.52f)
            lineToRelative(6f, -2.25f)
            quadTo(11.65f, 2.15f, 12f, 2.15f)
            reflectiveQuadToRelative(0.7f, 0.13f)
            lineToRelative(6f, 2.25f)
            quadToRelative(0.58f, 0.23f, 0.94f, 0.73f)
            reflectiveQuadTo(20f, 6.38f)
            verticalLineTo(11.1f)
            quadToRelative(0f, 1.88f, -0.59f, 3.66f)
            reflectiveQuadTo(17.73f, 18f)
            quadToRelative(-0.2f, 0.27f, -0.54f, 0.29f)
            reflectiveQuadTo(16.6f, 18.05f)
            lineTo(14f, 15.45f)
            quadToRelative(-0.45f, 0.28f, -0.96f, 0.41f)
            reflectiveQuadTo(12f, 16f)
            quadTo(10.35f, 16f, 9.18f, 14.83f)
            reflectiveQuadTo(8f, 12f)
            reflectiveQuadTo(9.18f, 9.17f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadToRelative(2.83f, 1.17f)
            reflectiveQuadTo(16f, 12f)
            quadToRelative(0f, 0.55f, -0.14f, 1.06f)
            reflectiveQuadToRelative(-0.41f, 0.99f)
            lineToRelative(1.5f, 1.5f)
            quadToRelative(0.5f, -1.02f, 0.78f, -2.15f)
            reflectiveQuadTo(18f, 11.1f)
            verticalLineTo(6.38f)
            lineTo(12f, 4.13f)
            lineTo(6f, 6.38f)
            verticalLineTo(11.1f)
            quadToRelative(0f, 3.03f, 1.7f, 5.5f)
            reflectiveQuadTo(12f, 19.9f)
            quadToRelative(0.4f, -0.13f, 0.79f, -0.3f)
            reflectiveQuadToRelative(0.76f, -0.4f)
            quadTo(13.9f, 19f, 14.31f, 19.05f)
            reflectiveQuadToRelative(0.66f, 0.4f)
            quadToRelative(0.25f, 0.35f, 0.16f, 0.75f)
            reflectiveQuadTo(14.7f, 20.83f)
            quadToRelative(-0.5f, 0.3f, -1f, 0.54f)
            reflectiveQuadTo(12.63f, 21.8f)
            quadToRelative(-0.15f, 0.05f, -0.3f, 0.07f)
            reflectiveQuadTo(12f, 21.9f)
            reflectiveQuadTo(11.68f, 21.88f)
            close()
            moveToRelative(1.74f, -8.46f)
            quadTo(14f, 12.83f, 14f, 12f)
            reflectiveQuadTo(13.41f, 10.59f)
            reflectiveQuadTo(12f, 10f)
            reflectiveQuadToRelative(-1.41f, 0.59f)
            quadTo(10f, 11.18f, 10f, 12f)
            reflectiveQuadToRelative(0.59f, 1.41f)
            reflectiveQuadTo(12f, 14f)
            reflectiveQuadToRelative(1.41f, -0.59f)
            close()
            moveTo(12.2f, 12.08f)
            close()
          }
        }
        .build()
    return _PolicyIcon!!
  }

private var _PolicyIcon: ImageVector? = null


public val policy: ImageVector
    get() = PolicyIcon

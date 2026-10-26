package com.app.qrkit.ui.components

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.app.qrkit.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrDesignRenderer {

    fun getLogoDrawableRes(logoId: String?): Int? {
        return when (logoId?.lowercase()) {
            "whatsapp" -> R.drawable.ic_logo_whatsapp
            "instagram" -> R.drawable.ic_logo_instagram
            "youtube" -> R.drawable.ic_logo_youtube
            "telegram" -> R.drawable.ic_logo_telegram
            "x", "twitter" -> R.drawable.ic_logo_x
            "tiktok" -> R.drawable.ic_logo_tiktok
            "spotify" -> R.drawable.ic_logo_spotify
            "paypal" -> R.drawable.ic_logo_paypal
            "linkedin" -> R.drawable.ic_logo_linkedin
            "wifi" -> R.drawable.ic_logo_wifi
            "link" -> R.drawable.ic_logo_link
            "phone" -> R.drawable.ic_logo_phone
            "email" -> R.drawable.ic_logo_email
            "location" -> R.drawable.ic_logo_location
            "coffee" -> R.drawable.ic_logo_coffee
            "shopping" -> R.drawable.ic_logo_shopping
            "star" -> R.drawable.ic_logo_star
            "heart" -> R.drawable.ic_logo_heart
            else -> null
        }
    }

    val availableLogos = listOf(
        Triple("none", "None", null),
        Triple("whatsapp", "WhatsApp", R.drawable.ic_logo_whatsapp),
        Triple("instagram", "Instagram", R.drawable.ic_logo_instagram),
        Triple("youtube", "YouTube", R.drawable.ic_logo_youtube),
        Triple("telegram", "Telegram", R.drawable.ic_logo_telegram),
        Triple("x", "X / Twitter", R.drawable.ic_logo_x),
        Triple("tiktok", "TikTok", R.drawable.ic_logo_tiktok),
        Triple("spotify", "Spotify", R.drawable.ic_logo_spotify),
        Triple("paypal", "PayPal", R.drawable.ic_logo_paypal),
        Triple("linkedin", "LinkedIn", R.drawable.ic_logo_linkedin),
        Triple("wifi", "WiFi", R.drawable.ic_logo_wifi),
        Triple("link", "Link", R.drawable.ic_logo_link),
        Triple("phone", "Call", R.drawable.ic_logo_phone),
        Triple("email", "Email", R.drawable.ic_logo_email),
        Triple("location", "Location", R.drawable.ic_logo_location),
        Triple("coffee", "Cafe", R.drawable.ic_logo_coffee),
        Triple("shopping", "Shop", R.drawable.ic_logo_shopping),
        Triple("star", "Star", R.drawable.ic_logo_star),
        Triple("heart", "Heart", R.drawable.ic_logo_heart)
    )

    fun renderQr(
        context: Context,
        content: String,
        eyeColor: Int,
        pixelColor: Int,
        bgColor: Int,
        eyeRadius: Float,
        pixelRadius: Float,
        bgRadius: Float,
        eyeShape: QrEyeShape = QrEyeShape.DEFAULT,
        pixelShape: QrPixelShape = QrPixelShape.DEFAULT,
        logoId: String = "none",
        logoShape: QrLogoShape = QrLogoShape.ROUNDED,
        frameStyle: QrFrameStyle = QrFrameStyle.NONE,
        frameCtaText: String = "",
        frameColor: Int = pixelColor,
        canvasSize: Int = 1024
    ): Bitmap {
        val safeContent = content.ifBlank { "QR Kit" }
        val hasLogo = logoId != "none" && getLogoDrawableRes(logoId) != null
        val ecLevel = if (hasLogo) ErrorCorrectionLevel.H else ErrorCorrectionLevel.M

        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 0)
            put(EncodeHintType.ERROR_CORRECTION, ecLevel)
        }

        val bitMatrix = try {
            QRCodeWriter().encode(safeContent, BarcodeFormat.QR_CODE, 0, 0, hints)
        } catch (_: Exception) {
            QRCodeWriter().encode("QR Kit", BarcodeFormat.QR_CODE, 0, 0, hints)
        }

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        val marginModules = 2
        val totalWidth = matrixWidth + marginModules * 2
        val sizeF = canvasSize.toFloat()
        val moduleSize = sizeF / totalWidth

        val qrBmp = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
        val qrCanvas = Canvas(qrBmp)

        val maxCornerPx = sizeF / 2f
        val cornerRadiusPx = bgRadius * maxCornerPx

        // 1. Draw Surface Background
        if (frameStyle == QrFrameStyle.NONE) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
            if (cornerRadiusPx > 0f) {
                qrCanvas.drawRoundRect(RectF(0f, 0f, sizeF, sizeF), cornerRadiusPx, cornerRadiusPx, bgPaint)
            } else {
                qrCanvas.drawRect(0f, 0f, sizeF, sizeF, bgPaint)
            }
        }

        // 2. Draw Pixels
        val pixelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pixelColor }
        val pRadius = pixelRadius * (moduleSize / 2f)

        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                val isEye = (x < 7 && y < 7) || (x > matrixWidth - 8 && y < 7) || (x < 7 && y > matrixHeight - 8)
                if (bitMatrix[x, y] && !isEye) {
                    val left = (x + marginModules) * moduleSize
                    val top = (y + marginModules) * moduleSize
                    val right = left + moduleSize
                    val bottom = top + moduleSize
                    val cx = (left + right) / 2f
                    val cy = (top + bottom) / 2f

                    when (pixelShape) {
                        QrPixelShape.DEFAULT -> {
                            val r = if (pRadius > 0f) pRadius else 0f
                            if (r > 0f) {
                                qrCanvas.drawRoundRect(RectF(left, top, right, bottom), r, r, pixelPaint)
                            } else {
                                qrCanvas.drawRect(left, top, right, bottom, pixelPaint)
                            }
                        }
                        QrPixelShape.DIAMOND -> {
                            val dPath = Path().apply {
                                moveTo(cx, top)
                                lineTo(right, cy)
                                lineTo(cx, bottom)
                                lineTo(left, cy)
                                close()
                            }
                            qrCanvas.drawPath(dPath, pixelPaint)
                        }
                        QrPixelShape.STAR -> {
                            val inset = moduleSize * 0.22f
                            val starPath = Path().apply {
                                moveTo(cx, top)
                                lineTo(cx + inset, cy - inset)
                                lineTo(right, cy)
                                lineTo(cx + inset, cy + inset)
                                lineTo(cx, bottom)
                                lineTo(cx - inset, cy + inset)
                                lineTo(left, cy)
                                lineTo(cx - inset, cy - inset)
                                close()
                            }
                            qrCanvas.drawPath(starPath, pixelPaint)
                        }
                        QrPixelShape.LEAF -> {
                            val r = moduleSize / 2f
                            val leafPath = Path().apply {
                                addRoundRect(
                                    RectF(left, top, right, bottom),
                                    floatArrayOf(r, r, 0f, 0f, r, r, 0f, 0f),
                                    Path.Direction.CW
                                )
                            }
                            qrCanvas.drawPath(leafPath, pixelPaint)
                        }
                    }
                }
            }
        }

        // 3. Draw Eyes
        val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = eyeColor }
        val eyeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }

        fun drawEye(ex: Int, ey: Int) {
            val outerRect = RectF(
                (ex + marginModules) * moduleSize,
                (ey + marginModules) * moduleSize,
                (ex + 7 + marginModules) * moduleSize,
                (ey + 7 + marginModules) * moduleSize
            )
            val hollowRect = RectF(
                (ex + 1 + marginModules) * moduleSize,
                (ey + 1 + marginModules) * moduleSize,
                (ex + 6 + marginModules) * moduleSize,
                (ey + 6 + marginModules) * moduleSize
            )
            val innerRect = RectF(
                (ex + 2 + marginModules) * moduleSize,
                (ey + 2 + marginModules) * moduleSize,
                (ex + 5 + marginModules) * moduleSize,
                (ey + 5 + marginModules) * moduleSize
            )

            val cx = outerRect.centerX()
            val cy = outerRect.centerY()

            when (eyeShape) {
                QrEyeShape.DEFAULT -> {
                    val outerRadius = eyeRadius * 3.5f * moduleSize
                    val hollowRadius = (outerRadius - moduleSize).coerceAtLeast(0f)
                    val innerRadius = eyeRadius * 1.5f * moduleSize

                    if (outerRadius > 0f) {
                        qrCanvas.drawRoundRect(outerRect, outerRadius, outerRadius, eyePaint)
                        qrCanvas.drawRoundRect(hollowRect, hollowRadius, hollowRadius, eyeBgPaint)
                        qrCanvas.drawRoundRect(innerRect, innerRadius, innerRadius, eyePaint)
                    } else {
                        qrCanvas.drawRect(outerRect, eyePaint)
                        qrCanvas.drawRect(hollowRect, eyeBgPaint)
                        qrCanvas.drawRect(innerRect, eyePaint)
                    }
                }

                QrEyeShape.DIAMOND -> {
                    val outPath = Path().apply {
                        moveTo(cx, outerRect.top)
                        lineTo(outerRect.right, cy)
                        lineTo(cx, outerRect.bottom)
                        lineTo(outerRect.left, cy)
                        close()
                    }
                    qrCanvas.drawPath(outPath, eyePaint)

                    val holPath = Path().apply {
                        moveTo(cx, hollowRect.top)
                        lineTo(hollowRect.right, cy)
                        lineTo(cx, hollowRect.bottom)
                        lineTo(hollowRect.left, cy)
                        close()
                    }
                    qrCanvas.drawPath(holPath, eyeBgPaint)

                    val inPath = Path().apply {
                        moveTo(cx, innerRect.top)
                        lineTo(innerRect.right, cy)
                        lineTo(cx, innerRect.bottom)
                        lineTo(innerRect.left, cy)
                        close()
                    }
                    qrCanvas.drawPath(inPath, eyePaint)
                }

                QrEyeShape.LEAF -> {
                    val rOut = 3.5f * moduleSize
                    val outPath = Path().apply {
                        addRoundRect(outerRect, floatArrayOf(rOut, rOut, 0f, 0f, rOut, rOut, 0f, 0f), Path.Direction.CW)
                    }
                    qrCanvas.drawPath(outPath, eyePaint)

                    val rHol = (rOut - moduleSize).coerceAtLeast(0f)
                    val holPath = Path().apply {
                        addRoundRect(hollowRect, floatArrayOf(rHol, rHol, 0f, 0f, rHol, rHol, 0f, 0f), Path.Direction.CW)
                    }
                    qrCanvas.drawPath(holPath, eyeBgPaint)

                    val rIn = 1.5f * moduleSize
                    val inPath = Path().apply {
                        addRoundRect(innerRect, floatArrayOf(rIn, rIn, 0f, 0f, rIn, rIn, 0f, 0f), Path.Direction.CW)
                    }
                    qrCanvas.drawPath(inPath, eyePaint)
                }

                QrEyeShape.SHIELD -> {
                    val rOut = 3.5f * moduleSize
                    val outPath = Path().apply {
                        addRoundRect(outerRect, floatArrayOf(0f, 0f, 0f, 0f, rOut, rOut, rOut, rOut), Path.Direction.CW)
                    }
                    qrCanvas.drawPath(outPath, eyePaint)

                    val rHol = (rOut - moduleSize).coerceAtLeast(0f)
                    val holPath = Path().apply {
                        addRoundRect(hollowRect, floatArrayOf(0f, 0f, 0f, 0f, rHol, rHol, rHol, rHol), Path.Direction.CW)
                    }
                    qrCanvas.drawPath(holPath, eyeBgPaint)

                    val rIn = 1.5f * moduleSize
                    val inPath = Path().apply {
                        addRoundRect(innerRect, floatArrayOf(0f, 0f, 0f, 0f, rIn, rIn, rIn, rIn), Path.Direction.CW)
                    }
                    qrCanvas.drawPath(inPath, eyePaint)
                }
            }
        }

        drawEye(0, 0)
        drawEye(matrixWidth - 7, 0)
        drawEye(0, matrixHeight - 7)

        // 4. Draw Center Logo if enabled
        val logoRes = getLogoDrawableRes(logoId)
        if (logoRes != null) {
            val badgeSize = sizeF * 0.203f
            val badgeHalf = badgeSize / 2f
            val cx = sizeF / 2f
            val cy = sizeF / 2f
            val badgeRect = RectF(cx - badgeHalf, cy - badgeHalf, cx + badgeHalf, cy + badgeHalf)

            val currentRadius = when (logoShape) {
                QrLogoShape.SHARP -> 0f
                QrLogoShape.ROUNDED -> badgeSize * 0.23f
                QrLogoShape.CIRCLE -> badgeHalf
            }

            // Draw white badge background
            val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            qrCanvas.drawRoundRect(badgeRect, currentRadius, currentRadius, badgeBgPaint)

            // Draw badge border
            val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = eyeColor
                style = Paint.Style.STROKE
                strokeWidth = sizeF * 0.0078f
            }
            qrCanvas.drawRoundRect(badgeRect, currentRadius, currentRadius, badgeBorderPaint)

            // Draw vector icon
            val iconDrawable = ContextCompat.getDrawable(context, logoRes)
            if (iconDrawable != null) {
                val iconInset = badgeSize * 0.22f
                iconDrawable.setBounds(
                    (badgeRect.left + iconInset).toInt(),
                    (badgeRect.top + iconInset).toInt(),
                    (badgeRect.right - iconInset).toInt(),
                    (badgeRect.bottom - iconInset).toInt()
                )
                iconDrawable.draw(qrCanvas)
            }
        }

        if (frameStyle == QrFrameStyle.NONE) {
            return qrBmp
        }

        // 5. Wrap in Selected Frame
        return QrFrameRenderer.renderFramedQr(
            innerQrBitmap = qrBmp,
            frameStyle = frameStyle,
            ctaText = frameCtaText,
            frameColor = frameColor,
            bgColor = bgColor
        )
    }
}

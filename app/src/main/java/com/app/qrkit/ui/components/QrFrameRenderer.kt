package com.app.qrkit.ui.components

import android.graphics.*
import androidx.core.graphics.ColorUtils

enum class QrFrameStyle(val id: String, val title: String, val defaultText: String) {
    NONE("none", "None", ""),
    BOTTOM_BANNER("bottom_banner", "Classic", "SCAN ME"),
    SMARTPHONE("smartphone", "Smartphone", "SCAN ME"),
    HEADER_BADGE("header_badge", "Header & Badge", "SCAN ME"),
    SPEECH_BUBBLE("speech_bubble", "Speech Bubble", "SCAN ME"),
    TICKET("ticket", "Coupon Ticket", "SCAN TO REDEEM"),
    RETRO("retro", "Retro Frame", "SCAN HERE"),
    POLAROID("polaroid", "Polaroid", "SCAN ME"),
    MINIMAL_BORDER("minimal_border", "Minimal Tag", "SCAN ME"),
    NEON_CYBER("neon_cyber", "Cyberpunk", "SCAN CODE"),
    ELEGANT_RIBBON("elegant_ribbon", "Ribbon Tag", "SCAN ME"),
    COFFEE_SHOP("coffee_shop", "Cafe Board", "MENU & ORDER");
}

object QrFrameRenderer {

    // 2x HD Resolution Canvas
    private const val CANVAS_WIDTH = 1200
    private const val CANVAS_HEIGHT = 1480

    fun renderFramedQr(
        innerQrBitmap: Bitmap,
        frameStyle: QrFrameStyle,
        ctaText: String,
        frameColor: Int,
        bgColor: Int
    ): Bitmap {
        if (frameStyle == QrFrameStyle.NONE) {
            return innerQrBitmap
        }

        val output = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Clear canvas with transparent pixels
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val cleanText = ctaText.trim().ifBlank { frameStyle.defaultText }
        val isFrameLight = ColorUtils.calculateLuminance(frameColor) > 0.5
        val bannerTextColor = if (isFrameLight) Color.BLACK else Color.WHITE

        val filterPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        when (frameStyle) {
            QrFrameStyle.NONE -> innerQrBitmap

            QrFrameStyle.BOTTOM_BANNER -> {
                drawBottomBannerFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.SMARTPHONE -> {
                drawSmartphoneFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.HEADER_BADGE -> {
                drawHeaderBadgeFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.SPEECH_BUBBLE -> {
                drawSpeechBubbleFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.TICKET -> {
                drawTicketFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.RETRO -> {
                drawRetroFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.POLAROID -> {
                drawPolaroidFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = if (ColorUtils.calculateLuminance(bgColor) > 0.5) Color.BLACK else Color.WHITE,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.MINIMAL_BORDER -> {
                drawMinimalBorderFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.NEON_CYBER -> {
                drawNeonCyberFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.ELEGANT_RIBBON -> {
                drawElegantRibbonFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }

            QrFrameStyle.COFFEE_SHOP -> {
                drawCoffeeShopFrame(
                    canvas = canvas,
                    qrBmp = innerQrBitmap,
                    text = cleanText,
                    frameColor = frameColor,
                    bgColor = bgColor,
                    textColor = bannerTextColor,
                    filterPaint = filterPaint
                )
            }
        }

        return output
    }

    private fun drawBottomBannerFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val cardRect = RectF(50f, 40f, 1150f, 1440f)
        val cardRadius = 72f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, borderPaint)

        val qrRect = RectF(120f, 100f, 1080f, 1060f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)

        val bannerRect = RectF(110f, 1140f, 1090f, 1340f)
        val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bannerRect, 32f, 32f, bannerPaint)

        drawFittedText(canvas, text, bannerRect, textColor, 60f)
    }

    private fun drawSmartphoneFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val bodyRect = RectF(70f, 30f, 1130f, 1450f)
        val bodyRadius = 90f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bodyRect, bodyRadius, bodyRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        canvas.drawRoundRect(bodyRect, bodyRadius, bodyRadius, borderPaint)

        val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        val notchRect = RectF(500f, 60f, 700f, 85f)
        canvas.drawRoundRect(notchRect, 12f, 12f, notchPaint)

        val qrRect = RectF(120f, 160f, 1080f, 1120f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)

        val ctaRect = RectF(130f, 1180f, 1070f, 1360f)
        val ctaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(ctaRect, 36f, 36f, ctaPaint)

        drawFittedText(canvas, text, ctaRect, textColor, 56f)
    }

    private fun drawHeaderBadgeFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val cardRect = RectF(60f, 120f, 1140f, 1440f)
        val cardRadius = 72f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, borderPaint)

        val badgeRect = RectF(180f, 40f, 1020f, 210f)
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(badgeRect, 48f, 48f, badgePaint)
        drawFittedText(canvas, text, badgeRect, textColor, 58f)

        val qrRect = RectF(120f, 280f, 1080f, 1240f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)
    }

    private fun drawSpeechBubbleFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val bubbleRect = RectF(100f, 40f, 1100f, 240f)
        val bubbleRadius = 56f
        val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bubbleRect, bubbleRadius, bubbleRadius, bubblePaint)

        val tailPath = Path().apply {
            moveTo(560f, 240f)
            lineTo(600f, 285f)
            lineTo(640f, 240f)
            close()
        }
        canvas.drawPath(tailPath, bubblePaint)
        drawFittedText(canvas, text, bubbleRect, textColor, 56f)

        val cardRect = RectF(60f, 320f, 1140f, 1440f)
        val cardRadius = 72f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, borderPaint)

        val qrRect = RectF(120f, 380f, 1080f, 1340f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)
    }

    private fun drawTicketFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val path = Path().apply {
            moveTo(110f, 40f)
            lineTo(1090f, 40f)
            quadTo(1150f, 40f, 1150f, 100f)

            lineTo(1150f, 1050f)
            arcTo(RectF(1090f, 1050f, 1210f, 1170f), 270f, -180f, false)
            lineTo(1150f, 1380f)
            quadTo(1150f, 1440f, 1090f, 1440f)

            lineTo(110f, 1440f)
            quadTo(50f, 1440f, 50f, 1380f)
            lineTo(50f, 1170f)
            arcTo(RectF(-10f, 1050f, 110f, 1170f), 90f, -180f, false)
            lineTo(50f, 100f)
            quadTo(50f, 40f, 110f, 40f)
            close()
        }

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawPath(path, borderPaint)

        val qrRect = RectF(120f, 90f, 1080f, 1050f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)

        val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 5f
            pathEffect = DashPathEffect(floatArrayOf(18f, 12f), 0f)
        }
        canvas.drawLine(120f, 1110f, 1080f, 1110f, dashPaint)

        val bannerRect = RectF(120f, 1180f, 1080f, 1370f)
        val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bannerRect, 32f, 32f, bannerPaint)
        drawFittedText(canvas, text, bannerRect, textColor, 56f)
    }

    private fun drawRetroFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val outerRect = RectF(40f, 36f, 1160f, 1444f)
        val innerRect = RectF(65f, 61f, 1135f, 1419f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(outerRect, bgPaint)

        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawRect(outerRect, outerBorderPaint)

        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(innerRect, innerBorderPaint)

        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(RectF(44f, 40f, 64f, 60f), accentPaint)
        canvas.drawRect(RectF(1136f, 40f, 1156f, 60f), accentPaint)
        canvas.drawRect(RectF(44f, 1420f, 64f, 1440f), accentPaint)
        canvas.drawRect(RectF(1136f, 1420f, 1156f, 1440f), accentPaint)

        val qrRect = RectF(120f, 110f, 1080f, 1070f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)

        val bannerRect = RectF(110f, 1130f, 1090f, 1330f)
        canvas.drawRoundRect(bannerRect, 24f, 24f, accentPaint)
        drawFittedText(canvas, text, bannerRect, textColor, 60f)
    }

    private fun drawPolaroidFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val cardRect = RectF(50f, 40f, 1150f, 1440f)
        val cardRadius = 36f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, borderPaint)

        val qrRect = RectF(90f, 80f, 1110f, 1100f)
        val qrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(qrRect, qrBgPaint)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)

        val textRect = RectF(90f, 1150f, 1110f, 1380f)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = 64f
        }
        val fontMetrics = textPaint.fontMetrics
        val textY = textRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(text, textRect.centerX(), textY, textPaint)
    }

    private fun drawMinimalBorderFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val cardRect = RectF(60f, 50f, 1140f, 1430f)
        val cardRadius = 54f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, borderPaint)

        val qrRect = RectF(120f, 110f, 1080f, 1070f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)

        val pillRect = RectF(220f, 1160f, 980f, 1340f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(pillRect, 90f, 90f, pillPaint)
        drawFittedText(canvas, text, pillRect, textColor, 56f)
    }

    private fun drawNeonCyberFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val cut = 48f
        val path = Path().apply {
            moveTo(60f + cut, 40f)
            lineTo(1140f - cut, 40f)
            lineTo(1140f, 40f + cut)
            lineTo(1140f, 1440f - cut)
            lineTo(1140f - cut, 1440f)
            lineTo(60f + cut, 1440f)
            lineTo(60f, 1440f - cut)
            lineTo(60f, 40f + cut)
            close()
        }

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawPath(path, borderPaint)

        // Corner bracket accents
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }
        canvas.drawLine(80f, 40f, 160f, 40f, accentPaint)
        canvas.drawLine(1040f, 40f, 1120f, 40f, accentPaint)
        canvas.drawLine(80f, 1440f, 160f, 1440f, accentPaint)
        canvas.drawLine(1040f, 1440f, 1120f, 1440f, accentPaint)

        val qrRect = RectF(120f, 110f, 1080f, 1070f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)

        val badgePath = Path().apply {
            val bCut = 24f
            moveTo(140f + bCut, 1150f)
            lineTo(1060f - bCut, 1150f)
            lineTo(1060f, 1150f + bCut)
            lineTo(1060f, 1340f - bCut)
            lineTo(1060f - bCut, 1340f)
            lineTo(140f + bCut, 1340f)
            lineTo(140f, 1340f - bCut)
            lineTo(140f, 1150f + bCut)
            close()
        }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawPath(badgePath, badgePaint)
        drawFittedText(canvas, text, RectF(140f, 1150f, 1060f, 1340f), textColor, 58f)
    }

    private fun drawElegantRibbonFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val cardRect = RectF(60f, 90f, 1140f, 1440f)
        val cardRadius = 64f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, borderPaint)

        // Top hanging ribbon
        val ribbonRect = RectF(160f, 30f, 1040f, 200f)
        val ribbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(ribbonRect, 28f, 28f, ribbonPaint)
        drawFittedText(canvas, text, ribbonRect, textColor, 58f)

        val qrRect = RectF(120f, 260f, 1080f, 1220f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)
    }

    private fun drawCoffeeShopFrame(
        canvas: Canvas,
        qrBmp: Bitmap,
        text: String,
        frameColor: Int,
        bgColor: Int,
        textColor: Int,
        filterPaint: Paint
    ) {
        val cardRect = RectF(50f, 40f, 1150f, 1440f)
        val cardRadius = 50f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 9f
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, borderPaint)

        val innerLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(RectF(66f, 56f, 1134f, 1424f), cardRadius - 10f, cardRadius - 10f, innerLine)

        val qrRect = RectF(120f, 100f, 1080f, 1060f)
        canvas.drawBitmap(qrBmp, null, qrRect, filterPaint)

        val bannerRect = RectF(110f, 1130f, 1090f, 1340f)
        val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bannerRect, 40f, 40f, bannerPaint)
        drawFittedText(canvas, text, bannerRect, textColor, 56f)
    }

    private fun drawFittedText(
        canvas: Canvas,
        text: String,
        bannerRect: RectF,
        textColor: Int,
        initialSize: Float
    ) {
        val maxTextWidth = bannerRect.width() - 72f
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = textColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = initialSize
        }

        while (textPaint.measureText(text) > maxTextWidth && textPaint.textSize > 28f) {
            textPaint.textSize -= 2f
        }

        val fontMetrics = textPaint.fontMetrics
        val textY = bannerRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(text, bannerRect.centerX(), textY, textPaint)
    }
}

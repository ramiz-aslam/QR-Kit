package com.app.qrkit.domain

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.tasks.await

class ImageProcessor {
    private val options = SubjectSegmenterOptions.Builder()
        .enableForegroundBitmap()
        .build()
        
    private val segmenter = SubjectSegmentation.getClient(options)
    
    suspend fun removeBackground(bitmap: Bitmap): Result<Bitmap> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = segmenter.process(image).await()
            val foregroundBitmap = result.foregroundBitmap
            if (foregroundBitmap != null) {
                Result.success<Bitmap>(foregroundBitmap)
            } else {
                Result.failure<Bitmap>(Exception("No subject found"))
            }
        } catch (e: Exception) {
            Result.failure<Bitmap>(e)
        }
    }

    fun addWhiteBorder(bitmap: Bitmap, borderSize: Float = 15f): Bitmap {
        val width = bitmap.width + (borderSize * 2).toInt()
        val height = bitmap.height + (borderSize * 2).toInt()
        
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = borderSize * 2
            strokeJoin = android.graphics.Paint.Join.ROUND
            strokeCap = android.graphics.Paint.Cap.ROUND
        }
        
        val alphaBitmap = bitmap.extractAlpha()
        canvas.drawBitmap(alphaBitmap, borderSize, borderSize, paint)
        canvas.drawBitmap(bitmap, borderSize, borderSize, null)
        
        alphaBitmap.recycle()
        return output
    }

    fun addCaption(bitmap: Bitmap, text: String): Bitmap {
        if (text.isBlank()) return bitmap
        
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(output)
        
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = output.height * 0.15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = paint.textSize
            typeface = paint.typeface
            textAlign = paint.textAlign
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = paint.textSize * 0.15f
        }
        
        val x = output.width / 2f
        val y = output.height - (output.height * 0.1f)
        
        canvas.drawText(text, x, y, strokePaint)
        canvas.drawText(text, x, y, paint)
        
        return output
    }
    
    fun formatForWhatsApp(bitmap: Bitmap, targetSize: Int = 512): Bitmap {
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        
        // Calculate scaling factor to fit within targetSize while maintaining aspect ratio
        val scale = Math.min(targetSize.toFloat() / bitmap.width, targetSize.toFloat() / bitmap.height)
        
        // Calculate new dimensions
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        
        // Calculate position to center the image
        val dx = (targetSize - newWidth) / 2f
        val dy = (targetSize - newHeight) / 2f
        
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        
        canvas.drawBitmap(scaledBitmap, dx, dy, null)
        
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        
        return output
    }
}

package com.app.qrkit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ProfessionalColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
    
    var hue by remember { mutableStateOf(hsv[0]) }
    var saturation by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }
    
    val currentColor = remember(hue, saturation, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }
    
    var hexText by remember { 
        mutableStateOf(String.format("#%06X", 0xFFFFFF and currentColor.toArgb())) 
    }

    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF1E1E1E) else Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Color", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                Spacer(modifier = Modifier.height(20.dp))
                
                // 2D Picker (Hue & Saturation)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()
                                val x = change.position.x.coerceIn(0f, width)
                                val y = change.position.y.coerceIn(0f, height)
                                hue = (x / width) * 360f
                                saturation = 1f - (y / height)
                                hexText = String.format("#%06X", 0xFFFFFF and Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))).toArgb())
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()
                                val x = offset.x.coerceIn(0f, width)
                                val y = offset.y.coerceIn(0f, height)
                                hue = (x / width) * 360f
                                saturation = 1f - (y / height)
                                hexText = String.format("#%06X", 0xFFFFFF and Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))).toArgb())
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val hueColors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                        drawRect(brush = Brush.horizontalGradient(hueColors))
                        drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.White)))
                        
                        val selectorX = (hue / 360f) * size.width
                        val selectorY = (1f - saturation) * size.height
                        drawCircle(color = Color.Black, radius = 14.dp.toPx(), center = Offset(selectorX, selectorY), style = Stroke(width = 3.dp.toPx()))
                        drawCircle(color = Color.White, radius = 12.dp.toPx(), center = Offset(selectorX, selectorY), style = Stroke(width = 2.dp.toPx()))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Brightness/Value Slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val width = size.width.toFloat()
                                val x = change.position.x.coerceIn(0f, width)
                                value = x / width
                                hexText = String.format("#%06X", 0xFFFFFF and Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))).toArgb())
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val width = size.width.toFloat()
                                val x = offset.x.coerceIn(0f, width)
                                value = x / width
                                hexText = String.format("#%06X", 0xFFFFFF and Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))).toArgb())
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val brightColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f)))
                        drawRect(brush = Brush.horizontalGradient(listOf(Color.Black, brightColor)))
                        
                        val selectorX = value * size.width
                        drawCircle(color = Color.White, radius = 16.dp.toPx(), center = Offset(selectorX, size.height / 2f))
                        drawCircle(color = currentColor, radius = 13.dp.toPx(), center = Offset(selectorX, size.height / 2f))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Hex Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(currentColor).border(1.dp, Color.Gray.copy(alpha=0.3f), CircleShape))
                    
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { newValue ->
                            hexText = newValue.take(7)
                            if (hexText.length == 7 && hexText.startsWith("#")) {
                                try {
                                    val parsed = android.graphics.Color.parseColor(hexText)
                                    val tempHsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(parsed, tempHsv)
                                    hue = tempHsv[0]
                                    saturation = tempHsv[1]
                                    value = tempHsv[2]
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("HEX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF353535) else Color(0xFFF7F7F7),
                            contentColor = if (isDark) Color.White else Color.Black
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { 
                            onColorSelected(currentColor)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Apply", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun InlineColorPicker(
    initialColor: Color,
    onColorChanged: (Color) -> Unit
) {
    val hsv = remember(initialColor) { 
        FloatArray(3).apply { android.graphics.Color.colorToHSV(initialColor.toArgb(), this) } 
    }
    
    var hue by remember { mutableStateOf(hsv[0]) }
    var saturation by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }
    
    val currentColor = remember(hue, saturation, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }
    
    LaunchedEffect(currentColor) {
        onColorChanged(currentColor)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 2D Picker (Hue & Saturation)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val x = change.position.x.coerceIn(0f, width)
                        val y = change.position.y.coerceIn(0f, height)
                        hue = (x / width) * 360f
                        saturation = 1f - (y / height)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val x = offset.x.coerceIn(0f, width)
                        val y = offset.y.coerceIn(0f, height)
                        hue = (x / width) * 360f
                        saturation = 1f - (y / height)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val hueColors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                drawRect(brush = Brush.horizontalGradient(hueColors))
                drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.White)))
                
                val selectorX = (hue / 360f) * size.width
                val selectorY = (1f - saturation) * size.height
                drawCircle(color = Color.Black, radius = 12.dp.toPx(), center = Offset(selectorX, selectorY), style = Stroke(width = 3.dp.toPx()))
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(selectorX, selectorY), style = Stroke(width = 2.dp.toPx()))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Brightness/Value Slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val width = size.width.toFloat()
                        val x = change.position.x.coerceIn(0f, width)
                        value = x / width
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val width = size.width.toFloat()
                        val x = offset.x.coerceIn(0f, width)
                        value = x / width
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val brightColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f)))
                drawRect(brush = Brush.horizontalGradient(listOf(Color.Black, brightColor)))
                
                val selectorX = value * size.width
                drawCircle(color = Color.White, radius = 11.dp.toPx(), center = Offset(selectorX, size.height / 2f))
                drawCircle(color = currentColor, radius = 8.dp.toPx(), center = Offset(selectorX, size.height / 2f))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}


package com.app.qrkit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import com.app.qrkit.ui.theme.isAppInDarkTheme

@Composable
fun CustomAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = true,
    isDark: Boolean = isAppInDarkTheme()
) {
    val dialogBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val titleColor = if (isDark) Color.White else Color.Black
    val messageColor = if (isDark) Color(0xFFAAAAAA) else Color.DarkGray
    val buttonBg = if (isDark) Color(0xFF353535) else Color(0xFFEBEBEB)
    val cancelTextColor = if (isDark) Color.White else Color.Black
    val confirmTextColor = if (isDestructive) Color(0xFFCE4235) else (if (isDark) Color.White else Color.Black)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(dialogBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = messageColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBg,
                            contentColor = cancelTextColor
                        ),
                        shape = CircleShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(dismissText, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBg,
                            contentColor = confirmTextColor
                        ),
                        shape = CircleShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(confirmText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

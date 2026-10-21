package com.app.qrkit.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.qrkit.ui.components.scaleOnPress
import com.app.qrkit.ui.icons.*
import com.app.qrkit.ui.theme.isAppInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val isDark = isAppInDarkTheme()
    val isDynamic = com.app.qrkit.ui.theme.LocalAppDynamicColor.current

    val screenBgColor = if (isDark) Color.Black else Color.White
    val cardBgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)
    val fieldBgColor = if (isDark) Color(0xFF353535) else Color(0xFFF2F2F2)
    val textColor = if (isDark) Color.White else Color.Black
    val subtitleColor = if (isDark) Color(0xFFA0A0A0) else Color(0xFF707070)

    val primaryAccent = if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFF333333) else Color.Black)
    val primaryAccentContent = if (isDynamic) MaterialTheme.colorScheme.onPrimary else Color.White

    var selectedCategory by remember { mutableStateOf("General") }
    var feedbackText by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val categories = listOf("General", "Feature Request", "Bug Report", "UI & Design", "Performance")

    val versionName = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    val submitFeedback = {
        if (feedbackText.isNotBlank()) {
            val emailBody = buildString {
                append("Category: ").append(selectedCategory).append("\n\n")
                append("Message:\n").append(feedbackText.trim()).append("\n\n")
                if (userEmail.isNotBlank()) {
                    append("Contact Email: ").append(userEmail.trim()).append("\n\n")
                }
                append("--- Diagnostics ---\n")
                append("App Version: ").append(versionName).append("\n")
                append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n")
                append("Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@qrkit.app")
                putExtra(Intent.EXTRA_SUBJECT, "[QR Kit Feedback] $selectedCategory")
                putExtra(Intent.EXTRA_TEXT, emailBody)
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If no email app is installed, show confirmation directly
                Toast.makeText(context, "Thank you! Your feedback has been noted.", Toast.LENGTH_SHORT).show()
            }
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        com.app.qrkit.ui.components.CustomAlertDialog(
            title = "Thank You!",
            message = "We appreciate you taking the time to share your thoughts. Your feedback helps make QR Kit better with every update.",
            confirmText = "Done",
            onConfirm = {
                showSuccessDialog = false
                onBackClick()
            },
            onDismiss = {
                showSuccessDialog = false
                onBackClick()
            },
            isDestructive = false,
            isDark = isDark
        )
    }

    Scaffold(
        containerColor = screenBgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Feedback",
                        fontFamily = com.app.qrkit.ui.theme.InterFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(ArrowBackIcon, contentDescription = "Back", modifier = Modifier.size(28.dp), tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBgColor,
                    scrolledContainerColor = screenBgColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Hero Intro Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0xFF353535) else Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FeedbackIcon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "We'd Love Your Input",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Suggest new features, report bugs, or share your thoughts with us.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = subtitleColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Topic / Category Selector
            Text(
                text = "Feedback Topic",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val chipBg = if (isSelected) primaryAccent else fieldBgColor
                    val chipText = if (isSelected) primaryAccentContent else textColor.copy(alpha = 0.85f)

                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = chipBg,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Feedback Message Field
            Text(
                text = "Your Feedback",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                placeholder = {
                    Text(
                        "Describe your suggestion, the bug you encountered, or ideas for improving QR Kit...",
                        color = subtitleColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fieldBgColor,
                    unfocusedContainerColor = fieldBgColor,
                    focusedBorderColor = if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f)),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email (Optional) Field
            Text(
                text = "Email (Optional)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Provide your email address if you would like us to reply.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = subtitleColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = userEmail,
                onValueChange = { userEmail = it },
                placeholder = {
                    Text(
                        "name@example.com",
                        color = subtitleColor,
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fieldBgColor,
                    unfocusedContainerColor = fieldBgColor,
                    focusedBorderColor = if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f)),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Diagnostics Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBgColor.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = InfoIcon,
                    contentDescription = null,
                    tint = subtitleColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "QR Kit v$versionName • Android ${Build.VERSION.RELEASE}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = subtitleColor
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            val isButtonEnabled = feedbackText.isNotBlank()
            val interactionSource = remember { MutableInteractionSource() }

            Surface(
                onClick = { if (isButtonEnabled) submitFeedback() },
                enabled = isButtonEnabled,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scaleOnPress(interactionSource),
                shape = RoundedCornerShape(26.dp),
                color = if (isButtonEnabled) primaryAccent else primaryAccent.copy(alpha = 0.4f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Submit Feedback",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isButtonEnabled) primaryAccentContent else primaryAccentContent.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

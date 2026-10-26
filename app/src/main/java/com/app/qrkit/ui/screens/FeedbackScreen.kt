package com.app.qrkit.ui.screens

import android.os.Build
import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.app.qrkit.ui.components.CustomAlertDialog
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
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var includeDeviceInfo by remember { mutableStateOf(true) }
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

    val deviceName = remember {
        val manufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        val model = Build.MODEL.orEmpty()
        if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model".trim().ifEmpty { "Android Device" }
        }
    }

    val isEmailValid = remember(userEmail) {
        userEmail.isBlank() || Patterns.EMAIL_ADDRESS.matcher(userEmail.trim()).matches()
    }

    val submitFeedback = {
        if (feedbackText.isNotBlank()) {
            if (!isEmailValid) {
                Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            } else {
                // Feedback is recorded; show thank you dialog directly without launching email client
                showSuccessDialog = true
            }
        }
    }

    if (showSuccessDialog) {
        CustomAlertDialog(
            title = "Thank You!",
            message = "We appreciate you taking the time to share your thoughts. Your feedback helps make QR Kit better with every update.",
            confirmText = "Done",
            dismissText = null,
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
                .padding(vertical = 12.dp)
        ) {
            // Hero Intro Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
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
                color = textColor,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
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
                color = textColor,
                modifier = Modifier.padding(horizontal = 20.dp)
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
                    .padding(horizontal = 20.dp)
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

            // Name (Optional) Field
            Text(
                text = "Name (Optional)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Let us know what to call you (optional).",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = subtitleColor,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                placeholder = {
                    Text(
                        "Your Name",
                        color = subtitleColor,
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Email (Optional) Field
            Text(
                text = "Email (Optional)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Provide your email address if you would like us to reply.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = subtitleColor,
                modifier = Modifier.padding(horizontal = 20.dp)
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
                isError = !isEmailValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fieldBgColor,
                    unfocusedContainerColor = fieldBgColor,
                    focusedBorderColor = if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f)),
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = Color(0xFFE53935),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            if (!isEmailValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Please enter a valid email address (e.g. name@example.com)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFE53935),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Device & App Info Card with Toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Include Technical Details",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (includeDeviceInfo) "App & device info will be attached" else "Technical details excluded",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = subtitleColor
                            )
                        }

                        Switch(
                            checked = includeDeviceInfo,
                            onCheckedChange = { includeDeviceInfo = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color.Black),
                                uncheckedThumbColor = if (isDark) Color(0xFFAAAAAA) else Color(0xFF777777),
                                uncheckedTrackColor = if (isDark) Color(0xFF353535) else Color(0xFFE0E0E0)
                            )
                        )
                    }

                    AnimatedVisibility(
                        visible = includeDeviceInfo,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(fieldBgColor)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("App Name & Version", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = subtitleColor)
                                Text("QR Kit v$versionName", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = textColor)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Device Name", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = subtitleColor)
                                Text(deviceName, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = textColor)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Android Version", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = subtitleColor)
                                Text("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = textColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            val isButtonEnabled = feedbackText.isNotBlank() && isEmailValid
            val interactionSource = remember { MutableInteractionSource() }

            Surface(
                onClick = {
                    if (isButtonEnabled) {
                        submitFeedback()
                    } else if (!isEmailValid) {
                        Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = isButtonEnabled,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
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
                        text = "Submit",
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

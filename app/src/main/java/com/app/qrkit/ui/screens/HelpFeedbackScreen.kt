package com.app.qrkit.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.qrkit.ui.components.ScaleOnPressFilledIconButton
import com.app.qrkit.ui.icons.*
import com.app.qrkit.ui.theme.isAppInDarkTheme

data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val isDark = isAppInDarkTheme()
    val containerColor = if (isDark) Color.Black else Color.White
    val containerBgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)
    val textColor = if (isDark) Color.White else Color.Black
    val bodyTextColor = if (isDark) Color(0xFFD6D6D6) else Color(0xFF2C2C2C)
    val captionColor = if (isDark) Color(0xFFA0A0A0) else Color(0xFF707070)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)

    val faqs = remember {
        listOf(
            FaqItem(
                id = 1,
                question = "Is my data stored securely and offline?",
                answer = "Yes, 100%. QR Kit operates on an offline-first architecture. All your generated codes, scanned history, and custom presets are stored exclusively in your device's local storage. No data is uploaded to remote servers."
            ),
            FaqItem(
                id = 2,
                question = "How do I customize QR colors and corners?",
                answer = "When generating a new QR code, fill in the details and tap 'Next' on the top header. You will find tabs for 'Color' (to customize eye and pixel colors) and 'Corners' (to adjust eye and pixel curvature with live sliders)."
            ),
            FaqItem(
                id = 3,
                question = "Can I scan codes directly from gallery images or screenshots?",
                answer = "Yes! Open the Scanner screen and tap the photo icon located at the bottom right. Pick any image or screenshot from your device's gallery, and QR Kit will decode it instantly."
            ),
            FaqItem(
                id = 4,
                question = "How do I export or save high-resolution QR codes?",
                answer = "On the QR Details screen (or immediately after generating a code), tap the Download button to export a lossless PNG image directly to your gallery, or tap Share to send it via any messaging app."
            ),
            FaqItem(
                id = 5,
                question = "What should I do if a barcode won't scan?",
                answer = "Ensure your camera lens is clean and there is adequate lighting. You can toggle the flashlight using the torch icon on the scanner header. Hold your device steady about 6 to 12 inches away from the code to allow auto-focus."
            ),
            FaqItem(
                id = 6,
                question = "Does QR Kit require any account or registration?",
                answer = "No registration or login is ever required. You can use all features of QR Kit immediately upon installation without providing any personal information or accounts."
            ),
            FaqItem(
                id = 7,
                question = "How do I wipe or delete my saved vault data?",
                answer = "You can delete individual QR codes from the QR Details page. To delete all saved items at once, go to Settings and tap 'Clear Vault' to purge all saved codes and files permanently."
            )
        )
    }

    val openSupportEmail = {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@qrkit.app")
            putExtra(Intent.EXTRA_SUBJECT, "QR Kit Support Request")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No email client found. Contact: support@qrkit.app", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = containerColor,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Help", 
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
                actions = {
                    IconButton(
                        onClick = { openSupportEmail() },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(MailIcon, contentDescription = "Support Email", modifier = Modifier.size(28.dp), tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    scrolledContainerColor = containerColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "FAQ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            faqs.forEach { faq ->
                FaqAccordionCard(
                    faq = faq,
                    containerBgColor = containerBgColor,
                    textColor = textColor,
                    bodyTextColor = bodyTextColor,
                    dividerColor = dividerColor
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HelpFeedbackScreen(onBackClick: () -> Unit) = HelpScreen(onBackClick)

@Composable
fun FaqAccordionCard(
    faq: FaqItem,
    containerBgColor: Color,
    textColor: Color,
    bodyTextColor: Color,
    dividerColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "ArrowAnim"
    )

    Surface(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = containerBgColor,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    ),
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(arrowRotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(250)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        thickness = 1.dp,
                        color = dividerColor
                    )
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        ),
                        color = bodyTextColor
                    )
                }
            }
        }
    }
}

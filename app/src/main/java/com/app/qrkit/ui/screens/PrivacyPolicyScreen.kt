package com.app.qrkit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.qrkit.ui.components.ScaleOnPressFilledIconButton
import com.app.qrkit.ui.icons.ArrowBackIcon
import com.app.qrkit.ui.theme.isAppInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    val isDark = isAppInDarkTheme()
    val containerColor = if (isDark) Color.Black else Color.White
    val buttonBgColor = if (isDark) Color(0xFF242424) else Color(0xFFF2F2F2)
    val textColor = if (isDark) Color.White else Color.Black
    val bodyTextColor = if (isDark) Color(0xFFD6D6D6) else Color(0xFF2C2C2C)
    val captionColor = if (isDark) Color(0xFFA0A0A0) else Color(0xFF666666)

    Scaffold(
        containerColor = containerColor,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Privacy Policy", 
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Last updated: October 2026",
                style = MaterialTheme.typography.labelMedium,
                color = captionColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            PolicySection(
                title = "1. Introduction & Overview",
                body = "Welcome to QR Kit. Your privacy is paramount to us, and we are committed to being completely transparent about how our application operates. This Privacy Policy details our privacy practices and explains why our application is designed with on-device privacy as its core foundation.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            PolicySection(
                title = "2. Zero-Knowledge On-Device Storage",
                body = "QR Kit is built from the ground up to operate offline. All data generated or managed by the app—including custom QR codes, scanned history, vault entries, folders, and customized visual presets—is stored exclusively on your device's local storage.\n\nWe do not operate remote databases, clouds, or user telemetry servers to harvest your QR records. You retain 100% custody and control over your data at all times.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            PolicySection(
                title = "3. Information We Do Not Collect",
                body = "• Personal Identification: We do not require any registration, login, phone number, email address, or name.\n• QR Content: We never intercept, transmit, or analyze the contents of your QR codes (whether URLs, WiFi passwords, phone numbers, or plain text).\n• Location Data: We do not request, track, or record your physical GPS location.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            PolicySection(
                title = "4. Device Permissions & Usage",
                body = "To provide core QR functionality, the app requests minimal Android system permissions:\n\n• Camera: Required only when you use the real-time QR scanner. The camera feed is processed in-memory directly on your device via ML Kit and is never recorded, captured to disk, or broadcast.\n• Storage / Photos: Required only when you explicitly export, save, or download a generated QR code image to your device's gallery.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            PolicySection(
                title = "5. Anonymous Crash Reporting",
                body = "To diagnose performance bugs and maintain app stability, the app may collect non-identifiable technical diagnostics (such as OS version, device model, and stack traces) in the event of an unhandled crash. These logs contain strictly technical telemetry and never contain personal information or QR code content.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            PolicySection(
                title = "6. Data Retention & Deletion",
                body = "Because all data resides strictly on your local device, you can delete individual records directly from the Vault or wipe your entire repository via Settings > Clear Vault. If you uninstall QR Kit, all associated local data and database files are permanently purged from your device immediately.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            PolicySection(
                title = "7. Security Safeguards",
                body = "We leverage standard Android security sandboxing protocols to ensure that QR Kit's local storage is protected against unauthorized access by other third-party applications installed on your system.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            PolicySection(
                title = "8. Policy Updates",
                body = "We may update this Privacy Policy periodically to reflect new features or regulatory requirements. Any modifications will be reflected directly within the app with an updated effective date.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            PolicySection(
                title = "9. Contact Us",
                body = "If you have questions, feedback, or concerns regarding this Privacy Policy, please reach out to us using the Help & Feedback option found in the Settings page.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    body: String,
    textColor: Color,
    bodyTextColor: Color
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            color = bodyTextColor
        )
    }
}

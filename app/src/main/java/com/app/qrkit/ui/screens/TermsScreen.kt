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
fun TermsScreen(onBackClick: () -> Unit) {
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
                        "Terms of Service", 
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

            TermsSection(
                title = "1. Agreement to Terms",
                body = "By downloading, installing, accessing, or using QR Kit, you agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use the application.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "2. Description of the Application",
                body = "QR Kit is an offline-first QR code utility and management application. It provides tools to scan, generate, customize, organize, and export QR codes and barcodes for personal and commercial workflows. All core processing, encryption, and storage mechanisms operate locally on your device.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "3. User Conduct & Acceptable Use",
                body = "You agree to use QR Kit only for lawful purposes. You shall not generate, share, or distribute QR codes that:\n\n• Point to fraudulent, phishing, deceptive, or malware-infected destinations.\n• Facilitate illegal activities, copyright infringement, or malicious exploits.\n• Encode abusive, threatening, defamatory, or hateful content.\n• Violate applicable local, state, national, or international laws and regulations.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "4. Scanned Content & Third-Party Destinations",
                body = "QR Kit decodes text, URLs, and data embedded in physical or digital barcodes. QR Kit has no control over the content, security, or privacy policies of third-party websites or services linked by scanned codes.\n\nYou acknowledge that opening links or executing actions derived from scanned codes is done entirely at your own risk. Always verify the safety and authenticity of destination URLs before proceeding.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "5. Offline Data Storage & User Responsibility",
                body = "Because QR Kit stores your codes, categories, and scan history directly on your local device without centralized cloud syncing, you are solely responsible for maintaining device backups and safeguarding access to your device. We cannot recover data that has been deleted by you or lost due to device resets or uninstalls.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "6. Intellectual Property Rights",
                body = "The QR Kit name, interface designs, icons, custom styling algorithms, and underlying source code are the intellectual property of the developers. You retain full ownership of any original content, data, or media that you encode and produce with the application.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "7. Disclaimer of Warranties",
                body = "QR Kit is provided on an \"AS IS\" and \"AS AVAILABLE\" basis without warranties of any kind, whether express or implied. We do not warrant that the application will be uninterrupted, error-free, or compatible with every camera hardware configuration or barcode standard under extreme environmental conditions.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "8. Limitation of Liability",
                body = "To the fullest extent permitted by applicable law, in no event shall QR Kit or its creators be liable for any indirect, incidental, special, consequential, or punitive damages arising from your access to, use of, or inability to use the application or any content decoded therein.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "9. Modifications to Terms",
                body = "We reserve the right to modify these Terms of Service at any time. Any updates will be reflected within the application with an updated effective date. Continued use of the application following any changes constitutes acceptance of the new terms.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            TermsSection(
                title = "10. Contact Information",
                body = "If you have any questions, suggestions, or concerns regarding these Terms of Service, please contact us through the Help & Feedback section in the app settings.",
                textColor = textColor,
                bodyTextColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TermsSection(
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

package com.app.qrkit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.qrkit.ui.icons.ArrowBackIcon
import com.app.qrkit.ui.theme.InterFontFamily
import com.app.qrkit.ui.theme.isAppInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isAppInDarkTheme()

    val screenBg = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF7F7F8)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)
    val goldColor = if (isDark) Color(0xFFFFD54F) else Color(0xFFD97706)

    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Premium",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            ArrowBackIcon,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Glowing Star Icon Badge
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                goldColor.copy(alpha = 0.35f),
                                goldColor.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = goldColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, goldColor.copy(alpha = 0.5f)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = goldColor,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Coming Soon Badge
            Surface(
                shape = CircleShape,
                color = goldColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "COMING SOON",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    color = goldColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "QR Kit Premium",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Exclusive VIP presets, advanced styling tools, and cloud capabilities are currently being developed for future releases.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = InterFontFamily,
                color = textColor.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Feature Preview Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumFeatureItem(
                    icon = Icons.Rounded.AutoAwesome,
                    iconTint = goldColor,
                    title = "Curated Designer Presets",
                    description = "Handcrafted professional templates with custom themes, frames, and color palettes ready in 1 tap.",
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textColor = textColor
                )

                PremiumFeatureItem(
                    icon = Icons.Rounded.Palette,
                    iconTint = Color(0xFF64B5F6),
                    title = "Pro Customization Hub",
                    description = "High-resolution vector SVG exports, custom gradient styling, and custom brand watermark logos.",
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textColor = textColor
                )

                PremiumFeatureItem(
                    icon = Icons.Rounded.CloudSync,
                    iconTint = Color(0xFF81C784),
                    title = "Cloud Sync & Backup",
                    description = "Securely backup and synchronize your saved QR codes and custom templates across all your devices.",
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textColor = textColor
                )

                PremiumFeatureItem(
                    icon = Icons.Rounded.Speed,
                    iconTint = Color(0xFFFF8A65),
                    title = "Bulk QR Generator",
                    description = "Generate and export dozens or hundreds of styled QR codes simultaneously with zero limits.",
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textColor = textColor
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = {
                    Toast.makeText(context, "We'll notify you when Premium launches!", Toast.LENGTH_SHORT).show()
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Stay Tuned",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PremiumFeatureItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    cardBg: Color,
    cardBorder: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = InterFontFamily,
                    color = textColor.copy(alpha = 0.65f),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

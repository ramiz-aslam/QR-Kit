package com.app.qrkit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.qrkit.R
import com.app.qrkit.ui.components.scaleOnPress
import com.app.qrkit.ui.theme.InterFontFamily
import com.app.qrkit.ui.theme.UpdateSystemBars
import com.app.qrkit.ui.theme.isAppInDarkTheme

private data class OnboardingPoint(
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val isDark = isAppInDarkTheme()
    val isDynamic = com.app.qrkit.ui.theme.LocalAppDynamicColor.current

    val bgColor = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val descTextColor = if (isDark) Color(0xFFA6A6AA) else Color(0xFF505055)
    val dotColor = if (isDark) Color.White else Color.Black

    val buttonBgColor = if (isDynamic) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDark) Color.White else Color.Black
    }
    val buttonTextColor = if (isDynamic) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        if (isDark) Color.Black else Color.White
    }

    UpdateSystemBars(
        darkStatusIcons = !isDark,
        darkNavigationIcons = !isDark
    )

    val points = remember {
        listOf(
            OnboardingPoint("Instant Scanning", "Lightning-fast camera detection for all QR and barcode formats."),
            OnboardingPoint("Custom Studio", "Create bespoke QR codes with personalized colors, shapes, and styles."),
            OnboardingPoint("Secure Vault", "Organize, search, and manage all your saved codes in one safe place."),
            OnboardingPoint("100% Offline", "Complete privacy with on-device processing — your data never leaves your phone.")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Center Content: Logo + App Name Title (Side by Side) + Bullet Point Description
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Header: App Icon Logo + App Name Title Side by Side
            val logoRes = if (isDark) R.drawable.onboarding_logo_dark else R.drawable.onboarding_logo_light
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(86.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "QR Kit Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "QR KIT",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    letterSpacing = 0.5.sp,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Bullet Point Description
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                points.forEach { point ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Black bullet point dot (white in dark mode for contrast)
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(6.5.dp)
                                .background(dotColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                ) {
                                    append(point.title)
                                    append(" — ")
                                }
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Normal,
                                        color = descTextColor
                                    )
                                ) {
                                    append(point.description)
                                }
                            },
                            fontFamily = InterFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            letterSpacing = 0.1.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Pinned Bottom Action: Solid capsule "Continue" button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            val interactionSource = remember { MutableInteractionSource() }

            Surface(
                onClick = onFinish,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scaleOnPress(interactionSource)
                    .shadow(
                        elevation = if (isDark) 6.dp else 10.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    ),
                shape = CircleShape,
                color = buttonBgColor
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continue",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp,
                        color = buttonTextColor
                    )
                }
            }
        }
    }
}

package com.app.qrkit.ui.screens

import com.app.qrkit.ui.components.modernShadow

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.qrkit.R
import com.app.qrkit.data.QrCode
import com.app.qrkit.ui.components.InlineColorPicker
import com.app.qrkit.ui.components.QrLogoShape
import com.app.qrkit.ui.components.QrFrameStyle
import com.app.qrkit.ui.components.QrFrameRenderer
import com.app.qrkit.ui.components.ScaleOnPressFilledIconButton
import com.app.qrkit.ui.icons.*

import com.app.qrkit.ui.theme.isAppInDarkTheme
import com.app.qrkit.ui.QrViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Color as ComposeColor
import java.util.UUID
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.app.qrkit.ui.components.QrEyeShape
import com.app.qrkit.ui.components.QrPixelShape
import com.app.qrkit.ui.components.QrDesignRenderer
import com.app.qrkit.ui.icons.QrCodeScannerIcon

enum class QrCustomizationTab(val title: String) {
    COLOR("Color"),
    CORNERS("Corners"),
    LOGO("Logo"),
    FRAME("Frame"),
    TEST("Test");
}

enum class QrTestStatus {
    NOT_TESTED,
    TESTING,
    PASSED,
    FAILED
}

fun getContentLabel(type: String): String {
    return when (type) {
        "TEXT" -> "Enter text"
        "URL" -> "Enter URL"
        "WIFI" -> "WiFi Network Name (SSID)"
        "EMAIL" -> "Email Address"
        "PHONE" -> "Phone Number"
        "LOCATION" -> "Location"
        "CONTACT" -> "Contact"
        "EVENT" -> "Event Details"
        "SMS" -> "Phone Number,Message"
        else -> "Enter content"
    }
}

fun buildVCard(
    firstName: String,
    lastName: String,
    phone: String,
    email: String,
    company: String,
    jobTitle: String,
    address: String,
    website: String,
    note: String
): String {
    val fn = when {
        firstName.isNotBlank() && lastName.isNotBlank() -> "${firstName.trim()} ${lastName.trim()}"
        firstName.isNotBlank() -> firstName.trim()
        lastName.isNotBlank() -> lastName.trim()
        company.isNotBlank() -> company.trim()
        else -> ""
    }
    val n = "${lastName.trim()};${firstName.trim()};;;"

    val sb = StringBuilder()
    sb.append("BEGIN:VCARD\n")
    sb.append("VERSION:3.0\n")
    if (fn.isNotBlank()) {
        sb.append("FN:$fn\n")
        sb.append("N:$n\n")
    }
    if (company.isNotBlank()) sb.append("ORG:${company.trim()}\n")
    if (jobTitle.isNotBlank()) sb.append("TITLE:${jobTitle.trim()}\n")
    if (phone.isNotBlank()) sb.append("TEL;TYPE=CELL:${phone.trim()}\n")
    if (email.isNotBlank()) sb.append("EMAIL;TYPE=WORK:${email.trim()}\n")
    if (address.isNotBlank()) sb.append("ADR;TYPE=WORK:;;${address.trim()};;;;\n")
    if (website.isNotBlank()) {
        val safeUrl = if (website.startsWith("http://") || website.startsWith("https://")) website.trim() else "https://${website.trim()}"
        sb.append("URL:$safeUrl\n")
    }
    if (note.isNotBlank()) sb.append("NOTE:${note.trim()}\n")
    sb.append("END:VCARD")
    return sb.toString()
}

fun buildLocationGeo(lat: String, lng: String, query: String): String {
    val cleanLat = lat.trim()
    val cleanLng = lng.trim()
    val cleanQuery = query.trim()
    return if (cleanLat.isNotEmpty() && cleanLng.isNotEmpty()) {
        if (cleanQuery.isNotEmpty()) {
            "geo:$cleanLat,$cleanLng?q=${android.net.Uri.encode(cleanQuery)}"
        } else {
            "geo:$cleanLat,$cleanLng"
        }
    } else if (cleanQuery.isNotEmpty()) {
        "geo:0,0?q=${android.net.Uri.encode(cleanQuery)}"
    } else {
        ""
    }
}

val QrTypes = listOf("URL", "TEXT", "WIFI", "EMAIL", "PHONE", "LOCATION", "CONTACT")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQrScreen(
    viewModel: QrViewModel,
    qrToEdit: QrCode? = null,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    com.app.qrkit.ui.theme.UpdateSystemBars(darkStatusIcons = !isDark, darkNavigationIcons = !isDark)
    val containerColor = if (isDark) ComposeColor.Black else ComposeColor.White
    val textColor = if (isDark) ComposeColor.White else ComposeColor.Black
    val screenBgColor = if (isDark) ComposeColor.Black else ComposeColor.White
    val containerBgColor = if (isDark) ComposeColor(0xFF1E1E1E) else ComposeColor(0xFFF2F2F2)
    val buttonBgColor = if (isDark) ComposeColor(0xFF242424) else ComposeColor(0xFFF2F2F2)
    val contentBoxColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = ComposeColor.White,
        unfocusedContainerColor = ComposeColor.White,
        disabledContainerColor = ComposeColor.White,
        errorContainerColor = ComposeColor.White,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedTextColor = ComposeColor.Black,
        unfocusedTextColor = ComposeColor.Black,
        disabledTextColor = ComposeColor.Black.copy(alpha = 0.6f),
        errorTextColor = ComposeColor.Black,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = ComposeColor(0xFF555555),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedPlaceholderColor = ComposeColor(0xFF888888),
        unfocusedPlaceholderColor = ComposeColor(0xFF888888),
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = ComposeColor(0xFF555555),
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = ComposeColor(0xFF555555)
    )
    
    val coroutineScope = rememberCoroutineScope()
    
    var currentStep by remember { mutableIntStateOf(1) }
    
    var name by remember { mutableStateOf(qrToEdit?.categoryName ?: "") }
    var type by remember { mutableStateOf(qrToEdit?.type ?: "URL") }
    
    // Generic / URL / Text / WiFi SSID
    var actualContent by remember {
        mutableStateOf(
            if (qrToEdit?.type == "WIFI") {
                Regex("S:([^;]+)").find(qrToEdit.content)?.groupValues?.get(1) ?: ""
            } else if (qrToEdit?.type == "LOCATION" || qrToEdit?.type == "CONTACT") {
                ""
            } else {
                qrToEdit?.content ?: ""
            }
        )
    }
    var wifiPass by remember {
        mutableStateOf(
            if (qrToEdit?.type == "WIFI") {
                Regex("P:([^;]+)").find(qrToEdit.content)?.groupValues?.get(1) ?: ""
            } else ""
        )
    }

    // Real Location Data Fields
    var locLatitude by remember {
        mutableStateOf(
            if (qrToEdit?.type == "LOCATION") {
                val geoMatch = Regex("geo:([-\\d.]+),([-\\d.]+)").find(qrToEdit.content)
                geoMatch?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var locLongitude by remember {
        mutableStateOf(
            if (qrToEdit?.type == "LOCATION") {
                val geoMatch = Regex("geo:([-\\d.]+),([-\\d.]+)").find(qrToEdit.content)
                geoMatch?.groupValues?.get(2) ?: ""
            } else ""
        )
    }
    var locPlaceName by remember {
        mutableStateOf(
            if (qrToEdit?.type == "LOCATION") {
                val queryMatch = Regex("[?&]q=([^&]+)").find(qrToEdit.content)
                queryMatch?.groupValues?.get(1)?.let { android.net.Uri.decode(it) } ?: ""
            } else ""
        )
    }

    // Real Contact Data Fields
    var contactFirstName by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                val nMatch = Regex("N:[^;]*;([^;\\n\\r]*)").find(qrToEdit.content)
                nMatch?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var contactLastName by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                val nMatch = Regex("N:([^;\\n\\r]*)").find(qrToEdit.content)
                nMatch?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var contactPhone by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                Regex("TEL[^:]*:([^\\n\\r]+)").find(qrToEdit.content)?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var contactEmail by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                Regex("EMAIL[^:]*:([^\\n\\r]+)").find(qrToEdit.content)?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var contactCompany by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                Regex("ORG:([^\\n\\r]+)").find(qrToEdit.content)?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var contactJobTitle by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                Regex("TITLE:([^\\n\\r]+)").find(qrToEdit.content)?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var contactAddress by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                val adrMatch = Regex("ADR[^:]*:(?:;;)?([^;\\n\\r]+)").find(qrToEdit.content)
                adrMatch?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var contactWebsite by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                Regex("URL:([^\\n\\r]+)").find(qrToEdit.content)?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    var contactNote by remember {
        mutableStateOf(
            if (qrToEdit?.type == "CONTACT") {
                Regex("NOTE:([^\\n\\r]+)").find(qrToEdit.content)?.groupValues?.get(1) ?: ""
            } else ""
        )
    }
    
    var eyeColor by remember { mutableStateOf(ComposeColor.Black) }
    var pixelColor by remember { mutableStateOf(ComposeColor.Black) }
    var eyeRadius by remember { mutableFloatStateOf(0f) }
    var pixelRadius by remember { mutableFloatStateOf(0f) }
    var bgColor by remember { mutableStateOf(if (qrToEdit != null) ComposeColor(qrToEdit.bgColor) else ComposeColor.White) }
    var bgRadius by remember { mutableFloatStateOf(qrToEdit?.bgRadius ?: 0f) }
    var selectedEyeShape by remember { mutableStateOf(QrEyeShape.DEFAULT) }
    var selectedPixelShape by remember { mutableStateOf(QrPixelShape.DEFAULT) }
    
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    var selectedCustomizationTab by remember { mutableStateOf(QrCustomizationTab.COLOR) }
    var showCustomColorPickerFor by remember { mutableStateOf<String?>(null) }
    var selectedLogo by remember { mutableStateOf("none") }
    var selectedLogoShape by remember { mutableStateOf(com.app.qrkit.ui.components.QrLogoShape.ROUNDED) }
    
    val initialFrameStyle = if (qrToEdit?.templateId?.startsWith("frame_") == true) {
        val frameId = qrToEdit.templateId.removePrefix("frame_")
        QrFrameStyle.values().firstOrNull { it.id == frameId } ?: QrFrameStyle.NONE
    } else {
        QrFrameStyle.NONE
    }
    var selectedFrameStyle by remember { mutableStateOf(initialFrameStyle) }
    val defaultCtaForType = when (type) {
        "WIFI" -> "CONNECT TO WI-FI"
        "PHONE" -> "CALL US"
        "EMAIL" -> "EMAIL US"
        "CONTACT" -> "SAVE CONTACT"
        "LOCATION" -> "FIND US"
        else -> "SCAN ME"
    }
    var frameCtaText by remember { mutableStateOf(defaultCtaForType) }
    var frameColor by remember { mutableStateOf(pixelColor) }
    var rawQrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var cachedBitMatrixContent by remember { mutableStateOf<String?>(null) }
    var cachedBitMatrixEc by remember { mutableStateOf<com.google.zxing.qrcode.decoder.ErrorCorrectionLevel?>(null) }
    var cachedBitMatrix by remember { mutableStateOf<com.google.zxing.common.BitMatrix?>(null) }
    
    val previewContent = remember(
        type, actualContent, wifiPass,
        locLatitude, locLongitude, locPlaceName,
        contactFirstName, contactLastName, contactPhone, contactEmail,
        contactCompany, contactJobTitle, contactAddress, contactWebsite, contactNote
    ) {
        when (type) {
            "WIFI" -> if (actualContent.isNotBlank()) "WIFI:S:$actualContent;T:WPA;P:$wifiPass;;" else ""
            "LOCATION" -> buildLocationGeo(locLatitude, locLongitude, locPlaceName)
            "CONTACT" -> {
                val hasAny = contactFirstName.isNotBlank() || contactLastName.isNotBlank() ||
                    contactPhone.isNotBlank() || contactEmail.isNotBlank() ||
                    contactCompany.isNotBlank() || contactJobTitle.isNotBlank()
                if (hasAny) {
                    buildVCard(
                        contactFirstName, contactLastName, contactPhone, contactEmail,
                        contactCompany, contactJobTitle, contactAddress, contactWebsite, contactNote
                    )
                } else ""
            }
            else -> actualContent
        }
    }
    
    val context = LocalContext.current

    LaunchedEffect(
        previewContent, eyeColor, pixelColor, eyeRadius, pixelRadius, bgColor, bgRadius,
        selectedEyeShape, selectedPixelShape, selectedLogo, selectedLogoShape,
        selectedFrameStyle, frameCtaText, frameColor, context
    ) {
        if (previewContent.isNotBlank()) {
            withContext(Dispatchers.Default) {
                try {
                    val finalBmp = QrDesignRenderer.renderQr(
                        context = context,
                        content = previewContent,
                        eyeColor = eyeColor.toArgb(),
                        pixelColor = pixelColor.toArgb(),
                        bgColor = bgColor.toArgb(),
                        eyeRadius = eyeRadius,
                        pixelRadius = pixelRadius,
                        bgRadius = bgRadius,
                        eyeShape = selectedEyeShape,
                        pixelShape = selectedPixelShape,
                        logoId = selectedLogo,
                        logoShape = selectedLogoShape,
                        frameStyle = selectedFrameStyle,
                        frameCtaText = frameCtaText,
                        frameColor = frameColor.toArgb(),
                        canvasSize = 1024
                    )
                    bitmap = finalBmp
                } catch (e: Exception) {
                    e.printStackTrace()
                    bitmap = null
                }
            }
        } else {
            bitmap = null
        }
    }
    var testStatus by remember { mutableStateOf(QrTestStatus.NOT_TESTED) }
    var scannedContent by remember { mutableStateOf<String?>(null) }
    var scannedBarcodeType by remember { mutableStateOf<String?>(null) }
    var testErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(
        previewContent, eyeColor, pixelColor, eyeRadius, pixelRadius, bgColor, bgRadius,
        selectedEyeShape, selectedPixelShape, selectedLogo, selectedLogoShape,
        selectedFrameStyle, frameCtaText, frameColor
    ) {
        testStatus = QrTestStatus.NOT_TESTED
        scannedContent = null
        scannedBarcodeType = null
        testErrorMessage = null
    }

    val runQrTest = {
        val bmp = bitmap
        if (bmp == null) {
            testStatus = QrTestStatus.FAILED
            testErrorMessage = "QR Bitmap is not generated yet."
        } else {
            testStatus = QrTestStatus.TESTING
            testErrorMessage = null

            try {
                val scanner = BarcodeScanning.getClient()
                val image = InputImage.fromBitmap(bmp, 0)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val detected = barcodes.firstOrNull { it.rawValue != null }
                        if (detected != null && !detected.rawValue.isNullOrBlank()) {
                            scannedContent = detected.rawValue
                            scannedBarcodeType = when (detected.valueType) {
                                Barcode.TYPE_URL -> "URL"
                                Barcode.TYPE_WIFI -> "Wi-Fi"
                                Barcode.TYPE_CONTACT_INFO -> "Contact"
                                Barcode.TYPE_GEO -> "Location"
                                Barcode.TYPE_PHONE -> "Phone"
                                Barcode.TYPE_EMAIL -> "Email"
                                Barcode.TYPE_SMS -> "SMS"
                                else -> "Text"
                            }
                            testStatus = QrTestStatus.PASSED
                        } else {
                            val zxingResult = tryZxingDecode(bmp)
                            if (zxingResult != null) {
                                scannedContent = zxingResult
                                scannedBarcodeType = type
                                testStatus = QrTestStatus.PASSED
                            } else {
                                testStatus = QrTestStatus.FAILED
                                testErrorMessage = "The scanner could not decode the code. Contrast may be too low, or corner radius is too high."
                            }
                        }
                    }
                    .addOnFailureListener {
                        val zxingResult = tryZxingDecode(bmp)
                        if (zxingResult != null) {
                            scannedContent = zxingResult
                            scannedBarcodeType = type
                            testStatus = QrTestStatus.PASSED
                        } else {
                            testStatus = QrTestStatus.FAILED
                            testErrorMessage = "Unable to decode QR code. Please ensure high contrast between colors."
                        }
                    }
            } catch (e: Exception) {
                val zxingResult = tryZxingDecode(bmp)
                if (zxingResult != null) {
                    scannedContent = zxingResult
                    scannedBarcodeType = type
                    testStatus = QrTestStatus.PASSED
                } else {
                    testStatus = QrTestStatus.FAILED
                    testErrorMessage = e.localizedMessage ?: "Failed to scan QR code."
                }
            }
        }
    }

    val canSave = previewContent.isNotBlank() && bitmap != null && testStatus == QrTestStatus.PASSED

    val handleSave = {
        if (canSave) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val imageFileName = "qr_" + System.currentTimeMillis() + ".png"
                    val file = java.io.File(context.filesDir, imageFileName)
                    bitmap?.let { b ->
                        java.io.FileOutputStream(file).use { out ->
                            b.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }
                    
                    val finalName = name.trim().ifBlank { "QR Code" }
                    
                    val newQr = QrCode(
                        id = qrToEdit?.id ?: UUID.randomUUID().toString(),
                        content = previewContent,
                        type = type,
                        categoryName = finalName,
                        imagePath = file.absolutePath,
                        isGenerated = true,
                        templateId = if (selectedFrameStyle != QrFrameStyle.NONE) "frame_${selectedFrameStyle.id}" else null,
                        createdAt = qrToEdit?.createdAt ?: System.currentTimeMillis(),
                        bgColor = bgColor.toArgb().toLong(),
                        bgRadius = if (selectedFrameStyle != QrFrameStyle.NONE) -1f else bgRadius
                    )
                    viewModel.insertQrCode(newQr)
                }
                
                onSuccess()
            }
        } else if (testStatus != QrTestStatus.PASSED) {
            selectedCustomizationTab = QrCustomizationTab.TEST
            android.widget.Toast.makeText(
                context, 
                if (testStatus == QrTestStatus.FAILED) "QR code failed readability test. Please fix errors and re-test." else "Please test the QR code before saving.", 
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    val isFormValid = when (type) {
        "URL" -> actualContent.isNotBlank() && (actualContent.startsWith("http://") || actualContent.startsWith("https://") || actualContent.contains("."))
        "WIFI" -> actualContent.isNotBlank() && wifiPass.isNotBlank()
        "EMAIL" -> actualContent.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(actualContent).matches()
        "PHONE" -> actualContent.isNotBlank() && android.util.Patterns.PHONE.matcher(actualContent).matches()
        "LOCATION" -> {
            val lat = locLatitude.trim()
            val lng = locLongitude.trim()
            (lat.isNotEmpty() && lng.isNotEmpty() && lat.toDoubleOrNull() != null && lng.toDoubleOrNull() != null) || locPlaceName.isNotBlank()
        }
        "CONTACT" -> {
            contactFirstName.isNotBlank() || contactLastName.isNotBlank() ||
            contactPhone.isNotBlank() || contactEmail.isNotBlank() ||
            contactCompany.isNotBlank()
        }
        else -> actualContent.isNotBlank()
    }
    
    Scaffold(
        containerColor = screenBgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { if (currentStep == 2) currentStep = 1 else onBackClick() },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(ArrowBackIcon, contentDescription = "Back", modifier = Modifier.size(28.dp), tint = textColor)
                    }
                },
                actions = {
                    if (currentStep == 1) {
                        Button(
                            onClick = { currentStep = 2 },
                            enabled = isFormValid,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = "Next",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else if (currentStep == 2) {
                        Button(
                            onClick = {
                                if (canSave) {
                                    handleSave()
                                } else {
                                    selectedCustomizationTab = QrCustomizationTab.TEST
                                    android.widget.Toast.makeText(
                                        context,
                                        if (testStatus == QrTestStatus.FAILED) "QR code failed readability test. Please fix errors and re-test." else "Please test the QR code before saving.",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canSave) MaterialTheme.colorScheme.primary else (if (isDark) ComposeColor(0xFF2A2A2A) else ComposeColor(0xFFE0E0E0)),
                                contentColor = if (canSave) MaterialTheme.colorScheme.onPrimary else (if (isDark) ComposeColor.White.copy(alpha = 0.45f) else ComposeColor.Black.copy(alpha = 0.45f))
                            )
                        ) {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBgColor,
                    scrolledContainerColor = screenBgColor
                )
            )
        }
    ) { padding ->
        AnimatedContent(targetState = currentStep, label = "StepAnim") { step ->
            if (step == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding())
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // 1. Container for QR types
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = containerBgColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(24.dp), ambientColor = ComposeColor.Black.copy(alpha = 0.08f), spotColor = ComposeColor.Black.copy(alpha = 0.04f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            QrTypes.chunked(4).forEach { rowItems ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    rowItems.forEach { cat ->
                                        val isSelected = type == cat
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else ComposeColor.Transparent,
                                            modifier = Modifier.weight(1f).clickable { 
                                                type = cat
                                            }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(vertical = 12.dp)
                                            ) {
                                                val icon = when(cat) {
                                                    "URL" -> LinkIcon
                                                    "WIFI" -> WifiIcon
                                                    "EMAIL" -> MailIcon
                                                    "PHONE" -> CallIcon
                                                    "LOCATION" -> LocationOnIcon
                                                    "CONTACT" -> PersonIcon
                                                    else -> TextAdIcon
                                                }
                                                val contentColor = if (isSelected) {
                                                    androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    if (isDark) ComposeColor.White else ComposeColor.Black
                                                }
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = cat,
                                                    tint = contentColor,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = cat,
                                                    color = contentColor,
                                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    if (rowItems.size < 4) {
                                        repeat(4 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("QR Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = contentBoxColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    when (type) {
                        "LOCATION" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = locLatitude,
                                    onValueChange = { input ->
                                        if (input.contains(",")) {
                                            val parts = input.split(",")
                                            locLatitude = parts[0].trim()
                                            if (parts.size > 1) locLongitude = parts[1].trim()
                                        } else {
                                            locLatitude = input
                                        }
                                    },
                                    label = { Text("Latitude") },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = contentBoxColors
                                )
                                OutlinedTextField(
                                    value = locLongitude,
                                    onValueChange = { locLongitude = it },
                                    label = { Text("Longitude") },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = contentBoxColors
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = locPlaceName,
                                onValueChange = { locPlaceName = it },
                                label = { Text("Location Name / Address (Optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = contentBoxColors
                            )
                        }
                        "CONTACT" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = contactFirstName,
                                    onValueChange = { contactFirstName = it },
                                    label = { Text("First Name") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = contentBoxColors
                                )
                                OutlinedTextField(
                                    value = contactLastName,
                                    onValueChange = { contactLastName = it },
                                    label = { Text("Last Name") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = contentBoxColors
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = contentBoxColors
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactEmail,
                                onValueChange = { contactEmail = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = contentBoxColors
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = contactCompany,
                                    onValueChange = { contactCompany = it },
                                    label = { Text("Company") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = contentBoxColors
                                )
                                OutlinedTextField(
                                    value = contactJobTitle,
                                    onValueChange = { contactJobTitle = it },
                                    label = { Text("Job Title") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = contentBoxColors
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactAddress,
                                onValueChange = { contactAddress = it },
                                label = { Text("Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = contentBoxColors
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactWebsite,
                                onValueChange = { contactWebsite = it },
                                label = { Text("Website") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = contentBoxColors
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactNote,
                                onValueChange = { contactNote = it },
                                label = { Text("Note (Optional)") },
                                placeholder = { Text("Additional information...") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = contentBoxColors
                            )
                        }
                        else -> {
                            OutlinedTextField(
                                value = actualContent,
                                onValueChange = { actualContent = it },
                                label = { Text(getContentLabel(type)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = type != "TEXT",
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = when(type) {
                                        "URL" -> androidx.compose.ui.text.input.KeyboardType.Uri
                                        "EMAIL" -> androidx.compose.ui.text.input.KeyboardType.Email
                                        "PHONE" -> androidx.compose.ui.text.input.KeyboardType.Phone
                                        else -> androidx.compose.ui.text.input.KeyboardType.Text
                                    }
                                ),
                                shape = RoundedCornerShape(20.dp),
                                colors = contentBoxColors
                            )
                            if (type == "WIFI") {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = wifiPass,
                                    onValueChange = { wifiPass = it },
                                    label = { Text("WiFi Password") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = contentBoxColors
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding())
                ) {
                    // 1. QR Preview (Upper Half)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        bitmap?.let { b ->
                            val isFramed = selectedFrameStyle != QrFrameStyle.NONE
                            if (isFramed) {
                                Image(
                                    bitmap = b.asImageBitmap(),
                                    contentDescription = "Live Generated QR",
                                    modifier = Modifier
                                        .fillMaxHeight(0.95f)
                                        .aspectRatio(b.width.toFloat() / b.height.toFloat()),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                val previewCornerPercent = (bgRadius * 50).coerceIn(0f, 50f).toInt()
                                val previewShape = RoundedCornerShape(percent = previewCornerPercent)
                                Card(
                                    modifier = Modifier.size(220.dp),
                                    shape = previewShape,
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = if (previewCornerPercent > 0) 4.dp else 2.dp
                                    ),
                                    colors = CardDefaults.cardColors(containerColor = ComposeColor.Transparent)
                                ) {
                                    Image(
                                        bitmap = b.asImageBitmap(),
                                        contentDescription = "Live Generated QR",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        } ?: run {
                            Box(modifier = Modifier.size(220.dp))
                        }
                    }

                    // 2. Customization Controls Section (seamless transparent page)
                    val dividerColor = if (isDark) ComposeColor.White.copy(alpha = 0.1f) else ComposeColor.Black.copy(alpha = 0.06f)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f)
                    ) {
                        // Top divider where customization bottom section starts, above tabs
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = dividerColor
                        )

                        // Tabs Section at top of sheet
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QrCustomizationTab.values().forEach { tab ->
                                val isSelected = selectedCustomizationTab == tab
                                val isPassed = tab == QrCustomizationTab.TEST && testStatus == QrTestStatus.PASSED
                                val isFailed = tab == QrCustomizationTab.TEST && testStatus == QrTestStatus.FAILED

                                val pillColor = if (isSelected) {
                                    if (isPassed) ComposeColor(0xFF2E7D32)
                                    else if (isFailed) ComposeColor(0xFFC62828)
                                    else MaterialTheme.colorScheme.primary
                                } else {
                                    if (isDark) ComposeColor(0xFF353535) else ComposeColor.White
                                }

                                val pillBorder = if (isSelected) {
                                    null
                                } else {
                                    if (isPassed) BorderStroke(1.dp, ComposeColor(0xFF4CAF50).copy(alpha = 0.5f))
                                    else if (isFailed) BorderStroke(1.dp, ComposeColor(0xFFE53935).copy(alpha = 0.5f))
                                    else BorderStroke(1.dp, if (isDark) ComposeColor.White.copy(alpha = 0.15f) else ComposeColor.Black.copy(alpha = 0.08f))
                                }

                                val contentColor = if (isSelected) {
                                    ComposeColor.White
                                } else {
                                    if (isPassed) ComposeColor(0xFF4CAF50)
                                    else if (isFailed) ComposeColor(0xFFE53935)
                                    else textColor.copy(alpha = 0.7f)
                                }

                                Surface(
                                    onClick = { selectedCustomizationTab = tab },
                                    shape = CircleShape,
                                    color = pillColor,
                                    border = pillBorder,
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isPassed) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Passed",
                                                modifier = Modifier.size(16.dp),
                                                tint = contentColor
                                            )
                                        } else if (isFailed) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Failed",
                                                modifier = Modifier.size(16.dp),
                                                tint = contentColor
                                            )
                                        }
                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                        }

                        // Divider between the tabs and the tabs contents section
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = dividerColor
                            )

                            // Scrollable options section with transparent background
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 14.dp)
                            ) {
                                when (selectedCustomizationTab) {
                                    QrCustomizationTab.COLOR -> {
                                        ColorSettingRow(
                                            title = "Eye Color",
                                            color = eyeColor,
                                            textColor = textColor,
                                            isDark = isDark,
                                            onClick = { showCustomColorPickerFor = "eye" }
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 6.dp),
                                            thickness = 1.dp,
                                            color = dividerColor
                                        )

                                        ColorSettingRow(
                                            title = "Pixel Color",
                                            color = pixelColor,
                                            textColor = textColor,
                                            isDark = isDark,
                                            onClick = { showCustomColorPickerFor = "pixel" }
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 6.dp),
                                            thickness = 1.dp,
                                            color = dividerColor
                                        )

                                        ColorSettingRow(
                                            title = "Surface Color",
                                            color = bgColor,
                                            textColor = textColor,
                                            isDark = isDark,
                                            onClick = { showCustomColorPickerFor = "background" }
                                        )
                                    }

                                    QrCustomizationTab.CORNERS -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp)
                                        ) {
                                            // Eye Radius
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Eye Radius",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                                Text(
                                                    text = "${(eyeRadius * 100).toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textColor.copy(alpha = 0.6f)
                                                )
                                            }
                                            Slider(
                                                value = eyeRadius,
                                                onValueChange = { 
                                                    eyeRadius = it
                                                    selectedEyeShape = QrEyeShape.DEFAULT
                                                },
                                                valueRange = 0f..1f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                                    inactiveTrackColor = if (isDark) ComposeColor.White.copy(alpha = 0.15f) else ComposeColor.Black.copy(alpha = 0.1f)
                                                )
                                            )

                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                thickness = 1.dp,
                                                color = dividerColor
                                            )

                                            // Pixel Radius
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Pixel Radius",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                                Text(
                                                    text = "${(pixelRadius * 100).toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textColor.copy(alpha = 0.6f)
                                                )
                                            }
                                            Slider(
                                                value = pixelRadius,
                                                onValueChange = { 
                                                    pixelRadius = it
                                                    selectedPixelShape = QrPixelShape.DEFAULT
                                                },
                                                valueRange = 0f..1f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                                    inactiveTrackColor = if (isDark) ComposeColor.White.copy(alpha = 0.15f) else ComposeColor.Black.copy(alpha = 0.1f)
                                                )
                                            )

                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                thickness = 1.dp,
                                                color = dividerColor
                                            )

                                            // Surface Radius
                                            val isFrameActive = selectedFrameStyle != QrFrameStyle.NONE
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .alpha(if (isFrameActive) 0.38f else 1f),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Surface Radius",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textColor
                                                    )
                                                    if (isFrameActive) {
                                                        Text(
                                                            text = "(Frame Active)",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = textColor.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (isFrameActive) "N/A" else "${(bgRadius * 100).toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textColor.copy(alpha = 0.6f)
                                                )
                                            }
                                            Slider(
                                                value = bgRadius,
                                                onValueChange = { if (!isFrameActive) bgRadius = it },
                                                enabled = !isFrameActive,
                                                valueRange = 0f..1f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = if (isFrameActive) textColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = if (isFrameActive) textColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary,
                                                    inactiveTrackColor = if (isDark) ComposeColor.White.copy(alpha = 0.15f) else ComposeColor.Black.copy(alpha = 0.1f)
                                                ),
                                                modifier = Modifier.alpha(if (isFrameActive) 0.38f else 1f)
                                            )
                                        }
                                    }

                                    QrCustomizationTab.LOGO -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // 1. Logo Shape Selection
                                            Text(
                                                text = "Logo Shape",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor,
                                                modifier = Modifier.padding(horizontal = 20.dp)
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                com.app.qrkit.ui.components.QrLogoShape.values().forEach { shapeItem ->
                                                    val isShapeSelected = selectedLogoShape == shapeItem
                                                    val shapeCorner = when (shapeItem) {
                                                        com.app.qrkit.ui.components.QrLogoShape.SHARP -> RoundedCornerShape(0.dp)
                                                        com.app.qrkit.ui.components.QrLogoShape.ROUNDED -> RoundedCornerShape(4.dp)
                                                        com.app.qrkit.ui.components.QrLogoShape.CIRCLE -> CircleShape
                                                    }
                                                    Surface(
                                                        onClick = { selectedLogoShape = shapeItem },
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = if (isShapeSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                                else if (isDark) ComposeColor(0xFF222222) else ComposeColor(0xFFF6F6F7),
                                                        border = BorderStroke(
                                                            width = if (isShapeSelected) 2.dp else 1.dp,
                                                            color = if (isShapeSelected) MaterialTheme.colorScheme.primary
                                                                    else if (isDark) ComposeColor.White.copy(alpha = 0.1f) else ComposeColor.Black.copy(alpha = 0.08f)
                                                        ),
                                                        modifier = Modifier.weight(1f).height(46.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .clip(shapeCorner)
                                                                    .background(if (isShapeSelected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.6f))
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = shapeItem.title,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = if (isShapeSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isShapeSelected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.85f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp),
                                                thickness = 1.dp,
                                                color = dividerColor
                                            )

                                            // 2. Preset Icons
                                            Text(
                                                text = "Preset Icons",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor,
                                                modifier = Modifier.padding(horizontal = 20.dp)
                                            )

                                            val logoOptions = QrDesignRenderer.availableLogos

                                            LazyRow(
                                                contentPadding = PaddingValues(horizontal = 20.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(logoOptions) { (id, label, iconRes) ->
                                                    val isSelected = selectedLogo == id
                                                    Surface(
                                                        onClick = { selectedLogo = id },
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                                else if (isDark) ComposeColor(0xFF222222) else ComposeColor(0xFFF6F6F7),
                                                        border = BorderStroke(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                                                    else if (isDark) ComposeColor.White.copy(alpha = 0.1f) else ComposeColor.Black.copy(alpha = 0.08f)
                                                        ),
                                                        modifier = Modifier
                                                            .width(80.dp)
                                                            .height(82.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(6.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            if (iconRes != null) {
                                                                Image(
                                                                    painter = painterResource(id = iconRes),
                                                                    contentDescription = label,
                                                                    modifier = Modifier.size(28.dp)
                                                                )
                                                            } else {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.Block,
                                                                    contentDescription = "None",
                                                                    tint = textColor.copy(alpha = 0.6f),
                                                                    modifier = Modifier.size(28.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = label,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isSelected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.85f),
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    QrCustomizationTab.FRAME -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // 1. Frame Style Selector Title
                                            Text(
                                                text = "Frame Style",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor,
                                                modifier = Modifier.padding(horizontal = 20.dp)
                                            )

                                            // Horizontal Row of Frame Options (generator excludes the 5 new template styles)
                                            val frameStyles = listOf(
                                                QrFrameStyle.NONE,
                                                QrFrameStyle.BOTTOM_BANNER,
                                                QrFrameStyle.SMARTPHONE,
                                                QrFrameStyle.HEADER_BADGE,
                                                QrFrameStyle.SPEECH_BUBBLE,
                                                QrFrameStyle.TICKET,
                                                QrFrameStyle.RETRO
                                            )

                                            LazyRow(
                                                contentPadding = PaddingValues(horizontal = 20.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(frameStyles) { style ->
                                                    val isSelected = selectedFrameStyle == style
                                                    Surface(
                                                        onClick = {
                                                            selectedFrameStyle = style
                                                            if (style != QrFrameStyle.NONE && frameCtaText.isBlank()) {
                                                                frameCtaText = style.defaultText
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                                else if (isDark) ComposeColor(0xFF222222) else ComposeColor(0xFFF6F6F7),
                                                        border = BorderStroke(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                                                    else if (isDark) ComposeColor.White.copy(alpha = 0.1f) else ComposeColor.Black.copy(alpha = 0.08f)
                                                        ),
                                                        modifier = Modifier.width(96.dp).height(80.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(8.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = when (style) {
                                                                    QrFrameStyle.NONE -> Icons.Rounded.Block
                                                                    QrFrameStyle.BOTTOM_BANNER -> Icons.Rounded.CropPortrait
                                                                    QrFrameStyle.SMARTPHONE -> Icons.Rounded.Smartphone
                                                                    QrFrameStyle.HEADER_BADGE -> Icons.Rounded.Badge
                                                                    QrFrameStyle.SPEECH_BUBBLE -> Icons.Rounded.ChatBubbleOutline
                                                                    QrFrameStyle.TICKET -> Icons.Rounded.ConfirmationNumber
                                                                    QrFrameStyle.RETRO -> Icons.Rounded.FilterFrames
                                                                    QrFrameStyle.POLAROID -> Icons.Rounded.PhotoCamera
                                                                    QrFrameStyle.MINIMAL_BORDER -> Icons.Rounded.CropFree
                                                                    QrFrameStyle.NEON_CYBER -> Icons.Rounded.Bolt
                                                                    QrFrameStyle.ELEGANT_RIBBON -> Icons.Rounded.Bookmark
                                                                    QrFrameStyle.COFFEE_SHOP -> Icons.Rounded.Coffee
                                                                },
                                                                contentDescription = style.title,
                                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.7f),
                                                                modifier = Modifier.size(26.dp)
                                                            )
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Text(
                                                                text = style.title,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isSelected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.85f),
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            if (selectedFrameStyle != QrFrameStyle.NONE) {
                                                HorizontalDivider(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 20.dp),
                                                    thickness = 1.dp,
                                                    color = dividerColor
                                                )

                                                // 2. Banner Text Input
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Banner Text (Call to Action)",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textColor,
                                                        modifier = Modifier.padding(horizontal = 20.dp)
                                                    )

                                                    OutlinedTextField(
                                                        value = frameCtaText,
                                                        onValueChange = { frameCtaText = it },
                                                        placeholder = { Text("e.g. SCAN ME, CONNECT TO WI-FI") },
                                                        singleLine = true,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 20.dp),
                                                        shape = RoundedCornerShape(14.dp),
                                                        colors = contentBoxColors
                                                    )

                                                    // Quick Preset Chips
                                                    val presetChips = listOf(
                                                        "SCAN ME",
                                                        "CONNECT TO WI-FI",
                                                        "PAY HERE",
                                                        "FOLLOW US",
                                                        "VIEW MENU",
                                                        "SAVE CONTACT",
                                                        "VISIT WEBSITE"
                                                    )

                                                    LazyRow(
                                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 4.dp)
                                                    ) {
                                                        items(presetChips) { chipText ->
                                                            val isChipActive = frameCtaText.equals(chipText, ignoreCase = true)
                                                            Surface(
                                                                onClick = { frameCtaText = chipText },
                                                                shape = CircleShape,
                                                                color = if (isChipActive) MaterialTheme.colorScheme.primary else (if (isDark) ComposeColor(0xFF2C2C2C) else ComposeColor(0xFFEEEEEE)),
                                                                border = if (isChipActive) null else BorderStroke(1.dp, if (isDark) ComposeColor.White.copy(alpha = 0.1f) else ComposeColor.Black.copy(alpha = 0.08f)),
                                                                modifier = Modifier.height(32.dp)
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier.padding(horizontal = 14.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = chipText,
                                                                        fontSize = 12.sp,
                                                                        fontWeight = if (isChipActive) FontWeight.Bold else FontWeight.Medium,
                                                                        color = if (isChipActive) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.8f)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                HorizontalDivider(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 20.dp),
                                                    thickness = 1.dp,
                                                    color = dividerColor
                                                )

                                                // 3. Frame Color Palette
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Text(
                                                        text = "Frame Color",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textColor,
                                                        modifier = Modifier.padding(horizontal = 20.dp)
                                                    )

                                                    val frameColors = listOf(
                                                        pixelColor to "QR Color",
                                                        ComposeColor(0xFF111827) to "Black",
                                                        ComposeColor(0xFF2563EB) to "Blue",
                                                        ComposeColor(0xFF059669) to "Emerald",
                                                        ComposeColor(0xFFDC2626) to "Crimson",
                                                        ComposeColor(0xFF7C3AED) to "Violet",
                                                        ComposeColor(0xFFD97706) to "Amber",
                                                        ComposeColor(0xFF0D9488) to "Teal"
                                                    )

                                                    LazyRow(
                                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        items(frameColors) { (col, _) ->
                                                            val isColSelected = frameColor == col
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(42.dp)
                                                                    .clip(CircleShape)
                                                                    .background(col)
                                                                    .border(
                                                                        width = if (isColSelected) 3.dp else 1.dp,
                                                                        color = if (isColSelected) MaterialTheme.colorScheme.primary else ComposeColor.White.copy(alpha = 0.3f),
                                                                        shape = CircleShape
                                                                    )
                                                                    .clickable { frameColor = col },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                if (isColSelected) {
                                                                    Icon(
                                                                        imageVector = Icons.Rounded.Check,
                                                                        contentDescription = "Selected",
                                                                        tint = if (androidx.core.graphics.ColorUtils.calculateLuminance(col.toArgb()) > 0.5) ComposeColor.Black else ComposeColor.White,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                    QrCustomizationTab.TEST -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            when (testStatus) {
                                                QrTestStatus.NOT_TESTED -> {
                                                    Surface(
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = if (isDark) ComposeColor(0xFF222222) else ComposeColor(0xFFF7F7F8),
                                                        border = BorderStroke(1.dp, if (isDark) ComposeColor.White.copy(alpha = 0.12f) else ComposeColor.Black.copy(alpha = 0.08f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(20.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(56.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isDark) ComposeColor(0xFF2F2F2F) else ComposeColor(0xFFEBEBEB)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = QrCodeScannerIcon,
                                                                    contentDescription = "Scan Test",
                                                                    tint = textColor,
                                                                    modifier = Modifier.size(28.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.height(14.dp))
                                                            Text(
                                                                text = "Readability Test Required",
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = textColor
                                                            )
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Text(
                                                                text = "Scan this QR code to verify it can be read properly before saving to your vault.",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = textColor.copy(alpha = 0.7f),
                                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                                lineHeight = 20.sp
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    Button(
                                                        onClick = { runQrTest() },
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(52.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = QrCodeScannerIcon,
                                                                contentDescription = "Test QR",
                                                                modifier = Modifier.size(28.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = "Test QR Code",
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 16.sp
                                                            )
                                                        }
                                                    }
                                                }

                                                QrTestStatus.TESTING -> {
                                                    Surface(
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = if (isDark) ComposeColor(0xFF222222) else ComposeColor(0xFFF7F7F8),
                                                        border = BorderStroke(1.dp, if (isDark) ComposeColor.White.copy(alpha = 0.12f) else ComposeColor.Black.copy(alpha = 0.08f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(vertical = 32.dp, horizontal = 20.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            CircularProgressIndicator(
                                                                color = MaterialTheme.colorScheme.primary,
                                                                strokeWidth = 3.dp,
                                                                modifier = Modifier.size(36.dp)
                                                            )
                                                            Spacer(modifier = Modifier.height(16.dp))
                                                            Text(
                                                                text = "Scanning & Analyzing...",
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = textColor
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Verifying contrast and scanner readability",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = textColor.copy(alpha = 0.6f)
                                                            )
                                                        }
                                                    }
                                                }

                                                QrTestStatus.PASSED -> {
                                                    Surface(
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = if (isDark) ComposeColor(0xFF162E1C) else ComposeColor(0xFFE8F5E9),
                                                        border = BorderStroke(1.5.dp, ComposeColor(0xFF2E7D32).copy(alpha = 0.4f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(18.dp)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Surface(
                                                                        shape = CircleShape,
                                                                        color = ComposeColor(0xFF2E7D32),
                                                                        modifier = Modifier.size(36.dp)
                                                                    ) {
                                                                        Box(contentAlignment = Alignment.Center) {
                                                                            Icon(
                                                                                imageVector = Icons.Rounded.Check,
                                                                                contentDescription = "Passed",
                                                                                tint = ComposeColor.White,
                                                                                modifier = Modifier.size(22.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                    Spacer(modifier = Modifier.width(12.dp))
                                                                    Column {
                                                                        Text(
                                                                            text = "PASSED",
                                                                            style = MaterialTheme.typography.titleMedium,
                                                                            fontWeight = FontWeight.ExtraBold,
                                                                            color = if (isDark) ComposeColor(0xFF81C784) else ComposeColor(0xFF2E7D32),
                                                                            letterSpacing = 1.sp
                                                                        )
                                                                        Text(
                                                                            text = "Scannable & working perfectly",
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = if (isDark) ComposeColor.White.copy(alpha = 0.7f) else ComposeColor.Black.copy(alpha = 0.6f)
                                                                        )
                                                                    }
                                                                }

                                                                IconButton(
                                                                    onClick = { runQrTest() },
                                                                    modifier = Modifier.size(36.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Rounded.Refresh,
                                                                        contentDescription = "Retest",
                                                                        tint = if (isDark) ComposeColor.White.copy(alpha = 0.8f) else ComposeColor.Black.copy(alpha = 0.7f),
                                                                        modifier = Modifier.size(22.dp)
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(14.dp))
                                                            HorizontalDivider(
                                                                thickness = 1.dp,
                                                                color = if (isDark) ComposeColor.White.copy(alpha = 0.1f) else ComposeColor.Black.copy(alpha = 0.08f)
                                                            )
                                                            Spacer(modifier = Modifier.height(12.dp))

                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = "Detected Type:",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = textColor.copy(alpha = 0.7f)
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = if (isDark) ComposeColor(0xFF2E7D32).copy(alpha = 0.3f) else ComposeColor(0xFFC8E6C9)
                                                                ) {
                                                                    Text(
                                                                        text = scannedBarcodeType ?: type,
                                                                        style = MaterialTheme.typography.labelMedium,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (isDark) ComposeColor(0xFF81C784) else ComposeColor(0xFF1B5E20),
                                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                                    )
                                                                }
                                                            }

                                                            if (!scannedContent.isNullOrBlank()) {
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                Surface(
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    color = if (isDark) ComposeColor.Black.copy(alpha = 0.3f) else ComposeColor.White.copy(alpha = 0.7f),
                                                                    modifier = Modifier.fillMaxWidth()
                                                                ) {
                                                                    Text(
                                                                        text = scannedContent ?: "",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = textColor,
                                                                        maxLines = 3,
                                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                                        modifier = Modifier.padding(10.dp)
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(12.dp))
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.CheckCircle,
                                                                    contentDescription = null,
                                                                    tint = if (isDark) ComposeColor(0xFF81C784) else ComposeColor(0xFF2E7D32),
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "Verification complete. You can now save.",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = if (isDark) ComposeColor(0xFF81C784) else ComposeColor(0xFF2E7D32)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(14.dp))

                                                    Button(
                                                        onClick = { handleSave() },
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(52.dp)
                                                    ) {
                                                        Text(
                                                            text = "Save to Vault",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp
                                                        )
                                                    }
                                                }

                                                QrTestStatus.FAILED -> {
                                                    Surface(
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = if (isDark) ComposeColor(0xFF331A1A) else ComposeColor(0xFFFFEBEE),
                                                        border = BorderStroke(1.5.dp, ComposeColor(0xFFD32F2F).copy(alpha = 0.4f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(18.dp)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Surface(
                                                                        shape = CircleShape,
                                                                        color = ComposeColor(0xFFD32F2F),
                                                                        modifier = Modifier.size(36.dp)
                                                                    ) {
                                                                        Box(contentAlignment = Alignment.Center) {
                                                                            Icon(
                                                                                imageVector = Icons.Rounded.Close,
                                                                                contentDescription = "Failed",
                                                                                tint = ComposeColor.White,
                                                                                modifier = Modifier.size(22.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                    Spacer(modifier = Modifier.width(12.dp))
                                                                    Column {
                                                                        Text(
                                                                            text = "FAILED",
                                                                            style = MaterialTheme.typography.titleMedium,
                                                                            fontWeight = FontWeight.ExtraBold,
                                                                            color = if (isDark) ComposeColor(0xFFEF9A9A) else ComposeColor(0xFFD32F2F),
                                                                            letterSpacing = 1.sp
                                                                        )
                                                                        Text(
                                                                            text = "QR code cannot be scanned",
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = if (isDark) ComposeColor.White.copy(alpha = 0.7f) else ComposeColor.Black.copy(alpha = 0.6f)
                                                                        )
                                                                    }
                                                                }

                                                                IconButton(
                                                                    onClick = { runQrTest() },
                                                                    modifier = Modifier.size(36.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Rounded.Refresh,
                                                                        contentDescription = "Retest",
                                                                        tint = if (isDark) ComposeColor.White.copy(alpha = 0.8f) else ComposeColor.Black.copy(alpha = 0.7f),
                                                                        modifier = Modifier.size(22.dp)
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(14.dp))
                                                            HorizontalDivider(
                                                                thickness = 1.dp,
                                                                color = if (isDark) ComposeColor.White.copy(alpha = 0.1f) else ComposeColor.Black.copy(alpha = 0.08f)
                                                            )
                                                            Spacer(modifier = Modifier.height(12.dp))

                                                            Text(
                                                                text = testErrorMessage ?: "The scanner could not decode the code.",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = textColor,
                                                                lineHeight = 20.sp
                                                            )

                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = "Suggestions to fix:",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = textColor.copy(alpha = 0.8f)
                                                            )
                                                            Text(
                                                                text = "• Increase contrast between pixels and background\n• Reduce eye or pixel corner radius\n• Avoid using similar colors for surface and pixels",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = textColor.copy(alpha = 0.7f),
                                                                lineHeight = 18.sp
                                                            )

                                                            Spacer(modifier = Modifier.height(10.dp))
                                                            Text(
                                                                text = "⚠️ Saving is disabled until the QR code passes verification.",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = if (isDark) ComposeColor(0xFFEF9A9A) else ComposeColor(0xFFD32F2F)
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(14.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        OutlinedButton(
                                                            onClick = { selectedCustomizationTab = QrCustomizationTab.COLOR },
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.weight(1f).height(46.dp)
                                                        ) {
                                                            Text("Fix Colors", fontWeight = FontWeight.Bold)
                                                        }
                                                        OutlinedButton(
                                                            onClick = { selectedCustomizationTab = QrCustomizationTab.CORNERS },
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.weight(1f).height(46.dp)
                                                        ) {
                                                            Text("Fix Corners", fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    Button(
                                                        onClick = { runQrTest() },
                                                        shape = RoundedCornerShape(14.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        ),
                                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = QrCodeScannerIcon,
                                                                contentDescription = "Test Again",
                                                                modifier = Modifier.size(28.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Test Again", fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.navigationBarsPadding())
                            }
                        }
                    }
                }
            }
        }

    if (showCustomColorPickerFor != null) {
        val initialPickerColor = when (showCustomColorPickerFor) {
            "eye" -> eyeColor
            "pixel" -> pixelColor
            "background" -> bgColor
            else -> ComposeColor.Black
        }
        com.app.qrkit.ui.components.ProfessionalColorPickerDialog(
            initialColor = initialPickerColor,
            onColorSelected = { selected ->
                when (showCustomColorPickerFor) {
                    "eye" -> eyeColor = selected
                    "pixel" -> pixelColor = selected
                    "background" -> bgColor = selected
                }
                showCustomColorPickerFor = null
            },
            onDismiss = { showCustomColorPickerFor = null }
        )
    }
}

@Composable
fun ColorSettingRow(
    title: String,
    color: ComposeColor,
    textColor: ComposeColor,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = ComposeColor.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                val hexString = String.format("#%06X", 0xFFFFFF and color.toArgb())
                Text(
                    text = hexString,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 1.5.dp,
                        color = if (isDark) ComposeColor.White.copy(alpha = 0.25f) else ComposeColor.Black.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            )
        }
    }
}

private fun tryZxingDecode(bmp: android.graphics.Bitmap): String? {
    return try {
        val width = bmp.width
        val height = bmp.height
        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = com.google.zxing.RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
        val reader = com.google.zxing.qrcode.QRCodeReader()
        val result = reader.decode(binaryBitmap)
        result.text
    } catch (e: Exception) {
        try {
            val width = bmp.width
            val height = bmp.height
            val pixels = IntArray(width * height)
            bmp.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = com.google.zxing.RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.GlobalHistogramBinarizer(source))
            val reader = com.google.zxing.qrcode.QRCodeReader()
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (e2: Exception) {
            null
        }
    }
}


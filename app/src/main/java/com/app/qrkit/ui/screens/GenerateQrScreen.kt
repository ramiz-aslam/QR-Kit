package com.app.qrkit.ui.screens

import com.app.qrkit.ui.components.modernShadow

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import com.app.qrkit.ui.icons.QrCodeScannerIcon

enum class QrCustomizationTab(val title: String) {
    COLOR("Color"),
    CORNERS("Corners"),
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
    
    var selectedTemplateId by remember { mutableStateOf<String?>(qrToEdit?.templateId) }
    
    var eyeColor by remember { mutableStateOf(ComposeColor.Black) }
    var pixelColor by remember { mutableStateOf(ComposeColor.Black) }
    var eyeRadius by remember { mutableFloatStateOf(0f) }
    var pixelRadius by remember { mutableFloatStateOf(0f) }
    var bgColor by remember { mutableStateOf(if (qrToEdit != null) ComposeColor(qrToEdit.bgColor) else ComposeColor.White) }
    var bgRadius by remember { mutableFloatStateOf(qrToEdit?.bgRadius ?: 0f) }
    
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    var selectedCustomizationTab by remember { mutableStateOf(QrCustomizationTab.COLOR) }
    var showCustomColorPickerFor by remember { mutableStateOf<String?>(null) }
    
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
    LaunchedEffect(previewContent, eyeColor, pixelColor, eyeRadius, pixelRadius, bgColor, bgRadius, selectedTemplateId, context) {
        if (previewContent.isNotBlank()) {
            withContext(Dispatchers.Default) {
                try {
                    val hints = mapOf(com.google.zxing.EncodeHintType.MARGIN to 0)
                    val writer = com.google.zxing.qrcode.QRCodeWriter()
                    val bitMatrix = writer.encode(previewContent, com.google.zxing.BarcodeFormat.QR_CODE, 0, 0, hints)
                    
                    val matrixWidth = bitMatrix.width
                    val matrixHeight = bitMatrix.height
                    val marginModules = 2
                    val totalWidth = matrixWidth + marginModules * 2
                    val moduleSize = 512f / totalWidth
                    
                    val qrBmp = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                    val qrCanvas = android.graphics.Canvas(qrBmp)
                    
                    val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                    bgPaint.color = bgColor.toArgb()
                    val maxCornerPx = 512f / 2f
                    val cornerRadiusPx = bgRadius * maxCornerPx
                    if (cornerRadiusPx > 0f) {
                        qrCanvas.drawRoundRect(android.graphics.RectF(0f, 0f, 512f, 512f), cornerRadiusPx, cornerRadiusPx, bgPaint)
                    } else {
                        qrCanvas.drawRect(0f, 0f, 512f, 512f, bgPaint)
                    }

                    val eyePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                    eyePaint.color = eyeColor.toArgb()

                    val pixelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                    pixelPaint.color = pixelColor.toArgb()

                    for (x in 0 until matrixWidth) {
                        for (y in 0 until matrixHeight) {
                            val isEye = (x < 7 && y < 7) || (x > matrixWidth - 8 && y < 7) || (x < 7 && y > matrixHeight - 8)
                            if (bitMatrix[x, y] && !isEye) {
                                val left = (x + marginModules) * moduleSize
                                val top = (y + marginModules) * moduleSize
                                val right = left + moduleSize
                                val bottom = top + moduleSize
                                
                                val radius = pixelRadius * (moduleSize / 2f)
                                
                                qrCanvas.drawRoundRect(
                                    android.graphics.RectF(left, top, right, bottom),
                                    radius, radius, pixelPaint
                                )
                            }
                        }
                    }
                    
                    val eyeBgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                    eyeBgPaint.color = bgColor.toArgb()
                    
                    val drawEye = { ex: Int, ey: Int ->
                        val outerRadius = eyeRadius * 3.5f * moduleSize
                        val hollowRadius = java.lang.Math.max(0f, outerRadius - moduleSize)
                        val innerRadius = eyeRadius * 1.5f * moduleSize
                        
                        val outerRect = android.graphics.RectF(
                            (ex + marginModules) * moduleSize,
                            (ey + marginModules) * moduleSize,
                            (ex + 7 + marginModules) * moduleSize,
                            (ey + 7 + marginModules) * moduleSize
                        )
                        qrCanvas.drawRoundRect(outerRect, outerRadius, outerRadius, eyePaint)
                        
                        val hollowRect = android.graphics.RectF(
                            (ex + 1 + marginModules) * moduleSize,
                            (ey + 1 + marginModules) * moduleSize,
                            (ex + 6 + marginModules) * moduleSize,
                            (ey + 6 + marginModules) * moduleSize
                        )
                        qrCanvas.drawRoundRect(hollowRect, hollowRadius, hollowRadius, eyeBgPaint)
                        
                        val innerRect = android.graphics.RectF(
                            (ex + 2 + marginModules) * moduleSize,
                            (ey + 2 + marginModules) * moduleSize,
                            (ex + 5 + marginModules) * moduleSize,
                            (ey + 5 + marginModules) * moduleSize
                        )
                        qrCanvas.drawRoundRect(innerRect, innerRadius, innerRadius, eyePaint)
                    }
                    
                    drawEye(0, 0)
                    drawEye(matrixWidth - 7, 0)
                    drawEye(0, matrixHeight - 7)
                    
                    bitmap = qrBmp
                    
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

    LaunchedEffect(previewContent, eyeColor, pixelColor, eyeRadius, pixelRadius, bgColor, bgRadius, selectedTemplateId) {
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
                    templateId = null,
                    createdAt = qrToEdit?.createdAt ?: System.currentTimeMillis(),
                    bgColor = bgColor.toArgb().toLong(),
                    bgRadius = bgRadius
                )
                viewModel.insertQrCode(newQr)
                
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
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable(
                                    enabled = !canSave,
                                    onClick = {
                                        selectedCustomizationTab = QrCustomizationTab.TEST
                                        android.widget.Toast.makeText(
                                            context,
                                            if (testStatus == QrTestStatus.FAILED) "QR code failed readability test. Please fix errors and re-test." else "Please test the QR code before saving.",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                        ) {
                            Button(
                                onClick = { handleSave() },
                                enabled = canSave,
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = if (isDark) ComposeColor(0xFF2A2A2A) else ComposeColor(0xFFE0E0E0),
                                    disabledContentColor = if (isDark) ComposeColor.White.copy(alpha = 0.35f) else ComposeColor.Black.copy(alpha = 0.35f)
                                )
                            ) {
                                Text(
                                    text = "Save",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = containerBgColor,
                            unfocusedContainerColor = containerBgColor,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = ComposeColor.Transparent
                        )
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
                                    placeholder = { Text("e.g. 37.7749") },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = containerBgColor,
                                        unfocusedContainerColor = containerBgColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ComposeColor.Transparent
                                    )
                                )
                                OutlinedTextField(
                                    value = locLongitude,
                                    onValueChange = { locLongitude = it },
                                    label = { Text("Longitude") },
                                    placeholder = { Text("e.g. -122.4194") },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = containerBgColor,
                                        unfocusedContainerColor = containerBgColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ComposeColor.Transparent
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = locPlaceName,
                                onValueChange = { locPlaceName = it },
                                label = { Text("Location Name / Address (Optional)") },
                                placeholder = { Text("e.g. Central Park, New York") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerBgColor,
                                    unfocusedContainerColor = containerBgColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = ComposeColor.Transparent
                                )
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
                                    placeholder = { Text("e.g. John") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = containerBgColor,
                                        unfocusedContainerColor = containerBgColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ComposeColor.Transparent
                                    )
                                )
                                OutlinedTextField(
                                    value = contactLastName,
                                    onValueChange = { contactLastName = it },
                                    label = { Text("Last Name") },
                                    placeholder = { Text("e.g. Doe") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = containerBgColor,
                                        unfocusedContainerColor = containerBgColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ComposeColor.Transparent
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("Phone Number") },
                                placeholder = { Text("e.g. +1 (555) 019-2834") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerBgColor,
                                    unfocusedContainerColor = containerBgColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = ComposeColor.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactEmail,
                                onValueChange = { contactEmail = it },
                                label = { Text("Email Address") },
                                placeholder = { Text("e.g. john.doe@example.com") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerBgColor,
                                    unfocusedContainerColor = containerBgColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = ComposeColor.Transparent
                                )
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
                                    placeholder = { Text("e.g. Acme Corp") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = containerBgColor,
                                        unfocusedContainerColor = containerBgColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ComposeColor.Transparent
                                    )
                                )
                                OutlinedTextField(
                                    value = contactJobTitle,
                                    onValueChange = { contactJobTitle = it },
                                    label = { Text("Job Title") },
                                    placeholder = { Text("e.g. Designer") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = containerBgColor,
                                        unfocusedContainerColor = containerBgColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ComposeColor.Transparent
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactAddress,
                                onValueChange = { contactAddress = it },
                                label = { Text("Address") },
                                placeholder = { Text("e.g. 123 Market St, San Francisco") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerBgColor,
                                    unfocusedContainerColor = containerBgColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = ComposeColor.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = contactWebsite,
                                onValueChange = { contactWebsite = it },
                                label = { Text("Website") },
                                placeholder = { Text("https://example.com") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerBgColor,
                                    unfocusedContainerColor = containerBgColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = ComposeColor.Transparent
                                )
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerBgColor,
                                    unfocusedContainerColor = containerBgColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = ComposeColor.Transparent
                                )
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerBgColor,
                                    unfocusedContainerColor = containerBgColor,
                                    focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = ComposeColor.Transparent
                                )
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
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = containerBgColor,
                                        unfocusedContainerColor = containerBgColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = ComposeColor.Transparent
                                    )
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
                            val isPoster = selectedTemplateId?.endsWith("_poster") == true
                            val previewCornerPercent = (bgRadius * 50).coerceIn(0f, 50f).toInt()
                            val previewShape = RoundedCornerShape(percent = previewCornerPercent)
                            Card(
                                modifier = if (isPoster) Modifier.fillMaxWidth(0.85f).wrapContentHeight() else Modifier.size(220.dp),
                                shape = if (isPoster) RoundedCornerShape(0.dp) else previewShape,
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isPoster) 0.dp else if (previewCornerPercent > 0) 4.dp else 2.dp),
                                colors = CardDefaults.cardColors(containerColor = ComposeColor.Transparent)
                            ) {
                                Image(
                                    bitmap = b.asImageBitmap(),
                                    contentDescription = "Live Generated QR",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
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
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                QrCustomizationTab.values().forEach { tab ->
                                    val isSelected = selectedCustomizationTab == tab
                                    val tabText = when (tab) {
                                        QrCustomizationTab.TEST -> {
                                            when (testStatus) {
                                                QrTestStatus.PASSED -> "Test ✓"
                                                QrTestStatus.FAILED -> "Test ⚠️"
                                                else -> "Test"
                                            }
                                        }
                                        else -> tab.title
                                    }
                                    Surface(
                                        onClick = { selectedCustomizationTab = tab },
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDark) ComposeColor(0xFF353535) else ComposeColor.White),
                                        border = if (isSelected) null else BorderStroke(1.dp, if (isDark) ComposeColor.White.copy(alpha = 0.15f) else ComposeColor.Black.copy(alpha = 0.08f)),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tabText,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else (
                                                    if (tab == QrCustomizationTab.TEST && testStatus == QrTestStatus.PASSED) ComposeColor(0xFF4CAF50)
                                                    else if (tab == QrCustomizationTab.TEST && testStatus == QrTestStatus.FAILED) ComposeColor(0xFFE53935)
                                                    else textColor.copy(alpha = 0.7f)
                                                )
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

                            // Scrollable options section with solid height
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .navigationBarsPadding()
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
                                                onValueChange = { eyeRadius = it },
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
                                                onValueChange = { pixelRadius = it },
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
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Surface Radius",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                                Text(
                                                    text = "${(bgRadius * 100).toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textColor.copy(alpha = 0.6f)
                                                )
                                            }
                                            Slider(
                                                value = bgRadius,
                                                onValueChange = { bgRadius = it },
                                                valueRange = 0f..1f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                                    inactiveTrackColor = if (isDark) ComposeColor.White.copy(alpha = 0.15f) else ComposeColor.Black.copy(alpha = 0.1f)
                                                )
                                            )
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
private fun ColorSettingRow(
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


package com.app.qrkit.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.app.qrkit.ui.icons.*
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executors

private enum class ScannedType {
    WIFI,
    LOCATION,
    CONTACT,
    PHONE,
    EMAIL,
    URL,
    TEXT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrScreen(viewModel: com.app.qrkit.ui.QrViewModel, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scanned by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<Barcode?>(null) }
    
    var camera by remember { mutableStateOf<Camera?>(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    com.app.qrkit.ui.theme.UpdateSystemBars(
        darkStatusIcons = false,
        darkNavigationIcons = false
    )
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val containerColor = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }
    
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && !scanned) {
            try {
                val scanner = BarcodeScanning.getClient()
                val image = InputImage.fromFilePath(context, uri)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            val rawValue = barcode.rawValue
                            if (rawValue != null) {
                                scanned = true
                                scannedBarcode = barcode
                                
                                break
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }

    if (scannedBarcode != null) {
        val barcode = scannedBarcode!!
        val rawValue = barcode.rawValue ?: ""

        val scannedType = remember(barcode, rawValue) {
            when {
                barcode.valueType == Barcode.TYPE_WIFI || rawValue.startsWith("WIFI:", ignoreCase = true) -> ScannedType.WIFI
                barcode.valueType == Barcode.TYPE_CONTACT_INFO || rawValue.contains("BEGIN:VCARD", ignoreCase = true) || rawValue.contains("MECARD:", ignoreCase = true) -> ScannedType.CONTACT
                barcode.valueType == Barcode.TYPE_GEO || rawValue.startsWith("geo:", ignoreCase = true) || rawValue.contains("maps.google.com", ignoreCase = true) || rawValue.contains("maps.app.goo.gl", ignoreCase = true) || rawValue.contains("goo.gl/maps", ignoreCase = true) -> ScannedType.LOCATION
                barcode.valueType == Barcode.TYPE_PHONE || rawValue.startsWith("tel:", ignoreCase = true) -> ScannedType.PHONE
                barcode.valueType == Barcode.TYPE_EMAIL || rawValue.startsWith("mailto:", ignoreCase = true) || rawValue.startsWith("MATMSG:", ignoreCase = true) -> ScannedType.EMAIL
                barcode.valueType == Barcode.TYPE_URL || rawValue.startsWith("http://", ignoreCase = true) || rawValue.startsWith("https://", ignoreCase = true) -> ScannedType.URL
                else -> ScannedType.TEXT
            }
        }

        // Location Parsing
        val locLat: Double?
        val locLng: Double?
        val locQuery: String?

        if (barcode.valueType == Barcode.TYPE_GEO && barcode.geoPoint != null) {
            locLat = barcode.geoPoint?.lat
            locLng = barcode.geoPoint?.lng
            locQuery = if (rawValue.contains("?q=") || rawValue.contains("&q=")) {
                Regex("[?&]q=([^&]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.let { Uri.decode(it) }
            } else null
        } else {
            val geoMatch = Regex("geo:([-\\d.]+),([-\\d.]+)", RegexOption.IGNORE_CASE).find(rawValue)
            val mapMatch = Regex("@([-\\d.]+),([-\\d.]+)").find(rawValue)
            locLat = geoMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: mapMatch?.groupValues?.get(1)?.toDoubleOrNull()
            locLng = geoMatch?.groupValues?.get(2)?.toDoubleOrNull() ?: mapMatch?.groupValues?.get(2)?.toDoubleOrNull()
            locQuery = Regex("[?&]q(?:uery)?=([^&]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.let { Uri.decode(it) }
        }

        val hasCoords = locLat != null && locLng != null && !(locLat == 0.0 && locLng == 0.0)
        val geoUri = when {
            hasCoords && !locQuery.isNullOrBlank() -> "geo:$locLat,$locLng?q=${Uri.encode(locQuery)}"
            hasCoords -> "geo:$locLat,$locLng"
            !locQuery.isNullOrBlank() -> "geo:0,0?q=${Uri.encode(locQuery)}"
            rawValue.startsWith("geo:", ignoreCase = true) -> rawValue
            else -> "geo:0,0?q=${Uri.encode(rawValue)}"
        }

        // Contact Parsing
        val cInfo = if (barcode.valueType == Barcode.TYPE_CONTACT_INFO) barcode.contactInfo else null

        val contactName: String? = cInfo?.name?.formattedName?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(cInfo?.name?.first, cInfo?.name?.last).joinToString(" ").takeIf { it.isNotBlank() }
            ?: Regex("FN:([^\\n\\r]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()
            ?: Regex("N:([^;\\n\\r]*);([^;\\n\\r]*)", RegexOption.IGNORE_CASE).find(rawValue)?.let {
                listOf(it.groupValues[2], it.groupValues[1]).filter { s -> s.isNotBlank() }.joinToString(" ")
            }?.takeIf { it.isNotBlank() }
            ?: Regex("MECARD:.*?(?:^|;)N:([^;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.replace(",", " ")?.trim()

        val contactPhone: String? = cInfo?.phones?.firstOrNull()?.number?.takeIf { it.isNotBlank() }
            ?: Regex("TEL[^:]*:([^\\n\\r;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()
            ?: Regex("MECARD:.*?(?:^|;)TEL:([^;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()

        val contactEmail: String? = cInfo?.emails?.firstOrNull()?.address?.takeIf { it.isNotBlank() }
            ?: Regex("EMAIL[^:]*:([^\\n\\r;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()
            ?: Regex("MECARD:.*?(?:^|;)EMAIL:([^;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()

        val contactOrg: String? = cInfo?.organization?.takeIf { it.isNotBlank() }
            ?: Regex("ORG:([^\\n\\r;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()
            ?: Regex("MECARD:.*?(?:^|;)ORG:([^;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()

        val contactTitle: String? = cInfo?.title?.takeIf { it.isNotBlank() }
            ?: Regex("TITLE:([^\\n\\r;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()

        val contactAddress: String? = cInfo?.addresses?.firstOrNull()?.addressLines?.joinToString(", ")?.takeIf { it.isNotBlank() }
            ?: Regex("ADR[^:]*:(?:;;)?([^;\\n\\r]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.replace(";", " ")?.trim()
            ?: Regex("MECARD:.*?(?:^|;)ADR:([^;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()

        val contactWebsite: String? = cInfo?.urls?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: Regex("URL:([^\\n\\r;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()
            ?: Regex("MECARD:.*?(?:^|;)URL:([^;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()

        val contactNote: String? = Regex("NOTE:([^\\n\\r;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()
            ?: Regex("MECARD:.*?(?:^|;)NOTE:([^;]+)", RegexOption.IGNORE_CASE).find(rawValue)?.groupValues?.get(1)?.trim()

        ModalBottomSheet(
            onDismissRequest = { 
                scannedBarcode = null
                scanned = false 
            },
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Recognition result", style = MaterialTheme.typography.titleMedium, color = textColor)
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF353535) else Color(0xFFF7F7F7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                when (scannedType) {
                                    ScannedType.WIFI -> {
                                        Text("Wi-Fi detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val ssid = barcode.wifi?.ssid ?: Regex("S:([^;]+)").find(rawValue)?.groupValues?.get(1) ?: ""
                                        Text("Network name: $ssid", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                        val encType = when (barcode.wifi?.encryptionType) {
                                            Barcode.WiFi.TYPE_WEP -> "WEP"
                                            Barcode.WiFi.TYPE_WPA -> "WPA/WPA2"
                                            Barcode.WiFi.TYPE_OPEN -> "Open"
                                            else -> Regex("T:([^;]+)").find(rawValue)?.groupValues?.get(1) ?: "Unknown"
                                        }
                                        Text("Encryption type: $encType", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                        val pwd = barcode.wifi?.password ?: Regex("P:([^;]+)").find(rawValue)?.groupValues?.get(1)
                                        if (!pwd.isNullOrEmpty()) {
                                            Text("Password: $pwd", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                        }
                                    }
                                    ScannedType.LOCATION -> {
                                        Text("Location detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (!locQuery.isNullOrBlank()) {
                                            Text(locQuery, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color.Black)
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        if (hasCoords) {
                                            Text(
                                                "Coordinates: ${String.format(Locale.US, "%.5f", locLat)}, ${String.format(Locale.US, "%.5f", locLng)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray
                                            )
                                        } else if (locQuery.isNullOrBlank()) {
                                            Text(rawValue, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                        }
                                    }
                                    ScannedType.CONTACT -> {
                                        Text("Contact detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val displayName = contactName ?: contactOrg ?: contactPhone ?: contactEmail ?: "Contact"
                                        Text(displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        
                                        val companyRole = listOfNotNull(contactTitle, contactOrg.takeIf { it != displayName }).joinToString(" · ")
                                        if (companyRole.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(companyRole, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFBBBBBB) else Color(0xFF555555))
                                        }
                                        if (!contactPhone.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Phone: $contactPhone", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                        }
                                        if (!contactEmail.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text("Email: $contactEmail", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                        }
                                        if (!contactAddress.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                "Address: $contactAddress",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    ScannedType.PHONE -> {
                                        Text("Phone number detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val phoneNum = barcode.phone?.number ?: rawValue.removePrefix("tel:").trim()
                                        Text(phoneNum, style = MaterialTheme.typography.bodyLarge, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                    }
                                    ScannedType.EMAIL -> {
                                        Text("Email detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val emailAddr = barcode.email?.address ?: rawValue.substringBefore("?").removePrefix("mailto:").trim()
                                        Text(emailAddr, style = MaterialTheme.typography.bodyLarge, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                    }
                                    ScannedType.URL -> {
                                        Text("Link detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val urlStr = barcode.url?.url ?: rawValue
                                        Text(urlStr, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                    }
                                    else -> {
                                        Text("Text detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(rawValue, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color(0xFFCCCCCC) else Color.DarkGray)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF454545) else Color.Black.copy(alpha = 0.05f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val iconTint = if (isDark) Color.White else Color.Gray
                                    when (scannedType) {
                                        ScannedType.WIFI -> Icon(WifiIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                                        ScannedType.PHONE -> Icon(CallIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                                        ScannedType.EMAIL -> Icon(MailIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                                        ScannedType.URL -> Icon(LinkIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                                        ScannedType.LOCATION -> Icon(LocationOnIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                                        ScannedType.CONTACT -> Icon(PersonIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                                        else -> Icon(TextAdIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val doCopy = {
                            clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", rawValue))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        val secondaryBtnColor = if (isDark) Color(0xFF454545) else Color(0xFFF0F0F0)
                        val secondaryTextColor = if (isDark) Color.White else Color.Black

                        when (scannedType) {
                            ScannedType.LOCATION -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val copyText = when {
                                                !locQuery.isNullOrBlank() && hasCoords -> "$locQuery ($locLat, $locLng)"
                                                hasCoords -> "$locLat, $locLng"
                                                !locQuery.isNullOrBlank() -> locQuery
                                                else -> rawValue
                                            }
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Location", copyText))
                                            Toast.makeText(context, "Location copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = secondaryBtnColor, contentColor = secondaryTextColor)
                                    ) {
                                        Text("Copy", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {
                                                val queryParam = locQuery ?: if (hasCoords) "$locLat,$locLng" else rawValue
                                                val webUrl = "https://www.google.com/maps/search/?api=1&query=${Uri.encode(queryParam)}"
                                                try {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
                                                } catch (e2: Exception) {
                                                    Toast.makeText(context, "Cannot open map application", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1.2f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Open in Maps", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            ScannedType.CONTACT -> {
                                val addContact = {
                                    try {
                                        val intent = Intent(Intent.ACTION_INSERT).apply {
                                            type = ContactsContract.RawContacts.CONTENT_TYPE
                                            if (!contactName.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.NAME, contactName)
                                            if (!contactPhone.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.PHONE, contactPhone)
                                            if (!contactEmail.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.EMAIL, contactEmail)
                                            if (!contactOrg.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.COMPANY, contactOrg)
                                            if (!contactTitle.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contactTitle)
                                            if (!contactAddress.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.POSTAL, contactAddress)
                                            if (!contactNote.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.NOTES, contactNote)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val fallbackSummary = buildString {
                                            if (!contactName.isNullOrBlank()) appendLine("Name: $contactName")
                                            if (!contactPhone.isNullOrBlank()) appendLine("Phone: $contactPhone")
                                            if (!contactEmail.isNullOrBlank()) appendLine("Email: $contactEmail")
                                            if (!contactOrg.isNullOrBlank()) appendLine("Company: $contactOrg")
                                        }.trim().ifBlank { rawValue }
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Contact", fallbackSummary))
                                        Toast.makeText(context, "Contact copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                val copyContact = {
                                    val contactSummary = buildString {
                                        if (!contactName.isNullOrBlank()) appendLine("Name: $contactName")
                                        if (!contactPhone.isNullOrBlank()) appendLine("Phone: $contactPhone")
                                        if (!contactEmail.isNullOrBlank()) appendLine("Email: $contactEmail")
                                        if (!contactOrg.isNullOrBlank()) appendLine("Company: $contactOrg")
                                        if (!contactTitle.isNullOrBlank()) appendLine("Role: $contactTitle")
                                        if (!contactAddress.isNullOrBlank()) appendLine("Address: $contactAddress")
                                        if (!contactWebsite.isNullOrBlank()) appendLine("Website: $contactWebsite")
                                        if (!contactNote.isNullOrBlank()) appendLine("Note: $contactNote")
                                    }.trim().ifBlank { rawValue }
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Contact", contactSummary))
                                    Toast.makeText(context, "Contact copied to clipboard", Toast.LENGTH_SHORT).show()
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!contactPhone.isNullOrBlank()) {
                                        FilledIconButton(
                                            onClick = copyContact,
                                            modifier = Modifier.size(48.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = secondaryBtnColor,
                                                contentColor = secondaryTextColor
                                            ),
                                            shape = CircleShape
                                        ) {
                                            Icon(IconCopy, contentDescription = "Copy contact", modifier = Modifier.size(20.dp))
                                        }

                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactPhone"))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Cannot dial phone number", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = secondaryBtnColor, contentColor = secondaryTextColor)
                                        ) {
                                            Text("Call", fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = addContact,
                                            modifier = Modifier.weight(1.3f).height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Add Contact", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = copyContact,
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = secondaryBtnColor, contentColor = secondaryTextColor)
                                        ) {
                                            Text("Copy", fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = addContact,
                                            modifier = Modifier.weight(1.3f).height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Add Contact", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            ScannedType.PHONE, ScannedType.EMAIL, ScannedType.URL -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = doCopy,
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = secondaryBtnColor, contentColor = secondaryTextColor)
                                    ) {
                                        Text("Copy", fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            when (scannedType) {
                                                ScannedType.PHONE -> {
                                                    val phoneNum = barcode.phone?.number ?: rawValue.removePrefix("tel:").trim()
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                                                    context.startActivity(intent)
                                                }
                                                ScannedType.EMAIL -> {
                                                    val emailAddr = barcode.email?.address ?: rawValue.substringBefore("?").removePrefix("mailto:").trim()
                                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$emailAddr"))
                                                    context.startActivity(intent)
                                                }
                                                ScannedType.URL -> {
                                                    val url = barcode.url?.url ?: rawValue
                                                    val safeUrl = if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) url else "https://$url"
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)))
                                                }
                                                else -> {}
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        val buttonText = when (scannedType) {
                                            ScannedType.PHONE -> "Call"
                                            ScannedType.EMAIL -> "Send Email"
                                            else -> "Open Link"
                                        }
                                        Text(buttonText, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            ScannedType.WIFI -> {
                                Button(
                                    onClick = {
                                        val pwd = barcode.wifi?.password ?: Regex("P:([^;]+)").find(rawValue)?.groupValues?.get(1)
                                        if (!pwd.isNullOrEmpty()) {
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Password", pwd))
                                            Toast.makeText(context, "Password copied!", Toast.LENGTH_SHORT).show()
                                        }
                                        context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Connect to this network", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {
                                Button(
                                    onClick = doCopy,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Copy", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    com.app.qrkit.ui.components.ScaleOnPressFilledIconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp).shadow(
                            elevation = 1.5.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.03f)
                        ),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor, contentColor = textColor)
                    ) {
                        Icon(ArrowBackIcon, "Back", modifier = Modifier.size(28.dp))
                    }
                },
                actions = {
                    com.app.qrkit.ui.components.ScaleOnPressFilledIconButton(
                        onClick = { 
                            flashEnabled = !flashEnabled
                            camera?.cameraControl?.enableTorch(flashEnabled)
                        },
                        modifier = Modifier.padding(end = 8.dp).shadow(
                            elevation = 1.5.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.03f)
                        ),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = containerColor,
                            contentColor = if (flashEnabled) MaterialTheme.colorScheme.primary else textColor
                        )
                    ) {
                        Icon(
                            if (flashEnabled) com.app.qrkit.ui.icons.FlashlightOnIcon else com.app.qrkit.ui.icons.FlashlightOffIcon, 
                            contentDescription = "Torch",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            val scanner = BarcodeScanning.getClient()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (scanned) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                val raw = barcode.rawValue
                                                if (raw != null && !scanned) {
                                                    scanned = true
                                                    scannedBarcode = barcode
                                                    break
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                                camera?.cameraControl?.enableTorch(flashEnabled)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    }
                )

                                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val cutoutSize = 280.dp.toPx()
                        
                        val left = (canvasWidth - cutoutSize) / 2f
                        val top = (canvasHeight - cutoutSize) / 2f - 40.dp.toPx() // Slight offset upwards
                        val cornerRadius = 32.dp.toPx()

                        // Darkened background with a clear rounded cutout
                        val path = androidx.compose.ui.graphics.Path().apply {
                            addRect(androidx.compose.ui.geometry.Rect(0f, 0f, canvasWidth, canvasHeight))
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    rect = androidx.compose.ui.geometry.Rect(left, top, left + cutoutSize, top + cutoutSize),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                                )
                            )
                            fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                        }
                        
                        drawPath(path, color = Color.Black.copy(alpha = 0.6f))
                        
                        // Draw Bold Scanner Corners
                        val markerLength = 48.dp.toPx()
                        val strokeWidth = 6.dp.toPx()
                        val strokeColor = Color.White // Modern clean white
                        
                        val style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                        
                        // Top Left
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(left, top + markerLength)
                            lineTo(left, top + cornerRadius)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(left, top, left + cornerRadius * 2, top + cornerRadius * 2),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(left + markerLength, top)
                        }, strokeColor, style = style)
                        
                        // Top Right
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(left + cutoutSize - markerLength, top)
                            lineTo(left + cutoutSize - cornerRadius, top)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(left + cutoutSize - cornerRadius * 2, top, left + cutoutSize, top + cornerRadius * 2),
                                startAngleDegrees = 270f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(left + cutoutSize, top + markerLength)
                        }, strokeColor, style = style)
                        
                        // Bottom Right
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(left + cutoutSize, top + cutoutSize - markerLength)
                            lineTo(left + cutoutSize, top + cutoutSize - cornerRadius)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(left + cutoutSize - cornerRadius * 2, top + cutoutSize - cornerRadius * 2, left + cutoutSize, top + cutoutSize),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(left + cutoutSize - markerLength, top + cutoutSize)
                        }, strokeColor, style = style)
                        
                        // Bottom Left
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(left + markerLength, top + cutoutSize)
                            lineTo(left + cornerRadius, top + cutoutSize)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(left, top + cutoutSize - cornerRadius * 2, left + cornerRadius * 2, top + cutoutSize),
                                startAngleDegrees = 90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(left, top + cutoutSize - markerLength)
                        }, strokeColor, style = style)
                    }

                    // Album Option below the cutout
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                        val screenHeight = configuration.screenHeightDp.dp
                        // Cutout is offset -40.dp from center. Cutout height is 280.dp.
                        // So bottom of cutout is roughly (screenHeight / 2) - 40.dp + 140.dp = center + 100.dp
                        Spacer(modifier = Modifier.height((screenHeight / 2) + 100.dp + 48.dp))
                        
                        com.app.qrkit.ui.components.ScaleOnPressFilledIconButton(
                            onClick = { pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    ambientColor = Color.Black.copy(alpha = 0.1f),
                                    spotColor = Color.Black.copy(alpha = 0.1f)
                                ),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = containerColor,
                                contentColor = textColor
                            )
                        ) {
                            Icon(com.app.qrkit.ui.icons.ImageIcon, contentDescription = "Gallery", modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission is required to scan QR codes.", color = textColor)
            }
        }
    }
}








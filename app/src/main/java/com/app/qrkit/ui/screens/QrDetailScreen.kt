package com.app.qrkit.ui.screens


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.qrkit.ui.QrViewModel
import com.app.qrkit.ui.icons.*
import java.io.File
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.app.qrkit.data.QrCode
import com.app.qrkit.data.isFramed

private val QrCardShape = RoundedCornerShape(12.dp)

val IconCopy: ImageVector
    get() = ImageVector.Builder("Copy", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16f, 1f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(14f); horizontalLineToRelative(2f); verticalLineTo(3f); horizontalLineToRelative(12f); verticalLineTo(1f); close()
            moveTo(15f, 5f); horizontalLineToRelative(-7f); curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f); lineTo(6f, 21f); curveToRelative(0f, 1.1f, 0.89f, 2f, 1.99f, 2f); horizontalLineTo(19f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(11f); lineToRelative(-6f, -6f); close()
            moveTo(14f, 12f); horizontalLineToRelative(4.5f); lineTo(14f, 16.5f); verticalLineTo(12f); close()
        }
    }.build()

val IconOpen: ImageVector
    get() = ImageVector.Builder("Open", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 19f); horizontalLineTo(5f); verticalLineTo(5f); horizontalLineToRelative(7f); verticalLineTo(3f); horizontalLineTo(5f); curveToRelative(-1.11f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(14f); curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f); horizontalLineToRelative(14f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineToRelative(-7f); horizontalLineToRelative(-2f); verticalLineToRelative(7f); close()
            moveTo(14f, 3f); verticalLineToRelative(2f); horizontalLineToRelative(3.59f); lineToRelative(-9.83f, 9.83f); lineToRelative(1.41f, 1.41f); lineTo(19f, 6.41f); verticalLineTo(10f); horizontalLineToRelative(2f); verticalLineTo(3f); horizontalLineToRelative(-7f); close()
        }
    }.build()

val IconCall: ImageVector
    get() = ImageVector.Builder("Call", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20.01f, 15.38f); curveToRelative(-1.23f, 0f, -2.42f, -0.2f, -3.53f, -0.56f); curveToRelative(-0.35f, -0.12f, -0.74f, -0.03f, -1.01f, 0.24f); lineToRelative(-1.57f, 1.97f); curveToRelative(-2.83f, -1.35f, -5.48f, -3.9f, -6.89f, -6.83f); lineToRelative(1.95f, -1.66f); curveToRelative(0.27f, -0.28f, 0.35f, -0.67f, 0.24f, -1.02f); curveToRelative(-0.37f, -1.11f, -0.56f, -2.3f, -0.56f, -3.53f); curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f); horizontalLineTo(4f); curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f); curveToRelative(0f, 9.39f, 7.61f, 17f, 17f, 17f); curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f); verticalLineToRelative(-3.49f); curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f); close()
        }
    }.build()

val IconEmail: ImageVector
    get() = ImageVector.Builder("Email", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 4f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f); lineTo(2f, 18f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(16f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(6f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
            moveTo(20f, 8f); lineToRelative(-8f, 5f); lineToRelative(-8f, -5f); verticalLineTo(6f); lineToRelative(8f, 5f); lineToRelative(8f, -5f); verticalLineToRelative(2f); close()
        }
    }.build()

val IconWifi: ImageVector
    get() = ImageVector.Builder("Wifi", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(1f, 9f); lineToRelative(2f, 2f); curveToRelative(4.97f, -4.97f, 13.03f, -4.97f, 18f, 0f); lineToRelative(2f, -2f); curveTo(16.93f, 2.93f, 7.08f, 2.93f, 1f, 9f); close()
            moveTo(9f, 17f); lineToRelative(3f, 3f); lineToRelative(3f, -3f); curveToRelative(-1.65f, -1.66f, -4.34f, -1.66f, -6f, 0f); close()
            moveTo(5f, 13f); lineToRelative(2f, 2f); curveToRelative(2.76f, -2.76f, 7.24f, -2.76f, 10f, 0f); lineToRelative(2f, -2f); curveTo(15.14f, 9.14f, 8.87f, 9.14f, 5f, 13f); close()
        }
    }.build()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.QrDetailScreen(
    qrId: String?,
    viewModel: QrViewModel,
    onBackClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var qr by remember { mutableStateOf<QrCode?>(null) }
    var showInfoSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    
    val context = LocalContext.current
    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    
    val containerColor = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val accentColor = MaterialTheme.colorScheme.primary
    val onAccentColor = MaterialTheme.colorScheme.onPrimary
    val bodyBgColor = if (isDark) Color.Black else Color.White
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(qrId) {
        if (qrId != null) {
            qr = viewModel.getQrCodeById(qrId)
        }
    }

    val copyToClipboard = { label: String, content: String ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label", Toast.LENGTH_SHORT).show()
    }
    
    val shareImage = {
        qr?.let {
            val file = if (it.imagePath.startsWith("/")) File(it.imagePath) else File(context.filesDir, it.imagePath)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share QR Code"))
            }
        }
    }

    if (showDeleteDialog) {
        com.app.qrkit.ui.components.CustomAlertDialog(
            title = "Delete QR Code",
            message = "Are you sure you want to delete this QR code? This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                qr?.let { 
                    viewModel.deleteQrCode(it)
                    val file = if (it.imagePath.startsWith("/")) File(it.imagePath) else File(context.filesDir, it.imagePath)
                    if (file.exists()) file.delete()
                }
                showDeleteDialog = false
                onBackClick()
            },
            onDismiss = { showDeleteDialog = false },
            isDestructive = true,
            isDark = isDark
        )
    }

    Scaffold(
        containerColor = bodyBgColor,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor, scrolledContainerColor = containerColor),
                title = {},
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
                        onClick = { showInfoSheet = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(com.app.qrkit.ui.icons.InfoIcon, contentDescription = "Info", modifier = Modifier.size(28.dp), tint = textColor)
                    }
                }
            )
        },
        bottomBar = {
            if (qr != null) {
                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.app.qrkit.ui.components.ScaleOnPressFilledIconButton(
                                onClick = { shareImage() },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1976D2)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(com.app.qrkit.ui.icons.ShareIcon, "Share", modifier = Modifier.size(28.dp))
                            }
                            com.app.qrkit.ui.components.ScaleOnPressFilledIconButton(
                                onClick = {
                                    qr?.let { q ->
                                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            val file = if (q.imagePath.startsWith("/")) File(q.imagePath) else File(context.filesDir, q.imagePath)
                                            if (file.exists()) {
                                                try {
                                                    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                                                    if (bitmap != null) {
                                                        val contentValues = android.content.ContentValues().apply {
                                                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "QR_${System.currentTimeMillis()}.png")
                                                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                                                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/QRKit")
                                                        }
                                                        val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                                                        if (uri != null) {
                                                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                                                            }
                                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
                                                            }
                                                        } else {
                                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                        Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF388E3C)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(com.app.qrkit.ui.icons.DownloadIcon, "Download", modifier = Modifier.size(28.dp))
                            }
                            com.app.qrkit.ui.components.ScaleOnPressFilledIconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isDark) Color(0xFFCE4235).copy(alpha = 0.16f) else Color(0xFFFFEBEE),
                                    contentColor = Color(0xFFCE4235)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(com.app.qrkit.ui.icons.DeleteIcon, "Delete", modifier = Modifier.size(28.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        val rawContent = qr?.content ?: ""
                        when (qr?.type?.lowercase()) {
                            "url" -> {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { copyToClipboard("URL", rawContent) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Copy", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            val url = if (!rawContent.startsWith("http://") && !rawContent.startsWith("https://")) "https://$rawContent" else rawContent
                                            context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = onAccentColor),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Open Link", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            "phone" -> {
                                val phone = rawContent.removePrefix("tel:")
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { copyToClipboard("Phone", phone) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Copy", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, android.net.Uri.parse(rawContent))) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = onAccentColor),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Call", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            "email" -> {
                                val email = rawContent.removePrefix("mailto:")
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { copyToClipboard("Email", email) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Copy", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { context.startActivity(Intent(Intent.ACTION_SENDTO, android.net.Uri.parse(rawContent))) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = onAccentColor),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Email", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            "wifi" -> {
                                val pass = Regex("P:([^;]+)").find(rawContent)?.groupValues?.get(1) ?: ""
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { copyToClipboard("Password", pass) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Copy Pass", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            if (pass.isNotEmpty()) {
                                                copyToClipboard("Password", pass)
                                            }
                                            context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
                                        },
                                        modifier = Modifier.weight(1.5f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = onAccentColor),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Connect", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            "location" -> {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { copyToClipboard("Location", rawContent) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Copy", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            val uri = if (rawContent.startsWith("geo:")) android.net.Uri.parse(rawContent) else android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(rawContent)}")
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "No maps application found", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = onAccentColor),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Open Maps", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            "contact" -> {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { copyToClipboard("Contact vCard", rawContent) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Copy", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                                    type = android.provider.ContactsContract.RawContacts.CONTENT_TYPE
                                                    val fn = Regex("FN:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)
                                                    val phone = Regex("TEL[^:]*:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)
                                                    val email = Regex("EMAIL[^:]*:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)
                                                    val company = Regex("ORG:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)
                                                    val title = Regex("TITLE:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)
                                                    val notes = Regex("NOTE:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)
                                                    if (!fn.isNullOrBlank()) putExtra(android.provider.ContactsContract.Intents.Insert.NAME, fn)
                                                    if (!phone.isNullOrBlank()) putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, phone)
                                                    if (!email.isNullOrBlank()) putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL, email)
                                                    if (!company.isNullOrBlank()) putExtra(android.provider.ContactsContract.Intents.Insert.COMPANY, company)
                                                    if (!title.isNullOrBlank()) putExtra(android.provider.ContactsContract.Intents.Insert.JOB_TITLE, title)
                                                    if (!notes.isNullOrBlank()) putExtra(android.provider.ContactsContract.Intents.Insert.NOTES, notes)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                copyToClipboard("Contact", rawContent)
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = onAccentColor),
                                        shape = CircleShape,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                    ) {
                                        Text("Add Contact", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { copyToClipboard("Content", rawContent) },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = onAccentColor),
                                    shape = CircleShape,
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                ) {
                                    Text("Copy", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bodyBgColor)
        ) {
            if (qr == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isFramed = qr?.isFramed(context) == true
                    if (isFramed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.72f)
                                .aspectRatio(600f / 740f)
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "qr_${qr!!.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qr!!.imagePath.isNotBlank()) {
                                val imgFile = remember(qr!!.imagePath) {
                                    if (qr!!.imagePath.startsWith("/")) File(qr!!.imagePath) else File(context.filesDir, qr!!.imagePath)
                                }
                                val imageRequest = remember(imgFile) {
                                    coil.request.ImageRequest.Builder(context)
                                        .data(imgFile)
                                        .crossfade(false)
                                        .allowHardware(true)
                                        .build()
                                }
                                AsyncImage(
                                    model = imageRequest,
                                    contentDescription = "QR Code",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    } else {
                        val qrBgRadius = qr?.bgRadius ?: 0f
                        val cornerPercent = (qrBgRadius * 50).coerceIn(0f, 50f).toInt()
                        val qrShape = remember(cornerPercent) { RoundedCornerShape(percent = cornerPercent) }
                        val qrBgColor = Color(qr?.bgColor ?: 0xFFFFFFFFL)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .aspectRatio(1f)
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "qr_${qr!!.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    clipInOverlayDuringTransition = OverlayClip(qrShape)
                                )
                                .shadow(
                                    elevation = if (cornerPercent > 0) 3.dp else 2.dp,
                                    shape = qrShape,
                                    ambientColor = Color.Black.copy(alpha = 0.08f),
                                    spotColor = Color.Black.copy(alpha = 0.04f)
                                )
                                .clip(qrShape)
                                .background(qrBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qr!!.imagePath.isNotBlank()) {
                                val imgFile = remember(qr!!.imagePath) {
                                    if (qr!!.imagePath.startsWith("/")) File(qr!!.imagePath) else File(context.filesDir, qr!!.imagePath)
                                }
                                val imageRequest = remember(imgFile) {
                                    coil.request.ImageRequest.Builder(context)
                                        .data(imgFile)
                                        .crossfade(false)
                                        .allowHardware(true)
                                        .build()
                                }
                                AsyncImage(
                                    model = imageRequest,
                                    contentDescription = "QR Code",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = false },
            sheetState = sheetState,
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
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
                        Text("Name", style = MaterialTheme.typography.labelMedium, color = if (isDark) Color(0xFFAAAAAA) else Color.Gray)
                        val displayName = if (qr?.categoryName.isNullOrBlank() ||
                            qr?.categoryName.equals("Location QR", ignoreCase = true) ||
                            qr?.categoryName.equals("Contact QR", ignoreCase = true) ||
                            qr?.categoryName.equals("WiFi QR", ignoreCase = true) ||
                            qr?.categoryName?.startsWith("Location (") == true ||
                            qr?.categoryName.equals(qr?.type, ignoreCase = true)) {
                            "QR Code"
                        } else {
                            qr?.categoryName ?: "QR Code"
                        }
                        Text(displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.5f))
                        
                        Text("Type", style = MaterialTheme.typography.labelMedium, color = if (isDark) Color(0xFFAAAAAA) else Color.Gray)
                        Text(qr?.type ?: "Text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.5f))
                        
                        Text("Content", style = MaterialTheme.typography.labelMedium, color = if (isDark) Color(0xFFAAAAAA) else Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        QrContentDetails(qr = qr, textColor = textColor)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, textColor: Color) {
    if (value.isNotBlank()) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = textColor)) {
                    append("$label - ")
                }
                append(value)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = textColor.copy(alpha = 0.9f),
            modifier = Modifier.padding(vertical = 3.dp)
        )
    }
}

@Composable
private fun QrContentDetails(qr: QrCode?, textColor: Color) {
    val rawContent = qr?.content ?: ""
    val type = qr?.type?.uppercase() ?: "TEXT"

    Column(modifier = Modifier.fillMaxWidth()) {
        when (type) {
            "PHONE" -> {
                val number = rawContent.removePrefix("tel:").trim()
                InfoRow("Number", number, textColor)
            }
            "WIFI" -> {
                val ssid = Regex("S:([^;]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val pass = Regex("P:([^;]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val sec = Regex("T:([^;]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                if (ssid.isNotBlank()) InfoRow("Network name", ssid, textColor)
                InfoRow("Password", pass.ifBlank { "None" }, textColor)
                if (sec.isNotBlank()) InfoRow("Security", sec, textColor)
            }
            "EMAIL" -> {
                val email = if (rawContent.startsWith("MATMSG:", ignoreCase = true)) {
                    Regex("TO:([^;]+)", RegexOption.IGNORE_CASE).find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                } else {
                    rawContent.substringBefore("?").removePrefix("mailto:").trim()
                }
                val subject = if (rawContent.startsWith("MATMSG:", ignoreCase = true)) {
                    Regex("SUB:([^;]+)", RegexOption.IGNORE_CASE).find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                } else {
                    Regex("[?&]subject=([^&]+)", RegexOption.IGNORE_CASE).find(rawContent)?.groupValues?.get(1)?.let { android.net.Uri.decode(it) } ?: ""
                }
                val body = if (rawContent.startsWith("MATMSG:", ignoreCase = true)) {
                    Regex("BODY:([^;]+)", RegexOption.IGNORE_CASE).find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                } else {
                    Regex("[?&]body=([^&]+)", RegexOption.IGNORE_CASE).find(rawContent)?.groupValues?.get(1)?.let { android.net.Uri.decode(it) } ?: ""
                }
                if (email.isNotBlank()) InfoRow("Email", email, textColor)
                if (subject.isNotBlank()) InfoRow("Subject", subject, textColor)
                if (body.isNotBlank()) InfoRow("Body", body, textColor)
            }
            "URL" -> {
                InfoRow("URL", rawContent.trim(), textColor)
            }
            "SMS" -> {
                val num = Regex("sms(?:to)?:([^:?]+)", RegexOption.IGNORE_CASE).find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val msg = if (rawContent.contains("?body=", ignoreCase = true)) {
                    Regex("[?&]body=([^&]+)", RegexOption.IGNORE_CASE).find(rawContent)?.groupValues?.get(1)?.let { android.net.Uri.decode(it) } ?: ""
                } else {
                    Regex("sms(?:to)?:[^:]*:(.*)", RegexOption.IGNORE_CASE).find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                }
                if (num.isNotBlank()) InfoRow("Number", num, textColor)
                if (msg.isNotBlank()) InfoRow("Message", msg, textColor)
            }
            "LOCATION" -> {
                val place = Regex("[?&]q=([^&]+)").find(rawContent)?.groupValues?.get(1)?.let { android.net.Uri.decode(it) } ?: ""
                val geoMatch = Regex("geo:([-\\d.]+),([-\\d.]+)").find(rawContent)
                if (place.isNotBlank()) InfoRow("Location", place, textColor)
                if (geoMatch != null) InfoRow("Coordinates", "${geoMatch.groupValues[1]}, ${geoMatch.groupValues[2]}", textColor)
                if (place.isBlank() && geoMatch == null) InfoRow("Location", rawContent, textColor)
            }
            "CONTACT" -> {
                val fn = Regex("FN:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim()
                    ?: run {
                        val nMatch = Regex("N:([^;\\n\\r]*);([^;\\n\\r]*)").find(rawContent)
                        if (nMatch != null) "${nMatch.groupValues[2]} ${nMatch.groupValues[1]}".trim() else ""
                    }
                val phone = Regex("TEL[^:]*:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val email = Regex("EMAIL[^:]*:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val org = Regex("ORG:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val title = Regex("TITLE:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val adr = Regex("ADR[^:]*:(?:;;)?([^;\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.replace(";", " ")?.trim() ?: ""
                val website = Regex("URL:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val note = Regex("NOTE:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""

                if (fn.isNotBlank()) InfoRow("Name", fn, textColor)
                if (phone.isNotBlank()) InfoRow("Number", phone, textColor)
                if (email.isNotBlank()) InfoRow("Email", email, textColor)
                if (org.isNotBlank()) InfoRow("Company", org, textColor)
                if (title.isNotBlank()) InfoRow("Job title", title, textColor)
                if (adr.isNotBlank()) InfoRow("Address", adr, textColor)
                if (website.isNotBlank()) InfoRow("Website", website, textColor)
                if (note.isNotBlank()) InfoRow("Note", note, textColor)
            }
            "EVENT" -> {
                val summary = Regex("SUMMARY:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val location = Regex("LOCATION:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                val desc = Regex("DESCRIPTION:([^\\n\\r]+)").find(rawContent)?.groupValues?.get(1)?.trim() ?: ""
                if (summary.isNotBlank()) InfoRow("Title", summary, textColor)
                if (location.isNotBlank()) InfoRow("Location", location, textColor)
                if (desc.isNotBlank()) InfoRow("Description", desc, textColor)
            }
            else -> {
                if (rawContent.startsWith("http://") || rawContent.startsWith("https://")) {
                    InfoRow("URL", rawContent, textColor)
                } else if (rawContent.startsWith("tel:")) {
                    InfoRow("Number", rawContent.removePrefix("tel:"), textColor)
                } else if (rawContent.startsWith("mailto:")) {
                    InfoRow("Email", rawContent.removePrefix("mailto:"), textColor)
                } else {
                    InfoRow("Text", rawContent, textColor)
                }
            }
        }
    }
}











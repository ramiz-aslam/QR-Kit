package com.app.qrkit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.qrkit.data.QrCode
import com.app.qrkit.ui.QrViewModel
import com.app.qrkit.ui.icons.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SharedTransitionScope.VaultScreen(
    viewModel: QrViewModel,
    onBackClick: () -> Unit,
    onQrClick: (String) -> Unit,
    onGenerateClick: () -> Unit,
    onFabClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val qrs by viewModel.allQrCodes.collectAsState()

    val isDark = com.app.qrkit.ui.theme.isAppInDarkTheme()
    val containerColor = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val context = LocalContext.current

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedQrIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Intercept back button when search or selection mode is active
    BackHandler(enabled = isSearchActive || isSelectionMode) {
        if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        }
        if (isSelectionMode) {
            isSelectionMode = false
            selectedQrIds = emptySet()
        }
    }

    // Filter QRs based on search query
    val filteredQrs = remember(qrs, searchQuery) {
        if (searchQuery.isBlank()) {
            qrs
        } else {
            val query = searchQuery.trim().lowercase()
            qrs.filter { qr ->
                val dName = getVaultQrDisplayName(qr.categoryName, qr.type)
                dName.lowercase().contains(query) ||
                qr.type.lowercase().contains(query) ||
                qr.content.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        containerColor = containerColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            when {
                isSearchActive -> {
                    val searchSurfaceColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)
                    val searchBorderColor = if (isDark) Color(0xFF353535) else Color.Transparent
                    val isDynamic = com.app.qrkit.ui.theme.LocalAppDynamicColor.current
                    val cursorColor = if (isDynamic) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color.Black)

                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = containerColor,
                            scrolledContainerColor = containerColor
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                }
                            ) {
                                Icon(ArrowBackIcon, contentDescription = "Close search", modifier = Modifier.size(28.dp), tint = textColor)
                            }
                        },
                        title = {
                            Surface(
                                shape = CircleShape,
                                color = searchSurfaceColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .padding(end = 12.dp)
                                    .border(1.dp, searchBorderColor, CircleShape)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp)
                                ) {
                                    Icon(
                                        imageVector = SearchIcon,
                                        contentDescription = "Search",
                                        tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.45f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search QR",
                                                color = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = textColor,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            cursorBrush = SolidColor(cursorColor),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(searchFocusRequester)
                                        )
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Clear search",
                                                tint = textColor.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            LaunchedEffect(Unit) {
                                searchFocusRequester.requestFocus()
                            }
                        }
                    )
                }
                isSelectionMode -> {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = containerColor,
                            scrolledContainerColor = containerColor
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    isSelectionMode = false
                                    selectedQrIds = emptySet()
                                }
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Cancel selection", tint = textColor)
                            }
                        },
                        title = {
                            Text(
                                text = if (selectedQrIds.isEmpty()) "Select items" else "${selectedQrIds.size} selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = textColor
                            )
                        },
                        actions = {
                            val allSelected = filteredQrs.isNotEmpty() && selectedQrIds.size == filteredQrs.size
                            TextButton(
                                onClick = {
                                    selectedQrIds = if (allSelected) {
                                        emptySet()
                                    } else {
                                        filteredQrs.map { it.id }.toSet()
                                    }
                                }
                            ) {
                                Text(
                                    text = if (allSelected) "Deselect All" else "Select All",
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (selectedQrIds.isNotEmpty()) {
                                        showDeleteDialog = true
                                    }
                                },
                                enabled = selectedQrIds.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = DeleteIcon,
                                    contentDescription = "Delete selected",
                                    modifier = Modifier.size(28.dp),
                                    tint = if (selectedQrIds.isNotEmpty()) Color(0xFFCE4235) else textColor.copy(alpha = 0.3f)
                                )
                            }
                        }
                    )
                }
                else -> {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = containerColor,
                            scrolledContainerColor = containerColor
                        ),
                        title = {
                            Text(
                                "VAULT",
                                fontFamily = com.app.qrkit.ui.theme.InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                letterSpacing = 0.sp,
                                color = textColor
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = { isSearchActive = true }
                            ) {
                                Icon(SearchIcon, contentDescription = "Search", modifier = Modifier.size(28.dp), tint = textColor)
                            }
                            IconButton(
                                onClick = {
                                    isSelectionMode = true
                                    selectedQrIds = emptySet()
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Icon(EditIcon, contentDescription = "Edit", modifier = Modifier.size(28.dp), tint = textColor)
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (qrs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Your vault is empty.", color = Color.Gray)
                    }
                } else if (filteredQrs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No QR codes match \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(filteredQrs, key = { _, qr -> qr.id }) { index, qr ->
                            val isSelected = selectedQrIds.contains(qr.id)
                            val topRadius = if (index == 0) 16.dp else 5.dp
                            val bottomRadius = if (index == filteredQrs.size - 1) 16.dp else 5.dp
                            val shape = RoundedCornerShape(
                                topStart = topRadius,
                                topEnd = topRadius,
                                bottomEnd = bottomRadius,
                                bottomStart = bottomRadius
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 2.dp,
                                        shape = shape,
                                        ambientColor = Color.Black.copy(alpha = 0.08f),
                                        spotColor = Color.Black.copy(alpha = 0.04f)
                                    )
                                    .clip(shape)
                                    .background(
                                        if (isSelected) {
                                            if (isDark) Color(0xFF454545) else Color(0xFFE2E2E2)
                                        } else {
                                            if (isDark) Color(0xFF353535) else Color(0xFFF2F2F2)
                                        }
                                    )
                                    .combinedClickable(
                                        onClick = {
                                            if (isSelectionMode) {
                                                selectedQrIds = if (isSelected) {
                                                    selectedQrIds - qr.id
                                                } else {
                                                    selectedQrIds + qr.id
                                                }
                                            } else {
                                                onQrClick(qr.id)
                                            }
                                        },
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedQrIds = setOf(qr.id)
                                            }
                                        }
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AnimatedVisibility(
                                    visible = isSelectionMode,
                                    enter = expandHorizontally() + fadeIn(),
                                    exit = shrinkHorizontally() + fadeOut()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f)),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                val cornerPercent = (qr.bgRadius * 50).coerceIn(0f, 50f).toInt()
                                val qrShape = RoundedCornerShape(percent = cornerPercent)
                                val qrBgColor = Color(qr.bgColor)
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "qr_${qr.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            clipInOverlayDuringTransition = OverlayClip(qrShape)
                                        )
                                        .clip(qrShape)
                                        .background(qrBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (qr.imagePath.isNotBlank()) {
                                        val imgFile = if (qr.imagePath.startsWith("/")) File(qr.imagePath) else File(context.filesDir, qr.imagePath)
                                        AsyncImage(
                                            model = imgFile,
                                            contentDescription = "QR Code",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    val displayName = getVaultQrDisplayName(qr.categoryName, qr.type)
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(qr.createdAt))
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (isDark) Color(0xFF1E1E1E) else Color.Black,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(34.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = getQrTypeIcon(qr.type),
                                            contentDescription = qr.type,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && selectedQrIds.isNotEmpty()) {
        val count = selectedQrIds.size
        com.app.qrkit.ui.components.CustomAlertDialog(
            title = if (count == 1) "Delete QR Code" else "Delete $count QR Codes",
            message = if (count == 1) {
                "Are you sure you want to delete this QR code? This action cannot be undone."
            } else {
                "Are you sure you want to delete these $count QR codes? This action cannot be undone."
            },
            confirmText = "Delete",
            onConfirm = {
                val toDelete = qrs.filter { it.id in selectedQrIds }
                viewModel.deleteQrCodes(toDelete)
                CoroutineScope(Dispatchers.IO).launch {
                    toDelete.forEach { qr ->
                        val file = if (qr.imagePath.startsWith("/")) File(qr.imagePath) else File(context.filesDir, qr.imagePath)
                        if (file.exists()) file.delete()
                    }
                }
                android.widget.Toast.makeText(
                    context,
                    "Deleted $count QR code${if (count > 1) "s" else ""}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                selectedQrIds = emptySet()
                isSelectionMode = false
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            isDestructive = true,
            isDark = isDark
        )
    }
}

private fun getQrTypeIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type.uppercase()) {
        "LOCATION" -> LocationOnIcon
        "CONTACT" -> PersonIcon
        "WIFI" -> WifiIcon
        "PHONE" -> CallIcon
        "EMAIL" -> MailIcon
        "URL" -> LinkIcon
        "SMS" -> MailIcon
        "EVENT" -> DescriptionIcon
        else -> TextAdIcon
    }
}

private fun getVaultQrDisplayName(categoryName: String?, type: String): String {
    return if (categoryName.isNullOrBlank() ||
        categoryName.equals("Location QR", ignoreCase = true) ||
        categoryName.equals("Contact QR", ignoreCase = true) ||
        categoryName.equals("WiFi QR", ignoreCase = true) ||
        categoryName.startsWith("Location (") ||
        categoryName.equals(type, ignoreCase = true)) {
        "QR Code"
    } else {
        categoryName
    }
}

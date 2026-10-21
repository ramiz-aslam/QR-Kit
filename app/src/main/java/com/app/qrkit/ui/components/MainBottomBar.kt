package com.app.qrkit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.app.qrkit.ui.icons.AddIcon
import com.app.qrkit.ui.icons.VaultIcon
import com.app.qrkit.ui.icons.HomeIcon

@Composable
fun MainBottomBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onVaultClick: () -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Pill on the Left
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isHome = currentRoute == "home"
            val isVault = currentRoute == "vault"

            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = HomeIcon,
                    contentDescription = "Home",
                    tint = if (isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onVaultClick) {
                Icon(
                    imageVector = VaultIcon,
                    contentDescription = "Vault",
                    tint = if (isVault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Separate FAB on the Right
        DynamicSquircleIconButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(AddIcon, "Create Sticker")
        }
    }
}


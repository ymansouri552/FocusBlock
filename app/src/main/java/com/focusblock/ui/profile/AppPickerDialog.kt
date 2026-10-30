package com.focusblock.ui.profile

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.R
import com.focusblock.domain.model.InstalledApp
import com.focusblock.ui.theme.LocalCustomColors
import kotlin.math.absoluteValue

@Composable
fun AppPickerDialog(
    apps: List<InstalledApp>,
    selectedPackages: Set<String>,
    lockedPackages: Set<String> = emptySet(),
    onTogglePackage: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Selected apps are sorted to the beginning of the list, then alphabetically by label.
    val filteredApps = remember(apps, searchQuery, selectedPackages) {
        val list = if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
        list.sortedWith(
            compareByDescending<InstalledApp> { selectedPackages.contains(it.packageName) }
                .thenBy { it.label.lowercase() }
        )
    }

    val customColors = LocalCustomColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.app_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${selectedPackages.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(id = R.string.app_picker_search_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Quick Toggle Row (Select Visible / Clear)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredApps.size} apps · selected first",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        Text(
                            text = stringResource(id = R.string.app_picker_select_all),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    filteredApps.forEach { app ->
                                        if (!selectedPackages.contains(app.packageName)) {
                                            onTogglePackage(app.packageName)
                                        }
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.app_picker_clear_all),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clickable {
                                    filteredApps.forEach { app ->
                                        if (selectedPackages.contains(app.packageName) && !lockedPackages.contains(app.packageName)) {
                                            onTogglePackage(app.packageName)
                                        }
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (apps.isEmpty()) stringResource(id = R.string.app_picker_loading)
                            else stringResource(id = R.string.app_picker_no_apps),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isLocked = lockedPackages.contains(app.packageName)
                            AppPickerRow(
                                app = app,
                                selected = selectedPackages.contains(app.packageName),
                                enabled = !isLocked,
                                isLockedByStrict = isLocked,
                                onToggle = { onTogglePackage(app.packageName) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("${stringResource(id = R.string.save)} (${selectedPackages.size})", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
private fun AppPickerRow(
    app: InstalledApp,
    selected: Boolean,
    enabled: Boolean = true,
    isLockedByStrict: Boolean = false,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val initial = app.label.firstOrNull()?.uppercaseChar() ?: 'A'
    val badgeColors = remember(app.packageName) {
        val palette = listOf(
            Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFF10B981),
            Color(0xFFF59E0B), Color(0xFFEC4899), Color(0xFF8B5CF6)
        )
        palette[app.packageName.hashCode().absoluteValue % palette.size]
    }

    // Asynchronously load app icon off the main thread with LRU memory caching
    val iconBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = AppIconCache.get(app.packageName),
        key1 = app.packageName
    ) {
        if (value == null) {
            value = AppIconCache.loadIcon(context, app.packageName)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon Badge
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(40.dp)
        ) {
            val currentIcon = iconBitmap
            if (currentIcon != null) {
                Image(
                    bitmap = currentIcon,
                    contentDescription = app.label,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initial.toString(),
                        fontWeight = FontWeight.Bold,
                        color = badgeColors,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            )
            Text(
                text = if (isLockedByStrict) stringResource(id = R.string.app_picker_locked_strict)
                else app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = if (isLockedByStrict) LocalCustomColors.current.strictAmber
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLockedByStrict) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = LocalCustomColors.current.strictAmber,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 4.dp)
            )
        }

        Checkbox(
            checked = selected,
            enabled = enabled,
            onCheckedChange = { if (enabled) onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = if (isLockedByStrict) LocalCustomColors.current.strictAmber else MaterialTheme.colorScheme.primary
            )
        )
    }
}

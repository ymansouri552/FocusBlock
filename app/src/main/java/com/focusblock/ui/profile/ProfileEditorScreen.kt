package com.focusblock.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.ui.theme.LocalCustomColors
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileEditorScreen(
    viewModel: ProfileEditorViewModel,
    onBack: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val profile = viewModel.profile
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val selectedPackages by viewModel.selectedPackages.collectAsStateWithLifecycle()
    val editingSchedule by viewModel.editingSchedule.collectAsStateWithLifecycle()
    val scheduleEditorOpen by viewModel.scheduleEditorOpen.collectAsStateWithLifecycle()
    val scheduleEditorIndex by viewModel.scheduleEditorIndex.collectAsStateWithLifecycle()
    val customColors = LocalCustomColors.current

    val now = Instant.now()
    val isStrictActive = StrictModeRules.isStrictActive(profile, now)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (profile.id == 0) stringResource(id = R.string.profile_editor_new)
                        else stringResource(id = R.string.profile_editor_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.close)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save(onSuccess = onBack) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(id = R.string.save), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Error Banner ────────────────────────────────────────────────
            ui.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // ── Section 1: Profile Identity Card ────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, customColors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PROFILE IDENTITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = profile.name,
                        onValueChange = { if (!isStrictActive) viewModel.setName(it) },
                        label = { Text(stringResource(id = R.string.profile_name)) },
                        placeholder = { Text("e.g. Deep Work, Study Hours") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isStrictActive,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.profile_enabled),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(id = R.string.profile_enabled_summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = profile.enabled,
                            onCheckedChange = { viewModel.toggleEnabled(it) },
                            enabled = !isStrictActive
                        )
                    }
                }
            }

            // ── Section 2: Blocked Apps Hub ─────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, customColors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.profile_blocked_apps),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${profile.blockedApps.size} selected",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (profile.blockedApps.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            profile.blockedApps.forEach { pkg ->
                                val appInfo = installedApps.find { it.packageName == pkg }
                                val displayName = appInfo?.label ?: pkg.substringAfterLast('.')
                                val isLockedInStrict = isStrictActive && viewModel.originalBlockedApps.contains(pkg)
                                InputChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text(displayName) },
                                    trailingIcon = {
                                        if (isLockedInStrict) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = customColors.strictAmber
                                            )
                                        } else if (!isStrictActive) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { viewModel.removeApp(pkg) }
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = InputChipDefaults.inputChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    } else {
                        Text(
                            text = "No apps blocked yet. Tap below to pick distracting apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    FilledTonalButton(
                        onClick = { viewModel.setAppPickerOpen(true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(id = R.string.profile_pick_apps), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Section 3: Schedules Hub ────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, customColors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.profile_schedule),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setScheduleEditorOpen(true, null, null) },
                            enabled = !isStrictActive
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.schedule_add),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (profile.schedules.isEmpty()) {
                        OutlinedCard(
                            onClick = { viewModel.setScheduleEditorOpen(true, null, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, customColors.cardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(id = R.string.profile_schedule_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "+ Add Schedule Window",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        profile.schedules.forEachIndexed { index, schedule ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, customColors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = formatScheduleSummary(schedule),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.setScheduleEditorOpen(true, schedule, index) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    if (!isStrictActive) {
                                        IconButton(
                                            onClick = { viewModel.removeSchedule(index) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
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

            // ── Strict Protection Notice (Only shown when Strict Mode is actively protecting) ──
            if (isStrictActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = customColors.strictContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, customColors.strictAmber.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = customColors.strictAmber.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = customColors.strictAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "STRICT MODE ACTIVE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = customColors.strictAmber
                            )
                            Text(
                                text = "Profiles are protected under independent strict terms. Deactivation, deletion, and app removal are locked.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Delete Profile (Danger Zone) ─────────────────────────────────
            if (profile.id != 0 && !isStrictActive) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.delete(onBack) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.delete), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Dialogs ─────────────────────────────────────────────────────────────

    // App Picker Dialog
    if (ui.appPickerOpen) {
        AppPickerDialog(
            apps = installedApps,
            selectedPackages = selectedPackages,
            lockedPackages = if (isStrictActive) viewModel.originalBlockedApps else emptySet(),
            onTogglePackage = { viewModel.toggleSelectedPackage(it) },
            onDismiss = { viewModel.setAppPickerOpen(false) },
            onConfirm = { viewModel.confirmAppSelection() }
        )
    }

    // Schedule Editor Dialog
    if (scheduleEditorOpen || ui.scheduleOpen) {
        ScheduleEditorDialog(
            initialSchedule = editingSchedule,
            index = scheduleEditorIndex,
            onDismiss = { viewModel.setScheduleEditorOpen(false) },
            onSave = { schedule ->
                val index = scheduleEditorIndex
                if (index != null && index >= 0) {
                    viewModel.updateSchedule(index, schedule)
                } else {
                    viewModel.addSchedule(schedule)
                }
                viewModel.setScheduleEditorOpen(false)
            },
            onDelete = scheduleEditorIndex?.let { index ->
                if (!isStrictActive) {
                    { viewModel.removeSchedule(index) }
                } else null
            }
        )
    }
}

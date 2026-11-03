package com.focusblock.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.ui.components.DayBar24h
import com.focusblock.ui.components.SectionHeader
import com.focusblock.ui.components.StampLabel
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.tabular
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
    val palette = LocalPledgePalette.current

    val now = Instant.now()
    val isStrictActive = StrictModeRules.isStrictActive(profile, now)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (profile.id == 0) "NEW CONTRACT SPEC" else "EDIT CONTRACT SPEC",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = palette.ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.close),
                            tint = palette.ink
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save(onSuccess = onBack) },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent,
                            contentColor = palette.onAccent
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("SAVE SPEC", fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.background
                )
            )
        },
        containerColor = palette.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Banner
            ui.error?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp),
                    color = palette.stampSurface,
                    border = BorderStroke(1.dp, palette.stamp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = palette.stamp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.stamp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Section 1: Profile Identity
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                color = palette.surface,
                border = BorderStroke(1.dp, palette.hairline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONTRACT IDENTITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary,
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = profile.name,
                        onValueChange = { if (!isStrictActive) viewModel.setName(it) },
                        placeholder = { Text("e.g. Deep Work, Study Hours", color = palette.inkSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isStrictActive,
                        singleLine = true,
                        shape = RoundedCornerShape(2.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.border,
                            unfocusedBorderColor = palette.hairline,
                            focusedTextColor = palette.ink,
                            unfocusedTextColor = palette.ink
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CONTRACT ACTIVE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.ink
                            )
                            Text(
                                text = "When disabled, scheduled rules are temporarily ignored",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.inkSecondary
                            )
                        }
                        Switch(
                            checked = profile.enabled,
                            onCheckedChange = { viewModel.toggleEnabled(it) },
                            enabled = !isStrictActive,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.onAccent,
                                checkedTrackColor = palette.accent,
                                uncheckedThumbColor = palette.inkSecondary,
                                uncheckedTrackColor = palette.surface
                            )
                        )
                    }
                }
            }

            // Section 2: Blocked Apps Hub
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                color = palette.surface,
                border = BorderStroke(1.dp, palette.hairline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TARGET APPLICATIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.inkSecondary,
                            letterSpacing = 1.0.sp
                        )
                        Text(
                            text = "${profile.blockedApps.size} SELECTED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.accent
                        )
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

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .border(1.dp, palette.hairline, RoundedCornerShape(2.dp)),
                                    color = palette.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.ink
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (isLockedInStrict) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = palette.stamp
                                            )
                                        } else if (!isStrictActive) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { viewModel.removeApp(pkg) },
                                                tint = palette.inkSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    } else {
                        Text(
                            text = "No applications assigned to this profile yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.inkSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = { viewModel.setAppPickerOpen(true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.border,
                            contentColor = palette.surface
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SELECT APPLICATIONS", fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    }
                }
            }

            // Section 3: Schedules Hub
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                color = palette.surface,
                border = BorderStroke(1.dp, palette.hairline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SCHEDULE TIMELINES",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.inkSecondary,
                            letterSpacing = 1.0.sp
                        )
                        if (!isStrictActive) {
                            TextButton(onClick = { viewModel.setScheduleEditorOpen(true, null, null) }) {
                                Text("+ ADD WINDOW", style = MaterialTheme.typography.labelSmall, color = palette.accent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (profile.schedules.isEmpty()) {
                        Text(
                            text = "No schedules configured. This profile will only run on manual demand.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.inkSecondary
                        )
                    } else {
                        profile.schedules.forEachIndexed { index, schedule ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, palette.hairline, RoundedCornerShape(2.dp)),
                                color = palette.surface
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatScheduleSummary(schedule),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.ink,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row {
                                            IconButton(
                                                onClick = { viewModel.setScheduleEditorOpen(true, schedule, index) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = palette.ink,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            if (!isStrictActive) {
                                                IconButton(
                                                    onClick = { viewModel.removeSchedule(index) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = palette.stamp,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    DayBar24h(
                                        startTime = schedule.startTime,
                                        endTime = schedule.endTime,
                                        isBlockMode = schedule.mode == ScheduleMode.BLOCK,
                                        height = 8.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Strict Protection Notice
            if (isStrictActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp),
                    color = palette.stampSurface,
                    border = BorderStroke(1.5.dp, palette.stamp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StampLabel(text = "LOCKED")
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "STRICT PROTECTION ACTIVE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.stamp
                            )
                            Text(
                                text = "This profile is locked under an active pledge. Disabling, deleting, and app removal are prohibited.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.inkSecondary
                            )
                        }
                    }
                }
            }

            // Delete Profile Button
            if (profile.id != 0 && !isStrictActive) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.delete(onBack) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = palette.stamp
                    ),
                    border = BorderStroke(1.5.dp, palette.stamp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DELETE CONTRACT", fontWeight = FontWeight.Bold, letterSpacing = 1.0.sp)
                }
            }
        }
    }

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

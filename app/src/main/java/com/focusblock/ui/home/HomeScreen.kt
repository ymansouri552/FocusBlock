package com.focusblock.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.domain.model.ActiveProfileInfo
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.domain.usecase.SystemPermissionsState
import com.focusblock.ui.profile.formatScheduleSummary
import com.focusblock.ui.theme.LocalCustomColors
import com.focusblock.ui.theme.tabular
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddProfile: () -> Unit,
    onEditProfile: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val blockedPackages by viewModel.blockedPackages.collectAsStateWithLifecycle()
    val strictModeState by viewModel.strictModeState.collectAsStateWithLifecycle()
    val permissionsState by viewModel.permissionsState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var now by remember { mutableStateOf(Instant.now()) }

    // Tick every second ONLY while an active session exists; otherwise update once a minute
    val isAnySessionActive = remember(profiles, strictModeState, now) {
        strictModeState.isEffectivelyActive(now) || ScheduleEngine.activeProfiles(profiles, now).isNotEmpty()
    }

    LaunchedEffect(isAnySessionActive) {
        if (isAnySessionActive) {
            while (true) {
                delay(1000)
                now = viewModel.now()
            }
        } else {
            while (true) {
                delay(60_000)
                now = viewModel.now()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val activeProfiles = remember(profiles, now) {
        ScheduleEngine.activeProfiles(profiles, now)
    }

    var showQuickBlockDialog by remember { mutableStateOf(false) }
    var preselectedQuickDuration by remember { mutableIntStateOf(30) }
    var showStrictTermsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val error = ui.error
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.settings_title)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Prime slot: Focus Status block owns the top of the screen (no card chrome)
            item {
                FocusStatusBlock(
                    activeProfiles = activeProfiles,
                    now = now,
                    blockedPackagesCount = blockedPackages.size
                )
            }

            // Collapsed single-line alert under status ONLY when something is broken; nothing when green
            if (permissionsState.hasAnyWarning) {
                item {
                    PermissionsWarningLine(
                        permissionsState = permissionsState,
                        onFix = {
                            if (!permissionsState.isAccessibilityGranted) {
                                runCatching {
                                    context.startActivity(viewModel.permissionChecker.getAccessibilitySettingsIntent())
                                }
                            } else if (!permissionsState.isDeviceAdminGranted) {
                                runCatching {
                                    context.startActivity(viewModel.permissionChecker.getDeviceAdminRequestIntent())
                                }
                            } else {
                                viewModel.permissionChecker.launchBatteryOptimizationSettings(context)
                            }
                        }
                    )
                }
            }

            // Quick durations: plain row of text buttons
            if (profiles.isNotEmpty()) {
                item {
                    QuickStartBar(
                        onSelectDuration = { duration ->
                            preselectedQuickDuration = duration
                            showQuickBlockDialog = true
                        }
                    )
                }
            }

            // Strict Mode: The single allowed bordered card when locked; clean text row when idle
            item {
                StrictModeSection(
                    strictState = strictModeState,
                    now = now,
                    onActivate = { showStrictTermsDialog = true },
                    onDisarm = { viewModel.disarmStrictMode() }
                )
            }

            // Profiles Header
            item {
                ProfilesHeader(
                    count = profiles.size,
                    onAddProfile = onAddProfile
                )
            }

            // Profiles List: hairline-separated rows
            if (profiles.isEmpty()) {
                item {
                    EmptyProfilesState(onCreateProfile = onAddProfile)
                }
            } else {
                itemsIndexed(profiles, key = { _, profile -> profile.id }) { index, profile ->
                    val isActiveNow = ScheduleEngine.isActive(profile, now)
                    val isStrictActive = StrictModeRules.isStrictActive(profile, now)
                    val activeInfo = ScheduleEngine.activeProfile(profile, now)
                    val remainingMs = activeInfo?.let { Duration.between(now, it.endInstant).toMillis() }

                    ProfileRow(
                        profile = profile,
                        isActiveNow = isActiveNow,
                        isStrictActive = isStrictActive,
                        activeInfo = activeInfo,
                        remainingMs = remainingMs,
                        showDivider = index < profiles.lastIndex,
                        onEdit = { onEditProfile(profile.id) },
                        onToggle = { enabled -> viewModel.toggleProfile(profile.id, enabled) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showQuickBlockDialog) {
        QuickBlockModal(
            initialDuration = preselectedQuickDuration,
            availableProfiles = profiles,
            onDismiss = { showQuickBlockDialog = false },
            onConfirm = { duration, apps ->
                viewModel.startQuickBlock(duration, apps)
                showQuickBlockDialog = false
            }
        )
    }

    if (showStrictTermsDialog) {
        StrictTermsDialog(
            onDismiss = { showStrictTermsDialog = false },
            onArm = { termType, customEnd ->
                viewModel.armStrictMode(termType, customEnd)
                showStrictTermsDialog = false
            }
        )
    }
}

@Composable
private fun FocusStatusBlock(
    activeProfiles: List<ActiveProfileInfo>,
    now: Instant,
    blockedPackagesCount: Int
) {
    val customColors = LocalCustomColors.current
    val hasActive = activeProfiles.isNotEmpty()
    val isAnyStrict = StrictModeRules.independentState.isEffectivelyActive(now) ||
        activeProfiles.any { it.profile.strict }

    if (hasActive) {
        val nextEnding = activeProfiles.minByOrNull { it.endInstant }
        val remainingMs = nextEnding?.let { Duration.between(now, it.endInstant).toMillis() } ?: 0L
        val stateColor = if (isAnyStrict) customColors.strictAmber else customColors.positive

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (isAnyStrict) "Strict Mode session" else "Focus session active",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = stateColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatTimer(remainingMs),
                style = MaterialTheme.typography.displayLarge.tabular(),
                fontWeight = FontWeight.Bold,
                color = stateColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = activeProfiles.joinToString(", ") { it.profile.name },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$blockedPackagesCount apps blocked",
                style = MaterialTheme.typography.bodySmall,
                color = customColors.subtleText
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Nothing is blocked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Start a timer or enable a profile to begin.",
                style = MaterialTheme.typography.bodyMedium,
                color = customColors.subtleText
            )
        }
    }
}

@Composable
private fun PermissionsWarningLine(
    permissionsState: SystemPermissionsState,
    onFix: () -> Unit
) {
    val message = when {
        !permissionsState.isAccessibilityGranted -> "Accessibility is off · Tap to enable"
        !permissionsState.isDeviceAdminGranted -> "Device admin is inactive · Tap to activate"
        else -> "Battery optimization active · Tap to exempt"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFix)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStartBar(
    onSelectDuration: (Int) -> Unit
) {
    val presets = listOf(15, 30, 45, 60, 120)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        presets.forEach { minutes ->
            TextButton(
                onClick = { onSelectDuration(minutes) },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatDurationShort(minutes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StrictModeSection(
    strictState: StrictModeState,
    now: Instant,
    onActivate: () -> Unit,
    onDisarm: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val isArmed = strictState.isEffectivelyActive(now)
    val termExpired = strictState.isArmed && !isArmed

    if (isArmed || termExpired) {
        val remainingMs = strictState.remainingMs(now)
        val shape = RoundedCornerShape(14.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(customColors.strictContainer)
                .border(1.dp, customColors.strictAmber.copy(alpha = 0.7f), shape)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = customColors.strictAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Strict Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = customColors.strictOnContainer
                )
                Text(
                    text = "${strictState.termType.label} commitment. All profiles are locked.",
                    style = MaterialTheme.typography.bodySmall,
                    color = customColors.strictOnContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (termExpired) {
                TextButton(onClick = onDisarm) {
                    Text("Disarm", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text(
                    text = if (remainingMs == null) "Ongoing" else formatTimer(remainingMs),
                    style = MaterialTheme.typography.titleMedium.tabular(),
                    fontWeight = FontWeight.Bold,
                    color = customColors.strictAmber,
                    maxLines = 1
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Strict Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Lock profiles for a fixed commitment term.",
                    style = MaterialTheme.typography.bodySmall,
                    color = customColors.subtleText
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            TextButton(onClick = onActivate) {
                Text("Set up", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProfilesHeader(
    count: Int,
    onAddProfile: () -> Unit
) {
    val customColors = LocalCustomColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.home_profiles),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            color = customColors.subtleText
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onAddProfile) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("New", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProfileRow(
    profile: Profile,
    isActiveNow: Boolean,
    isStrictActive: Boolean,
    activeInfo: ActiveProfileInfo?,
    remainingMs: Long?,
    showDivider: Boolean,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val customColors = LocalCustomColors.current

    val subtitle: String
    val subtitleColor: Color
    if (isActiveNow && remainingMs != null && remainingMs > 0) {
        val isUsageLimit = activeInfo?.schedule?.mode == ScheduleMode.USAGE_LIMIT
        subtitle = when {
            isStrictActive -> "Locked, ${formatCountdown(remainingMs)} remaining"
            isUsageLimit -> "Quota active, ${formatCountdown(remainingMs)} until reset"
            else -> "Active, ${formatCountdown(remainingMs)} remaining"
        }
        subtitleColor = if (isStrictActive) customColors.strictAmber else customColors.positive
    } else {
        val schedule = profile.schedules.firstOrNull()
        val appCount = profile.blockedApps.size
        subtitle = buildString {
            if (appCount > 0) append("$appCount apps")
            if (schedule != null) {
                if (isNotEmpty()) append(", ")
                append(formatScheduleSummary(schedule))
            }
            if (isEmpty()) append("No apps, no schedule")
        }
        subtitleColor = customColors.subtleText
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .heightIn(min = 68.dp)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name.ifBlank { "Untitled profile" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (isStrictActive) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = customColors.strictAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Locked",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = customColors.strictAmber
                    )
                }
            } else {
                Switch(
                    checked = profile.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = if (profile.strict) customColors.strictAmber
                        else MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(color = customColors.cardBorder, thickness = 1.dp)
        }
    }
}

@Composable
private fun EmptyProfilesState(onCreateProfile: () -> Unit) {
    val customColors = LocalCustomColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.home_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(id = R.string.home_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = customColors.subtleText
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCreateProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(stringResource(id = R.string.home_empty_action), fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickBlockModal(
    initialDuration: Int,
    availableProfiles: List<Profile>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Set<String>) -> Unit
) {
    var duration by remember { mutableIntStateOf(initialDuration) }
    val allUniqueApps = remember(availableProfiles) {
        availableProfiles.flatMap { it.blockedApps }.toSet()
    }
    var selectedApps by remember { mutableStateOf(allUniqueApps) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Quick session", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(15, 30, 45, 60, 120)
                    presets.forEach { mins ->
                        FilterChip(
                            selected = duration == mins,
                            onClick = { duration = mins },
                            label = { Text(formatDurationShort(mins)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Apps (${selectedApps.size} of ${allUniqueApps.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (allUniqueApps.isEmpty()) {
                    Text(
                        text = "No apps in your profiles yet. Create a profile to pick apps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allUniqueApps.take(8).forEach { pkg ->
                            val isSelected = selectedApps.contains(pkg)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedApps = if (isSelected) selectedApps - pkg else selectedApps + pkg
                                },
                                label = { Text(pkg.substringAfterLast('.')) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(duration, selectedApps) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Start ${formatDurationShort(duration)} session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

private fun formatDurationShort(minutes: Int): String =
    if (minutes >= 60 && minutes % 60 == 0) "${minutes / 60}h" else "${minutes}m"

private fun formatTimer(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private fun formatCountdown(ms: Long): String {
    val totalMinutes = ms / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "%dh %02dm".format(hours, minutes) else "%dm".format(minutes)
}

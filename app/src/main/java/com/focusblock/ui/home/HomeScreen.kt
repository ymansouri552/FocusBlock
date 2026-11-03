package com.focusblock.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.domain.model.ActiveProfileInfo
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.domain.usecase.SystemPermissionsState
import com.focusblock.ui.components.HoldButton
import com.focusblock.ui.components.LedgerRow
import com.focusblock.ui.components.SectionHeader
import com.focusblock.ui.components.SevenDayBarChart
import com.focusblock.ui.components.StampLabel
import com.focusblock.ui.components.StatTile
import com.focusblock.ui.components.StatusRing
import com.focusblock.ui.components.StreakGrid
import com.focusblock.ui.theme.CountdownNumericStyle
import com.focusblock.ui.theme.HeroNumericStyle
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.StampTextStyle
import com.focusblock.ui.theme.tabular
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val palette = LocalPledgePalette.current
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val blockedPackages by viewModel.blockedPackages.collectAsStateWithLifecycle()
    val strictModeState by viewModel.strictModeState.collectAsStateWithLifecycle()
    val permissionsState by viewModel.permissionsState.collectAsStateWithLifecycle()
    val focusStats by viewModel.focusStats.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var now by remember { mutableStateOf(Instant.now()) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FOCUSBLOCK",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.0.sp,
                            color = palette.ink
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LEDGER",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.inkSecondary,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.settings_title),
                            tint = palette.ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = palette.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HERO STATUS OBJECT
            item {
                HomeStatusHero(
                    activeProfiles = activeProfiles,
                    strictState = strictModeState,
                    now = now,
                    blockedCount = blockedPackages.size,
                    onDisarmStrict = { viewModel.disarmStrictMode() }
                )
            }

            // PERMISSIONS NOTICE (ONLY WHEN BROKEN)
            if (permissionsState.hasAnyWarning) {
                item {
                    HomePermissionsNotice(
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

            // STATS STRIP: Focused hours, streak, blocked attempts
            item {
                StatsStrip(
                    focusStats = focusStats
                )
            }

            // QUICK TIMERS BAR (PRESS & HOLD SEGMENTS)
            if (profiles.isNotEmpty()) {
                item {
                    Column {
                        SectionHeader(
                            title = "QUICK TIMERS",
                            badgeText = "HOLD TO START"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        QuickHoldTimerRow(
                            onSelectDuration = { duration ->
                                preselectedQuickDuration = duration
                                showQuickBlockDialog = true
                            }
                        )
                    }
                }
            }

            // STRICT MODE PLEDGE ENTRY (WHEN NOT ARMED)
            if (!strictModeState.isEffectivelyActive(now) && !strictModeState.isArmed) {
                item {
                    StrictPledgePromoCard(
                        onSignPledge = { showStrictTermsDialog = true }
                    )
                }
            }

            // PROFILES SECTION HEADER
            item {
                SectionHeader(
                    title = "CONTRACTS & PROFILES",
                    badgeText = "${profiles.size}",
                    actionText = "+ NEW",
                    onAction = onAddProfile
                )
            }

            // PROFILES LIST (LEDGER ROWS)
            if (profiles.isEmpty()) {
                item {
                    EmptyProfilesLedger(onCreateProfile = onAddProfile)
                }
            } else {
                itemsIndexed(profiles, key = { _, profile -> profile.id }) { index, profile ->
                    val isActiveNow = ScheduleEngine.isActive(profile, now)
                    val isStrictActive = StrictModeRules.isStrictActive(profile, now)

                    LedgerRow(
                        title = profile.name,
                        schedule = profile.schedules.firstOrNull(),
                        appCount = profile.blockedApps.size,
                        isStrictLocked = isStrictActive,
                        isActiveNow = isActiveNow,
                        enabled = profile.enabled,
                        showDivider = index < profiles.lastIndex,
                        onEdit = { onEditProfile(profile.id) },
                        onToggle = { enabled -> viewModel.toggleProfile(profile.id, enabled) }
                    )
                }
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

// ─────────────────────────────────────────────────────────────────────────────
// HERO OBJECT: Status Ring + Large Tabular Countdown + Pledge Label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeStatusHero(
    activeProfiles: List<ActiveProfileInfo>,
    strictState: StrictModeState,
    now: Instant,
    blockedCount: Int,
    onDisarmStrict: () -> Unit
) {
    val palette = LocalPledgePalette.current
    val isStrictActive = strictState.isEffectivelyActive(now)
    val isAnyProfileActive = activeProfiles.isNotEmpty()
    val isSealed = isStrictActive || isAnyProfileActive

    val title: String
    val subtitle: String
    val countdownText: String?

    if (isStrictActive) {
        val remainingMs = strictState.remainingMs(now)
        title = "STRICT MODE ACTIVE"
        subtitle = "${strictState.termType.label.uppercase()} PLEDGE · $blockedCount APPS LOCKED"
        countdownText = remainingMs?.let { formatHeroTimer(it) } ?: "ONGOING"
    } else if (isAnyProfileActive) {
        val nextEnding = activeProfiles.minByOrNull { it.endInstant }
        val remainingMs = nextEnding?.let { Duration.between(now, it.endInstant).toMillis() } ?: 0L
        val profileNames = activeProfiles.joinToString(", ") { it.profile.name }
        title = profileNames.uppercase()
        subtitle = "$blockedCount APPS BLOCKED"
        countdownText = formatHeroTimer(remainingMs)
    } else {
        title = "ALL APPS ACCESSIBLE"
        subtitle = "SELECT A QUICK TIMER OR PROFILE"
        countdownText = null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vault Status Ring
        StatusRing(
            isSealed = isSealed,
            title = title,
            subtitle = subtitle,
            countdownText = countdownText,
            isStrict = isStrictActive,
            size = 230.dp
        )

        // Hero pledge badge under ring
        if (isStrictActive) {
            Spacer(modifier = Modifier.height(14.dp))
            StampLabel(
                text = "STRICT MODE · ${strictState.termType.label.uppercase()} PLEDGE",
                rotation = 0f,
                isSealed = true
            )
        } else if (strictState.isArmed && !isStrictActive) {
            // Term expired
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDisarmStrict,
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.onAccent
                )
            ) {
                Text("DISARM EXPIRED PLEDGE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATS STRIP: 7-day focus bar chart, streak grid, blocked resisted
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatsStrip(
    focusStats: FocusStats,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "LEDGER METRICS"
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tile 1: Focused Hours This Week
            StatTile(
                title = "FOCUSED",
                value = "${String.format("%.1f", focusStats.focusedHoursThisWeek)}h",
                modifier = Modifier.weight(1f)
            ) {
                SevenDayBarChart(
                    dailyHours = focusStats.dailyHours,
                    height = 24.dp
                )
            }

            // Tile 2: Current Streak
            StatTile(
                title = "STREAK",
                value = "${focusStats.streakDays}d",
                modifier = Modifier.weight(1f)
            ) {
                StreakGrid(
                    streakDays = focusStats.streakDays,
                    totalSlots = 14
                )
            }

            // Tile 3: Resisted Attempts
            StatTile(
                title = "RESISTED",
                value = "${focusStats.blockedResisted}",
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "BLOCKS SHIELDED",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = LocalPledgePalette.current.inkSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUICK TIMERS: Press-and-hold square segments
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickHoldTimerRow(
    onSelectDuration: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(15, 30, 45, 60, 120)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        presets.forEach { minutes ->
            QuickTimerSegment(
                minutes = minutes,
                onConfirmed = { onSelectDuration(minutes) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickTimerSegment(
    minutes: Int,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var holdJob by remember { mutableStateOf<Job?>(null) }
    val fillProgress = remember { Animatable(0f) }

    val label = if (minutes >= 60 && minutes % 60 == 0) "${minutes / 60}H" else "${minutes}M"

    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(palette.surface)
            .border(1.5.dp, if (isPressed) palette.border else palette.hairline, RoundedCornerShape(2.dp))
            .pointerInput(minutes) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                        holdJob = scope.launch {
                            fillProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(800, easing = FastOutSlowInEasing)
                            )
                            if (fillProgress.value >= 0.99f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConfirmed()
                                delay(100)
                                fillProgress.snapTo(0f)
                            }
                        }

                        val released = tryAwaitRelease()
                        isPressed = false
                        holdJob?.cancel()
                        scope.launch {
                            fillProgress.animateTo(0f, tween(150))
                        }
                    }
                )
            }
    ) {
        // Fill progress wash
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fillProgress.value)
                .background(palette.accent)
        )

        // Center duration text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val textColor = if (fillProgress.value > 0.5f) palette.onAccent else palette.ink
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.tabular(),
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STRICT PLEDGE PROMO CARD (Paper contract / ledger sign-up style)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StrictPledgePromoCard(
    onSignPledge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, palette.border), RoundedCornerShape(3.dp)),
        color = palette.surface,
        shape = RoundedCornerShape(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "THE PLEDGE · STRICT MODE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.stamp,
                    letterSpacing = 1.2.sp
                )
                StampLabel(text = "UNLOCKED", rotation = 0f)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign an unalterable commitment term. Prevents editing profiles, stopping timers, or disarming protection until your commitment date.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.inkSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSignPledge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.border,
                    contentColor = palette.surface
                )
            ) {
                Text(
                    text = "SIGN A PLEDGE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PERMISSIONS NOTICE: Flat hairline row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomePermissionsNotice(
    permissionsState: SystemPermissionsState,
    onFix: () -> Unit
) {
    val palette = LocalPledgePalette.current
    val message = when {
        !permissionsState.isAccessibilityGranted -> "ACCESSIBILITY DISABLED · TAP TO RESTORE"
        !permissionsState.isDeviceAdminGranted -> "DEVICE ADMIN INACTIVE · TAP TO ACTIVATE"
        else -> "BATTERY OPTIMIZATION ACTIVE · TAP TO EXEMPT"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFix)
            .border(1.dp, palette.stamp, RoundedCornerShape(2.dp)),
        color = palette.stampSurface,
        shape = RoundedCornerShape(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = palette.stamp,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = palette.stamp,
                modifier = Modifier.weight(1f),
                letterSpacing = 0.8.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EMPTY PROFILES STATE: Clean ledger row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyProfilesLedger(onCreateProfile: () -> Unit) {
    val palette = LocalPledgePalette.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, palette.hairline), RoundedCornerShape(3.dp)),
        color = palette.surface,
        shape = RoundedCornerShape(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NO ACTIVE CONTRACTS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.ink,
                letterSpacing = 1.0.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create your first blocking schedule or quota rule to begin.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.inkSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.onAccent
                )
            ) {
                Text(
                    text = "DRAFT FIRST PROFILE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUICK BLOCK MODAL
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickBlockModal(
    initialDuration: Int,
    availableProfiles: List<Profile>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Set<String>) -> Unit
) {
    val palette = LocalPledgePalette.current
    var duration by remember { mutableIntStateOf(initialDuration) }
    val allUniqueApps = remember(availableProfiles) {
        availableProfiles.flatMap { it.blockedApps }.toSet()
    }
    var selectedApps by remember { mutableStateOf(allUniqueApps) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "QUICK BLOCK SESSION",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp,
                color = palette.ink
            )
        },
        text = {
            Column {
                Text(
                    text = "DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(15, 30, 45, 60, 120)
                    presets.forEach { mins ->
                        val isSelected = duration == mins
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .border(1.dp, if (isSelected) palette.accent else palette.hairline, RoundedCornerShape(2.dp))
                                .clickable { duration = mins },
                            color = if (isSelected) palette.accent else palette.surface
                        ) {
                            Text(
                                text = if (mins >= 60) "${mins / 60}H" else "${mins}M",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) palette.onAccent else palette.ink,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "APPS (${selectedApps.size} OF ${allUniqueApps.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (allUniqueApps.isEmpty()) {
                    Text(
                        text = "No apps in your profiles yet. Create a profile to pick apps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allUniqueApps.take(8).forEach { pkg ->
                            val isSelected = selectedApps.contains(pkg)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .border(1.dp, if (isSelected) palette.border else palette.hairline, RoundedCornerShape(2.dp))
                                    .clickable {
                                        selectedApps = if (isSelected) selectedApps - pkg else selectedApps + pkg
                                    },
                                color = if (isSelected) palette.hairline else palette.surface
                            ) {
                                Text(
                                    text = pkg.substringAfterLast('.'),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.ink,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(duration, selectedApps) },
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.onAccent
                )
            ) {
                Text(
                    text = "START ${if (duration >= 60) "${duration / 60}H" else "${duration}M"} SESSION",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = palette.inkSecondary, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = palette.surface,
        shape = RoundedCornerShape(4.dp)
    )
}

private fun formatHeroTimer(ms: Long): String {
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

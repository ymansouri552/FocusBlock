package com.focusblock.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.domain.model.ActiveProfileInfo
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.ui.profile.formatScheduleSummary
import com.focusblock.ui.theme.LocalCustomColors
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
    val customColors = LocalCustomColors.current

    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = Instant.now()
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
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProfile,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(id = R.string.home_add_profile), fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Hero Focus Dashboard ─────────────────────────────────────────
            item {
                FocusHeroCard(
                    activeProfiles = activeProfiles,
                    now = now,
                    blockedPackagesCount = blockedPackages.size,
                    onStartQuickBlock = {
                        preselectedQuickDuration = 30
                        showQuickBlockDialog = true
                    }
                )
            }

            // ── Independent Strict Mode Card ─────────────────────────────────
            item {
                IndependentStrictModeCard(
                    strictState = strictModeState,
                    now = now,
                    onActivate = { showStrictTermsDialog = true },
                    onDisarm = { viewModel.disarmStrictMode() }
                )
            }

            // ── Quick Block Presets Bar ──────────────────────────────────────
            item {
                QuickBlockPresetsBar(
                    onSelectDuration = { duration ->
                        preselectedQuickDuration = duration
                        showQuickBlockDialog = true
                    }
                )
            }

            // ── Profiles Section Header ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.home_profiles).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "${profiles.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // ── Profiles List / Empty State ─────────────────────────────────
            if (profiles.isEmpty()) {
                item {
                    EmptyProfilesCard(onCreateProfile = onAddProfile)
                }
            } else {
                items(profiles, key = { it.id }) { profile ->
                    val isActiveNow = ScheduleEngine.isActive(profile, now)
                    val isStrictActive = StrictModeRules.isStrictActive(profile, now)
                    val activeInfo = ScheduleEngine.activeProfile(profile, now)
                    val remainingMs = activeInfo?.let { Duration.between(now, it.endInstant).toMillis() }

                    ProfileCard(
                        profile = profile,
                        isActiveNow = isActiveNow,
                        isStrictActive = isStrictActive,
                        remainingMs = remainingMs,
                        onEdit = { onEditProfile(profile.id) },
                        onToggle = { enabled -> viewModel.toggleProfile(profile.id, enabled) }
                    )
                }
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
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

// ── Independent Strict Mode Card ─────────────────────────────────────────────

@Composable
private fun IndependentStrictModeCard(
    strictState: StrictModeState,
    now: Instant,
    onActivate: () -> Unit,
    onDisarm: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val isArmed = strictState.isEffectivelyActive(now)
    val remainingMs = strictState.remainingMs(now)

    if (isArmed) {
        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF381A00),
                Color(0xFF261200),
                Color(0xFF140900)
            )
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, customColors.strictAmber.copy(alpha = 0.8f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = customColors.strictContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = customColors.strictAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Strict Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Term: ${strictState.termType.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = customColors.strictAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = customColors.strictContainer,
                            border = BorderStroke(1.dp, customColors.strictAmber.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = customColors.strictAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ARMED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = customColors.strictOnContainer,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (remainingMs != null) {
                        Text(
                            text = formatTimer(remainingMs),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = customColors.strictAmber
                        )
                    } else {
                        Text(
                            text = "ONGOING",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = customColors.strictAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Protection is active 24/7 across all profiles. Turning off profiles, deletion, and uninstallation are locked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    if (remainingMs != null && remainingMs <= 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onDisarm,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = customColors.activeGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Term Complete · Disarm Strict Mode", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // Disarmed State Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, customColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Strict Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Independent Protection",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "DISARMED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Lock all profiles and prevent tampering across all schedules with custom commitment terms (4h, 24h, 7d, etc.).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onActivate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.strictContainer,
                        contentColor = customColors.strictAmber
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = customColors.strictAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Activate Strict Mode",
                        fontWeight = FontWeight.Bold,
                        color = customColors.strictAmber
                    )
                }
            }
        }
    }
}

// ── Hero Focus Dashboard Card ────────────────────────────────────────────────

@Composable
private fun FocusHeroCard(
    activeProfiles: List<ActiveProfileInfo>,
    now: Instant,
    blockedPackagesCount: Int,
    onStartQuickBlock: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val hasActive = activeProfiles.isNotEmpty()
    val isAnyStrict = StrictModeRules.independentState.isEffectivelyActive(now) || activeProfiles.any { it.profile.strict }

    val nextEnding = activeProfiles.minByOrNull { it.endInstant }
    val remainingMs = nextEnding?.let { Duration.between(now, it.endInstant).toMillis() } ?: 0L

    val gradientBrush = if (hasActive) {
        if (isAnyStrict) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF381A00),
                    Color(0xFF1E1405),
                    Color(0xFF140D00)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E1B4B),
                    Color(0xFF0F172A),
                    Color(0xFF0A0F1D)
                )
            )
        }
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceContainer,
                MaterialTheme.colorScheme.surfaceContainerLow
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.dp,
            if (hasActive) {
                if (isAnyStrict) customColors.strictAmber.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
                customColors.cardBorder
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            Column {
                if (hasActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAnyStrict) customColors.strictContainer else customColors.activeContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAnyStrict) Icons.Default.Security else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isAnyStrict) customColors.strictAmber else customColors.activeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAnyStrict) "STRICT FOCUS ACTIVE" else "SESSION ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnyStrict) customColors.strictOnContainer else customColors.activeOnContainer
                                )
                            }
                        }

                        Text(
                            text = "$blockedPackagesCount apps blocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = formatTimer(remainingMs),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Active Profiles: " + activeProfiles.joinToString(", ") { it.profile.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.home_session_idle),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ready to focus? Start a session or enable a schedule.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartQuickBlock,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Quick Block", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Quick Block Presets Bar ──────────────────────────────────────────────────

@Composable
private fun QuickBlockPresetsBar(
    onSelectDuration: (Int) -> Unit
) {
    Column {
        Text(
            text = "⚡ QUICK TIMERS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            val presets = listOf(15, 30, 45, 60, 120)
            items(presets) { minutes ->
                OutlinedCard(
                    onClick = { onSelectDuration(minutes) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (minutes >= 60) "${minutes / 60}h" else "${minutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ── Profile Card ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    profile: Profile,
    isActiveNow: Boolean,
    isStrictActive: Boolean,
    remainingMs: Long?,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val customColors = LocalCustomColors.current

    val borderColor by animateColorAsState(
        targetValue = when {
            isStrictActive -> customColors.strictAmber
            isActiveNow -> customColors.activeGreen
            profile.enabled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else -> customColors.cardBorder
        },
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActiveNow) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Name & Switch / Locked Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name.ifBlank { "Untitled Profile" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Schedule description or remaining time
                    if (isActiveNow && remainingMs != null && remainingMs > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Active · ${formatCountdown(remainingMs)} remaining",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isStrictActive) customColors.strictAmber else customColors.activeGreen
                        )
                    } else if (profile.schedules.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatScheduleSummary(profile.schedules.first()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Control: In Strict Mode while active, show dedicated Lock Pill instead of switch!
                if (isStrictActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = customColors.strictContainer,
                        border = BorderStroke(1.dp, customColors.strictAmber.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = customColors.strictAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = customColors.strictOnContainer
                            )
                        }
                    }
                } else {
                    Switch(
                        checked = profile.enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = if (profile.strict) customColors.strictAmber else MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Blocked Apps Chips Preview
            if (profile.blockedApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "${profile.blockedApps.size} apps",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profile.blockedApps.take(4).joinToString(", ") { it.substringAfterLast('.') },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    if (profile.blockedApps.size > 4) {
                        Text(
                            text = " +${profile.blockedApps.size - 4}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyProfilesCard(onCreateProfile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, LocalCustomColors.current.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.home_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.home_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCreateProfile,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.home_empty_action), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Quick Block Modal ────────────────────────────────────────────────────────

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Block", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Select duration:",
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
                            label = { Text("${mins}m") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Apps to block (${selectedApps.size} selected):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (allUniqueApps.isEmpty()) {
                    Text(
                        text = "No apps in your profiles yet. Create a profile to pick apps or edit in profile settings.",
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
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start Block (${duration}m)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

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

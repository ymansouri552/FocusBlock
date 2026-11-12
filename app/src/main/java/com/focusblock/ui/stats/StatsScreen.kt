package com.focusblock.ui.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.domain.model.AppSortOrder
import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.StatsPeriod
import com.focusblock.ui.profile.AppIconCache
import com.focusblock.ui.stats.components.AppUsageRow
import com.focusblock.ui.stats.components.DailyHourlyChart
import com.focusblock.ui.stats.components.ScreenTimeHeroCard
import com.focusblock.ui.stats.components.WeeklyInteractiveChart
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.tabular
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onOpenSettings: () -> Unit,
    onAddProfileForApp: (String) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val palette = LocalPledgePalette.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                            text = "USAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accent,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.stats_perm_refresh),
                            tint = palette.ink
                        )
                    }
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
        bottomBar = bottomBar,
        containerColor = palette.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. PERMISSION BANNER (IF NOT GRANTED)
            if (!uiState.hasPermission) {
                item {
                    PermissionNoticeCard(
                        onGrant = { viewModel.launchUsageSettings(context) }
                    )
                }
            }

            // 2. TIMEFRAME SELECTOR & DATE NAVIGATOR
            item {
                TimeframeControlCard(
                    period = uiState.period,
                    selectedDate = uiState.selectedDate,
                    canGoForward = uiState.canGoForward,
                    isCurrentPeriod = uiState.isCurrentPeriod,
                    onSelectPeriod = { viewModel.setPeriod(it) },
                    onPrevious = { viewModel.previousPeriod() },
                    onNext = { viewModel.nextPeriod() },
                    onResetCurrent = { viewModel.goToCurrent() }
                )
            }

            // 3. SCREEN TIME HERO METRIC
            item {
                val periodLabel = if (uiState.period == StatsPeriod.DAILY) {
                    val zone = ZoneId.systemDefault()
                    val today = LocalDate.now(zone)
                    when (uiState.selectedDate) {
                        today -> "Today Screen Time"
                        today.minusDays(1) -> "Yesterday Screen Time"
                        else -> "Screen Time on ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
                    }
                } else {
                    "Total Screen Time This Week"
                }

                val secondaryLabel = if (uiState.period == StatsPeriod.WEEKLY) "DAILY AVG" else "PEAK TIME"
                val secondaryValue = if (uiState.period == StatsPeriod.WEEKLY) {
                    uiState.overview.formattedDailyAverage
                } else {
                    uiState.overview.peakTimeLabel
                }

                ScreenTimeHeroCard(
                    totalDurationFormatted = uiState.overview.formattedTotalTime,
                    periodLabel = periodLabel,
                    appsCount = uiState.overview.appsUsedCount,
                    launchesCount = uiState.overview.totalLaunches,
                    secondaryMetricLabel = secondaryLabel,
                    secondaryMetricValue = secondaryValue
                )
            }

            // 4. INTERACTIVE CHART
            item {
                if (uiState.period == StatsPeriod.WEEKLY) {
                    WeeklyInteractiveChart(
                        buckets = uiState.overview.weeklyBuckets,
                        onSelectDay = { date -> viewModel.selectDay(date) }
                    )
                } else {
                    DailyHourlyChart(
                        buckets = uiState.overview.hourlyBuckets
                    )
                }
            }

            // 5. APPLICATIONS HEADER & CONTROLS
            item {
                ApplicationsSectionHeader(
                    appsCount = uiState.overview.appUsageList.size,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    sortOrder = uiState.sortOrder,
                    onSelectSortOrder = { viewModel.setSortOrder(it) }
                )
            }

            // 6. APPS LIST OR EMPTY STATE
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = palette.accent)
                    }
                }
            } else if (uiState.overview.appUsageList.isEmpty()) {
                item {
                    EmptyStatsCard(
                        hasQuery = uiState.searchQuery.isNotBlank(),
                        query = uiState.searchQuery
                    )
                }
            } else {
                items(
                    items = uiState.overview.appUsageList,
                    key = { it.packageName }
                ) { appItem ->
                    AppUsageRow(
                        item = appItem,
                        onClick = { viewModel.selectAppForDetails(appItem) }
                    )
                }
            }
        }
    }

    // APP DETAILS DIALOG
    uiState.selectedAppForDetails?.let { app ->
        AppDetailsDialog(
            app = app,
            onDismiss = { viewModel.selectAppForDetails(null) },
            onBlockApp = {
                viewModel.selectAppForDetails(null)
                onAddProfileForApp(app.packageName)
            }
        )
    }
}

/**
 * Notice card when Usage Access is not granted.
 */
@Composable
private fun PermissionNoticeCard(
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.stampSurface)
            .border(BorderStroke(1.dp, palette.stamp.copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = palette.stamp,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.stats_perm_required_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.ink
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.stats_perm_required_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.inkSecondary,
            textAlign = TextAlign.Left
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.stamp,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.stats_perm_grant_button),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.stats_privacy_badge),
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary
            )
        }
    }
}

/**
 * Timeframe Segment Toggle (Daily / Weekly) and Date Navigation.
 */
@Composable
private fun TimeframeControlCard(
    period: StatsPeriod,
    selectedDate: LocalDate,
    canGoForward: Boolean,
    isCurrentPeriod: Boolean,
    onSelectPeriod: (StatsPeriod) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onResetCurrent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface)
            .border(BorderStroke(1.dp, palette.hairline), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        // Toggle row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(palette.background)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PeriodToggleButton(
                title = stringResource(R.string.stats_daily),
                isSelected = period == StatsPeriod.DAILY,
                onClick = { onSelectPeriod(StatsPeriod.DAILY) },
                modifier = Modifier.weight(1f)
            )
            PeriodToggleButton(
                title = stringResource(R.string.stats_weekly),
                isSelected = period == StatsPeriod.WEEKLY,
                onClick = { onSelectPeriod(StatsPeriod.WEEKLY) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date navigation row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous period",
                    tint = palette.ink
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                val dateText = when (period) {
                    StatsPeriod.DAILY -> {
                        when (selectedDate) {
                            today -> "Today (${selectedDate.format(DateTimeFormatter.ofPattern("d MMM"))})"
                            today.minusDays(1) -> "Yesterday (${selectedDate.format(DateTimeFormatter.ofPattern("d MMM"))})"
                            else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy"))
                        }
                    }
                    StatsPeriod.WEEKLY -> {
                        val monday = selectedDate.with(DayOfWeek.MONDAY)
                        val sunday = monday.plusDays(6)
                        "${monday.format(DateTimeFormatter.ofPattern("d MMM"))} to ${sunday.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}"
                    }
                }

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                    textAlign = TextAlign.Center
                )

                if (!isCurrentPeriod) {
                    Text(
                        text = if (period == StatsPeriod.DAILY) "Jump to Today" else "Jump to This Week",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onResetCurrent() }
                            .padding(top = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = onNext,
                enabled = canGoForward,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next period",
                    tint = if (canGoForward) palette.ink else palette.inkSecondary.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun PeriodToggleButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) palette.accent else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 1.0.sp,
            color = if (isSelected) Color.White else palette.inkSecondary
        )
    }
}

/**
 * Section Header for Applications with search and sorting chips.
 */
@Composable
private fun ApplicationsSectionHeader(
    appsCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOrder: AppSortOrder,
    onSelectSortOrder: (AppSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "APPLICATIONS USED ($appsCount)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = palette.inkSecondary,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.stats_search_hint),
                    color = palette.inkSecondary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = palette.inkSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = palette.inkSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface,
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.hairline,
                focusedTextColor = palette.ink,
                unfocusedTextColor = palette.ink
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Sort Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortFilterChip(
                label = stringResource(R.string.stats_sort_time),
                selected = sortOrder == AppSortOrder.MOST_USED,
                onClick = { onSelectSortOrder(AppSortOrder.MOST_USED) }
            )
            SortFilterChip(
                label = stringResource(R.string.stats_sort_opens),
                selected = sortOrder == AppSortOrder.MOST_LAUNCHED,
                onClick = { onSelectSortOrder(AppSortOrder.MOST_LAUNCHED) }
            )
            SortFilterChip(
                label = stringResource(R.string.stats_sort_name),
                selected = sortOrder == AppSortOrder.NAME,
                onClick = { onSelectSortOrder(AppSortOrder.NAME) }
            )
        }
    }
}

@Composable
private fun SortFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalPledgePalette.current

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = palette.accent.copy(alpha = 0.18f),
            selectedLabelColor = palette.accent,
            containerColor = palette.surface,
            labelColor = palette.inkSecondary
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) palette.accent else palette.hairline
        )
    )
}

/**
 * Empty state card.
 */
@Composable
private fun EmptyStatsCard(
    hasQuery: Boolean,
    query: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface)
            .border(BorderStroke(1.dp, palette.hairline), RoundedCornerShape(14.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            tint = palette.inkSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasQuery) "No applications found matching \"$query\"" else stringResource(R.string.stats_no_usage),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.inkSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Detailed bottom sheet / dialog for an app's wellbeing metrics.
 */
@Composable
private fun AppDetailsDialog(
    app: AppUsageItem,
    onDismiss: () -> Unit,
    onBlockApp: () -> Unit
) {
    val palette = LocalPledgePalette.current
    val context = LocalContext.current
    var iconBitmap by remember(app.packageName) {
        mutableStateOf(AppIconCache.get(app.packageName))
    }

    LaunchedEffect(app.packageName) {
        if (iconBitmap == null) {
            iconBitmap = AppIconCache.loadIcon(context, app.packageName)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surfaceRaised,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap!!,
                            contentDescription = app.appName,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = palette.inkSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = palette.inkSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(color = palette.hairline)

                // Time spent row
                DetailLedgerRow(
                    label = "TIME IN FOREGROUND",
                    value = app.formattedDuration
                )

                // Launch count row
                DetailLedgerRow(
                    label = "LAUNCHES",
                    value = pluralStringResource(
                        R.plurals.stats_open_count,
                        app.launchCount,
                        app.launchCount
                    )
                )

                // Last active time
                if (app.lastTimeUsedMs > 0) {
                    val lastUsedInstant = Instant.ofEpochMilli(app.lastTimeUsedMs)
                    val localDateTime = lastUsedInstant.atZone(ZoneId.systemDefault()).toLocalDateTime()
                    val timeFormatted = localDateTime.format(DateTimeFormatter.ofPattern("h:mm a, MMM d"))
                    DetailLedgerRow(
                        label = "LAST OPENED",
                        value = timeFormatted
                    )
                }

                // FocusBlock Profile Status
                DetailLedgerRow(
                    label = "FOCUSBLOCK STATUS",
                    value = if (app.isBlocked) "Blocked in ${app.profileNames.joinToString(", ")}" else "Not restricted"
                )

                HorizontalDivider(color = palette.hairline)
            }
        },
        confirmButton = {
            Button(
                onClick = onBlockApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.stats_app_block_action),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    color = palette.inkSecondary
                )
            }
        }
    )
}

@Composable
private fun DetailLedgerRow(
    label: String,
    value: String
) {
    val palette = LocalPledgePalette.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = palette.inkSecondary,
            letterSpacing = 1.0.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.tabular(),
            fontWeight = FontWeight.SemiBold,
            color = palette.ink
        )
    }
}

package com.focusblock.ui.profile

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.domain.model.InstalledApp
import com.focusblock.domain.model.LimitScope
import com.focusblock.domain.model.ProfilePreset
import com.focusblock.domain.model.ProfilePresets
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.domain.model.ScheduleType
import com.focusblock.ui.components.DayBar24h
import com.focusblock.ui.components.DayTimeline24hEditor
import com.focusblock.ui.components.SectionHeader
import com.focusblock.ui.components.StampLabel
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.StampTextStyle
import com.focusblock.ui.theme.tabular
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileWizardScreen(
    viewModel: ProfileWizardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalPledgePalette.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEW CONTRACT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = palette.ink
                        )
                        Text(
                            text = "STEP ${state.step} / 5",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.accent,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.step > 1) {
                                viewModel.prevStep()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (state.step > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                            contentDescription = if (state.step > 1) stringResource(id = R.string.wizard_back) else stringResource(id = R.string.close),
                            tint = palette.ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = palette.surface,
                border = BorderStroke(1.dp, palette.hairline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.step > 1) {
                        Surface(
                            modifier = Modifier
                                .weight(0.4f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .border(1.5.dp, palette.border, RoundedCornerShape(2.dp))
                                .clickable { viewModel.prevStep() },
                            color = palette.surface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "BACK",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.ink,
                                    letterSpacing = 1.0.sp
                                )
                            }
                        }
                    }

                    // Primary Full-width, square-edged button
                    Button(
                        onClick = {
                            if (state.step == 5) {
                                viewModel.save(onSuccess = onBack)
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(2.dp),
                        enabled = !state.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent,
                            contentColor = palette.onAccent,
                            disabledContainerColor = palette.hairline
                        )
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = palette.onAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            val buttonText = when (state.step) {
                                1 -> "NEXT: SELECT APPS"
                                2 -> "NEXT: CHOOSE MODE (${state.selectedPackages.size})"
                                3 -> if (state.schedule.mode == ScheduleMode.BLOCK) "NEXT: SET BLACKOUT SCHEDULE" else "NEXT: SET ALLOWANCE & TIMEFRAME"
                                4 -> "NEXT: REVIEW & PLEDGE"
                                else -> "SEAL & SAVE CONTRACT"
                            }
                            Text(
                                text = buttonText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = palette.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Thin progress line across the top
            ThinStepProgressLine(
                currentStep = state.step,
                totalSteps = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // Error notice
            state.error?.let { err ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = palette.stampSurface,
                    border = BorderStroke(1.dp, palette.stamp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = palette.stamp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.stamp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "WizardStepTransition",
                modifier = Modifier.weight(1f)
            ) { targetStep ->
                when (targetStep) {
                    1 -> WizardStep1PresetsPager(
                        selectedPresetId = state.selectedPresetId,
                        profileName = state.profileName,
                        onSelectPreset = { viewModel.selectPreset(it) },
                        onNameChange = { viewModel.setProfileName(it) }
                    )
                    2 -> WizardStep2AppPickerLedger(
                        installedApps = state.installedApps,
                        selectedPackages = state.selectedPackages,
                        searchQuery = state.searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onTogglePackage = { viewModel.togglePackage(it) },
                        onSelectAll = { viewModel.selectAllFilteredApps(it) },
                        onClearAll = { viewModel.clearAllSelectedPackages() }
                    )
                    3 -> WizardStep3ModeSelection(
                        selectedMode = state.schedule.mode,
                        onSelectMode = { viewModel.setScheduleMode(it) }
                    )
                    4 -> if (state.schedule.mode == ScheduleMode.BLOCK) {
                        WizardStep4BlackoutSchedule(
                            schedule = state.schedule,
                            appCount = state.selectedPackages.size,
                            onScheduleChange = { viewModel.setSchedule(it) }
                        )
                    } else {
                        WizardStep4UsageAllowance(
                            schedule = state.schedule,
                            appCount = state.selectedPackages.size,
                            onScheduleChange = { viewModel.setSchedule(it) }
                        )
                    }
                    5 -> WizardStep5PledgeReview(
                        profileName = state.profileName,
                        selectedApps = state.installedApps.filter { state.selectedPackages.contains(it.packageName) },
                        schedule = state.schedule,
                        strict = state.strict,
                        onStrictChange = { viewModel.toggleStrict(it) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// THIN PROGRESS LINE (Thin line across top)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ThinStepProgressLine(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current
    val progress = currentStep.toFloat() / totalSteps.toFloat()

    Box(
        modifier = modifier
            .height(2.dp)
            .background(palette.hairline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(palette.accent)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 1: HORIZONTAL SWIPEABLE PAGER (One large preset card per page)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStep1PresetsPager(
    selectedPresetId: String,
    profileName: String,
    onSelectPreset: (ProfilePreset) -> Unit,
    onNameChange: (String) -> Unit
) {
    val palette = LocalPledgePalette.current
    val presets = remember { ProfilePresets.presets }
    val initialPage = remember(selectedPresetId) {
        presets.indexOfFirst { it.id == selectedPresetId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { presets.size })

    // Keep preset selection synchronized with pager swipe
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page in presets.indices) {
                onSelectPreset(presets[page])
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "SELECT TEMPLATE",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Swipeable Pager for Presets
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
        ) { page ->
            val preset = presets[page]
            val isSelected = selectedPresetId == preset.id
            PresetContractCard(
                preset = preset,
                isSelected = isSelected,
                onClick = { onSelectPreset(preset) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pager indicator dots / bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEachIndexed { idx, _ ->
                val active = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (active) 20.dp else 6.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (active) palette.accent else palette.hairline)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Name Input
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "CONTRACT CODENAME",
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = profileName,
                onValueChange = onNameChange,
                placeholder = { Text("e.g. Deep Work, Evening Rest", color = palette.inkSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.border,
                    unfocusedBorderColor = palette.hairline,
                    focusedTextColor = palette.ink,
                    unfocusedTextColor = palette.ink,
                    focusedContainerColor = palette.surface,
                    unfocusedContainerColor = palette.surface
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PresetContractCard(
    preset: ProfilePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalPledgePalette.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .border(
                BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) palette.border else palette.hairline),
                RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        color = palette.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = preset.badge.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary,
                        letterSpacing = 1.0.sp
                    )
                    if (isSelected) {
                        Surface(
                            color = palette.accent,
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text(
                                text = "SELECTED",
                                style = StampTextStyle,
                                color = palette.onAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = preset.title.uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = preset.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.inkSecondary,
                    lineHeight = 20.sp
                )
            }

            // Visual 24-hour bar on the card
            Column {
                Text(
                    text = "SCHEDULE COVERAGE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = palette.inkSecondary,
                    letterSpacing = 1.0.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                DayBar24h(
                    startTime = preset.defaultSchedule.startTime,
                    endTime = preset.defaultSchedule.endTime,
                    isBlockMode = preset.defaultSchedule.mode == ScheduleMode.BLOCK,
                    height = 14.dp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 2: APP PICKER LEDGER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStep2AppPickerLedger(
    installedApps: List<InstalledApp>,
    selectedPackages: Set<String>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onTogglePackage: (String) -> Unit,
    onSelectAll: (List<String>) -> Unit,
    onClearAll: () -> Unit
) {
    val palette = LocalPledgePalette.current

    val filteredApps = remember(installedApps, searchQuery, selectedPackages) {
        val list = if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
        list.sortedWith(
            compareByDescending<InstalledApp> { selectedPackages.contains(it.packageName) }
                .thenBy { it.label.lowercase() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Filter applications by name...", color = palette.inkSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = palette.inkSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = palette.inkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.border,
                unfocusedBorderColor = palette.hairline,
                focusedTextColor = palette.ink,
                unfocusedTextColor = palette.ink,
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Ledger selection count row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${selectedPackages.size} OF ${installedApps.size} SELECTED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = palette.ink,
                letterSpacing = 1.0.sp
            )
            Row {
                TextButton(
                    onClick = { onSelectAll(filteredApps.map { it.packageName }) }
                ) {
                    Text(
                        text = "SELECT ALL",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(
                    onClick = onClearAll
                ) {
                    Text(
                        text = "CLEAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.stamp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(color = palette.hairline, thickness = 1.dp)

        if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (installedApps.isEmpty()) "Scanning installed packages..." else "No matching applications found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.inkSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isSelected = selectedPackages.contains(app.packageName)
                    LedgerAppRow(
                        app = app,
                        isSelected = isSelected,
                        onToggle = { onTogglePackage(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerAppRow(
    app: InstalledApp,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val palette = LocalPledgePalette.current
    val context = LocalContext.current

    val iconBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = AppIconCache.get(app.packageName),
        key1 = app.packageName
    ) {
        if (value == null) {
            value = AppIconCache.loadIcon(context, app.packageName)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        color = if (isSelected) palette.hairline.copy(alpha = 0.5f) else Color.Transparent,
        shape = RoundedCornerShape(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bmp = iconBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = palette.hairline,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = app.label.firstOrNull()?.uppercase() ?: "A",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.ink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.ink
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall.tabular(),
                    color = palette.inkSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = palette.border,
                    checkmarkColor = palette.surface,
                    uncheckedColor = palette.hairline
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REUSABLE LEDGER HELPERS: ACTIVE DAYS, TIME PICKERS & SUMMARY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActiveDaysSelector(
    selectedDays: Set<Int>,
    onDaysChanged: (Set<Int>) -> Unit
) {
    val palette = LocalPledgePalette.current
    val daysList = remember {
        listOf(
            1 to "SUN", 2 to "MON", 3 to "TUE", 4 to "WED", 5 to "THU", 6 to "FRI", 7 to "SAT"
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE DAYS",
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary,
                letterSpacing = 1.2.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onDaysChanged(setOf(1, 2, 3, 4, 5, 6, 7)) }) {
                    Text("ALL", style = MaterialTheme.typography.labelSmall, color = palette.accent, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { onDaysChanged(setOf(2, 3, 4, 5, 6)) }) {
                    Text("WEEKDAYS", style = MaterialTheme.typography.labelSmall, color = palette.ink, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { onDaysChanged(setOf(1, 7)) }) {
                    Text("WEEKENDS", style = MaterialTheme.typography.labelSmall, color = palette.ink, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            daysList.forEach { (dayNum, dayLabel) ->
                val selected = selectedDays.contains(dayNum)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, if (selected) palette.border else palette.hairline, RoundedCornerShape(2.dp))
                        .clickable {
                            val updated = if (selected) selectedDays - dayNum else selectedDays + dayNum
                            onDaysChanged(updated)
                        },
                    color = if (selected) palette.border else palette.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) palette.surface else palette.ink
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickersRow(
    startTime: LocalTime,
    endTime: LocalTime,
    onStartTimeChanged: (LocalTime) -> Unit,
    onEndTimeChanged: (LocalTime) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalPledgePalette.current
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    fun openPicker(initial: LocalTime, onSelected: (LocalTime) -> Unit) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute -> onSelected(LocalTime.of(hourOfDay, minute)) },
            initial.hour,
            initial.minute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, palette.hairline, RoundedCornerShape(2.dp))
                .clickable { openPicker(startTime, onStartTimeChanged) },
            color = palette.surface
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = palette.inkSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START: ${startTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.titleSmall.tabular(),
                    fontWeight = FontWeight.Bold,
                    color = palette.ink
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, palette.hairline, RoundedCornerShape(2.dp))
                .clickable { openPicker(endTime, onEndTimeChanged) },
            color = palette.surface
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = palette.inkSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "END: ${endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.titleSmall.tabular(),
                    fontWeight = FontWeight.Bold,
                    color = palette.ink
                )
            }
        }
    }
}

@Composable
private fun ContractSummaryCard(
    schedule: Schedule,
    appCount: Int
) {
    val palette = LocalPledgePalette.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, palette.hairline), RoundedCornerShape(2.dp)),
        color = palette.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "CONTRACT ENFORCEMENT SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = palette.inkSecondary,
                letterSpacing = 1.0.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ProfilePresets.getExplanation(schedule, appCount),
                style = MaterialTheme.typography.bodySmall,
                color = palette.ink
            )
        }
    }
}

@Composable
private fun ModeFeatureBullet(text: String) {
    val palette = LocalPledgePalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(palette.accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = palette.inkSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 3: DEDICATED RESTRICTION MODE SELECTION (BLACKOUT vs USAGE ALLOWANCE)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStep3ModeSelection(
    selectedMode: ScheduleMode,
    onSelectMode: (ScheduleMode) -> Unit
) {
    val palette = LocalPledgePalette.current
    val isBlackout = selectedMode == ScheduleMode.BLOCK
    val isAllowance = selectedMode == ScheduleMode.USAGE_LIMIT

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "CHOOSE RESTRICTION DISCIPLINE",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "RESTRICTION MODE",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = palette.ink
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select how FocusBlock enforces barriers on your target applications.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.inkSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Option 1: Full Blackout Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .border(
                    BorderStroke(if (isBlackout) 2.dp else 1.dp, if (isBlackout) palette.border else palette.hairline),
                    RoundedCornerShape(3.dp)
                )
                .clickable { onSelectMode(ScheduleMode.BLOCK) },
            color = palette.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FULL BLACKOUT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink,
                        letterSpacing = 0.5.sp
                    )
                    if (isBlackout) {
                        StampLabel(text = "SELECTED", rotation = 0f)
                    } else {
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = palette.hairline.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, palette.hairline)
                        ) {
                            Text(
                                text = "BLOCK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.inkSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Complete obstruction during designated active windows. Once active, applications cannot be opened under any circumstance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.ink
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = palette.hairline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ModeFeatureBullet(text = "Uncompromising block screen overlay on launch")
                    ModeFeatureBullet(text = "Configured by 24-hour schedule or overnight windows")
                    ModeFeatureBullet(text = "Zero screen time allowance or snooze periods")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Option 2: Usage Allowance Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .border(
                    BorderStroke(if (isAllowance) 2.dp else 1.dp, if (isAllowance) palette.border else palette.hairline),
                    RoundedCornerShape(3.dp)
                )
                .clickable { onSelectMode(ScheduleMode.USAGE_LIMIT) },
            color = palette.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "USAGE ALLOWANCE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink,
                        letterSpacing = 0.5.sp
                    )
                    if (isAllowance) {
                        StampLabel(text = "SELECTED", rotation = 0f)
                    } else {
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = palette.hairline.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, palette.hairline)
                        ) {
                            Text(
                                text = "ALLOWANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.inkSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Permit a disciplined quota of screen time. Once exhausted, applications are blocked for the remainder of the timeframe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.ink
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = palette.hairline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ModeFeatureBullet(text = "Flexible timeframe: Daily, Hourly, or Custom window")
                    ModeFeatureBullet(text = "Shared pool across all apps or per-app quotas")
                    ModeFeatureBullet(text = "Live countdown tracks foreground usage")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Direction Notice
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.hairline, RoundedCornerShape(2.dp)),
            color = palette.hairline.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = palette.inkSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBlackout)
                        "Next: You will configure the active days and 24-hour blackout time window."
                    else
                        "Next: You will configure the quota duration, reset timeframe cadence (daily, hourly, or custom window), and sharing scope.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.inkSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 4 (BRANCH A): BLACKOUT SCHEDULE (DAYS + 24H TIMELINE & TIME PICKERS)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStep4BlackoutSchedule(
    schedule: Schedule,
    appCount: Int,
    onScheduleChange: (Schedule) -> Unit
) {
    val palette = LocalPledgePalette.current
    val currentStart = schedule.startTime ?: LocalTime.of(9, 0)
    val currentEnd = schedule.endTime ?: LocalTime.of(17, 0)
    val isOvernight = !currentEnd.isAfter(currentStart)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "STEP 4: BLACKOUT SCHEDULE",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ACTIVE DAYS & TIMELINE",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = palette.ink
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Designate when target applications are locked against access.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.inkSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Active Days
        ActiveDaysSelector(
            selectedDays = schedule.daysOfWeek,
            onDaysChanged = { onScheduleChange(schedule.copy(daysOfWeek = it)) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 24-Hour Drag Timeline
        Text(
            text = "24-HOUR BLOCK TIMELINE (DRAG TO ADJUST)",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        DayTimeline24hEditor(
            startTime = currentStart,
            endTime = currentEnd,
            onRangeChanged = { newStart, newEnd ->
                onScheduleChange(schedule.copy(startTime = newStart, endTime = newEnd))
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Numeric Time Pickers as Fallback / Fine-tune
        Text(
            text = "NUMERIC TIME PICKERS (TAP TO FINE-TUNE)",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.0.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        TimePickersRow(
            startTime = currentStart,
            endTime = currentEnd,
            onStartTimeChanged = { onScheduleChange(schedule.copy(startTime = it)) },
            onEndTimeChanged = { onScheduleChange(schedule.copy(endTime = it)) }
        )

        if (isOvernight) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, palette.hairline, RoundedCornerShape(2.dp)),
                color = palette.hairline.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Overnight schedule: locks start at ${currentStart.format(timeFormatter)} and release the following morning at ${currentEnd.format(timeFormatter)}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.ink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Plain English Contract Enforcement Summary
        ContractSummaryCard(schedule = schedule, appCount = appCount)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 4 (BRANCH B): USAGE ALLOWANCE (QUOTA + TIMEFRAME CADENCE + DAYS + SCOPE)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStep4UsageAllowance(
    schedule: Schedule,
    appCount: Int,
    onScheduleChange: (Schedule) -> Unit
) {
    val palette = LocalPledgePalette.current
    val allowancePresets = remember { listOf(15, 30, 45, 60, 90) }
    val currentStart = schedule.startTime ?: LocalTime.of(9, 0)
    val currentEnd = schedule.endTime ?: LocalTime.of(17, 0)
    val isOvernight = !currentEnd.isAfter(currentStart)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "STEP 4: USAGE ALLOWANCE",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ALLOWANCE & TIMEFRAME",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = palette.ink
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Configure your screen time quota, reset timeframe, and sharing scope.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.inkSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: ALLOWED QUOTA MINUTES
        Text(
            text = "ALLOWED QUOTA DURATION",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            allowancePresets.forEach { mins ->
                val isSelected = schedule.usageLimitMinutes == mins
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, if (isSelected) palette.border else palette.hairline, RoundedCornerShape(2.dp))
                        .clickable { onScheduleChange(schedule.copy(usageLimitMinutes = mins)) },
                    color = if (isSelected) palette.border else palette.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${mins}M",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) palette.surface else palette.ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = if (schedule.usageLimitMinutes > 0) schedule.usageLimitMinutes.toString() else "",
            onValueChange = { input ->
                val mins = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                onScheduleChange(schedule.copy(usageLimitMinutes = mins))
            },
            label = { Text("CUSTOM QUOTA (MINUTES)") },
            placeholder = { Text("e.g. 25") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.border,
                unfocusedBorderColor = palette.hairline,
                focusedTextColor = palette.ink,
                unfocusedTextColor = palette.ink
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: RESET TIMEFRAME CADENCE
        Text(
            text = "RESET TIMEFRAME CADENCE",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val cadenceOptions = listOf(
                Triple(ScheduleType.DAILY, "DAILY", "Resets 00:00"),
                Triple(ScheduleType.HOURLY, "HOURLY", "Resets :00"),
                Triple(ScheduleType.RECURRING, "CUSTOM WINDOW", "Within window")
            )

            cadenceOptions.forEach { (type, label, desc) ->
                val isSelected = schedule.type == type
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) palette.border else palette.hairline),
                            RoundedCornerShape(2.dp)
                        )
                        .clickable {
                            onScheduleChange(
                                schedule.copy(
                                    type = type,
                                    startTime = if (type == ScheduleType.RECURRING) currentStart else schedule.startTime,
                                    endTime = if (type == ScheduleType.RECURRING) currentEnd else schedule.endTime
                                )
                            )
                        },
                    color = if (isSelected) palette.hairline.copy(alpha = 0.3f) else palette.surface
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.ink
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = palette.inkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // If CUSTOM WINDOW (ScheduleType.RECURRING) is chosen, show the 24h timeline and start/end pickers
        if (schedule.type == ScheduleType.RECURRING) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ACTIVE ALLOWANCE TIMELINE (DRAG TO ADJUST)",
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            DayTimeline24hEditor(
                startTime = currentStart,
                endTime = currentEnd,
                onRangeChanged = { newStart, newEnd ->
                    onScheduleChange(schedule.copy(startTime = newStart, endTime = newEnd))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            TimePickersRow(
                startTime = currentStart,
                endTime = currentEnd,
                onStartTimeChanged = { onScheduleChange(schedule.copy(startTime = it)) },
                onEndTimeChanged = { onScheduleChange(schedule.copy(endTime = it)) }
            )

            if (isOvernight) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Overnight window: quota applies from ${currentStart.format(timeFormatter)} to ${currentEnd.format(timeFormatter)} next morning.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: ACTIVE DAYS
        ActiveDaysSelector(
            selectedDays = schedule.daysOfWeek,
            onDaysChanged = { onScheduleChange(schedule.copy(daysOfWeek = it)) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: QUOTA SHARING SCOPE
        Text(
            text = "QUOTA SHARING SCOPE",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Option A: Combined Pool
            val isCombined = schedule.limitScope == LimitScope.COMBINED
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .border(
                        BorderStroke(if (isCombined) 2.dp else 1.dp, if (isCombined) palette.border else palette.hairline),
                        RoundedCornerShape(2.dp)
                    )
                    .clickable { onScheduleChange(schedule.copy(limitScope = LimitScope.COMBINED)) },
                color = if (isCombined) palette.hairline.copy(alpha = 0.3f) else palette.surface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SHARED POOL",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All selected apps draw from one combined ${schedule.usageLimitMinutes}m quota",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )
                }
            }

            // Option B: Per App
            val isPerApp = schedule.limitScope == LimitScope.PER_APP
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .border(
                        BorderStroke(if (isPerApp) 2.dp else 1.dp, if (isPerApp) palette.border else palette.hairline),
                        RoundedCornerShape(2.dp)
                    )
                    .clickable { onScheduleChange(schedule.copy(limitScope = LimitScope.PER_APP)) },
                color = if (isPerApp) palette.hairline.copy(alpha = 0.3f) else palette.surface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "PER-APP",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Each app gets its own separate ${schedule.usageLimitMinutes}m quota",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Contract Plain-English Summary
        ContractSummaryCard(schedule = schedule, appCount = appCount)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 5: REVIEW & PLEDGE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStep5PledgeReview(
    profileName: String,
    selectedApps: List<InstalledApp>,
    schedule: Schedule,
    strict: Boolean,
    onStrictChange: (Boolean) -> Unit
) {
    val palette = LocalPledgePalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "STEP 5: CONTRACT SPECIFICATION",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Document Ledger Sheet Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, palette.border), RoundedCornerShape(3.dp)),
            color = palette.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profileName.ifBlank { "UNTITLED PROFILE" }.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    StampLabel(text = if (strict) "STRICT PLEDGE" else "STANDARD", rotation = 0f)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = palette.hairline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Detail item 0: Restriction Mode
                Text(
                    text = "DISCIPLINE & CADENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                val modeSummary = if (schedule.mode == ScheduleMode.BLOCK) {
                    "FULL BLACKOUT (Complete cessation during active hours)"
                } else {
                    val cadenceStr = when (schedule.type) {
                        ScheduleType.HOURLY -> "Hourly reset"
                        ScheduleType.DAILY -> "Daily reset"
                        ScheduleType.RECURRING -> "Custom active window"
                        else -> "Daily reset"
                    }
                    val scopeStr = if (schedule.limitScope == LimitScope.COMBINED) "Shared pool" else "Per app"
                    "USAGE ALLOWANCE: ${schedule.usageLimitMinutes} min ($cadenceStr · $scopeStr)"
                }
                Text(
                    text = modeSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.ink
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detail item 1: Apps
                Text(
                    text = "TARGET APPLICATIONS (${selectedApps.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedApps.isEmpty()) "No apps selected"
                    else selectedApps.joinToString(", ") { it.label },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = palette.ink
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detail item 2: Schedule bar
                Text(
                    text = "SCHEDULE TIMELINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                DayBar24h(
                    startTime = schedule.startTime,
                    endTime = schedule.endTime,
                    isBlockMode = schedule.mode == ScheduleMode.BLOCK,
                    height = 10.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detail item 3: Summary explanation
                Text(
                    text = "ENFORCEMENT TERMS",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ProfilePresets.getExplanation(schedule, selectedApps.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.ink
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Strict Lock Toggle
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.5.dp, if (strict) palette.stamp else palette.hairline), RoundedCornerShape(3.dp)),
            color = if (strict) palette.stampSurface else palette.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ENFORCE IN STRICT MODE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (strict) palette.stamp else palette.ink,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Locks this profile against modification or deletion while Strict Mode is active on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = strict,
                    onCheckedChange = onStrictChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = palette.surface,
                        checkedTrackColor = palette.stamp,
                        uncheckedThumbColor = palette.inkSecondary,
                        uncheckedTrackColor = palette.surface
                    )
                )
            }
        }
    }
}

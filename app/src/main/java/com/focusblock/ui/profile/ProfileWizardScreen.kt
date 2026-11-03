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
                            text = "STEP ${state.step} / 4",
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
                            if (state.step == 4) {
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
                                2 -> "NEXT: SET SCHEDULE (${state.selectedPackages.size})"
                                3 -> "NEXT: REVIEW & PLEDGE"
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
                totalSteps = 4,
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
                    3 -> WizardStep3ScheduleTimeline(
                        schedule = state.schedule,
                        appCount = state.selectedPackages.size,
                        onScheduleChange = { viewModel.setSchedule(it) }
                    )
                    4 -> WizardStep4PledgeReview(
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
// STEP 3: SCHEDULE EDITING WITH 24-HOUR DRAG TIMELINE & PER-DAY TOGGLES
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WizardStep3ScheduleTimeline(
    schedule: Schedule,
    appCount: Int,
    onScheduleChange: (Schedule) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalPledgePalette.current
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val daysList = remember {
        listOf(
            1 to "SUN", 2 to "MON", 3 to "TUE", 4 to "WED", 5 to "THU", 6 to "FRI", 7 to "SAT"
        )
    }

    val isFullBlock = schedule.mode == ScheduleMode.BLOCK
    val currentStart = schedule.startTime ?: LocalTime.of(9, 0)
    val currentEnd = schedule.endTime ?: LocalTime.of(17, 0)

    fun openTimePicker(initial: LocalTime, onSelected: (LocalTime) -> Unit) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute -> onSelected(LocalTime.of(hourOfDay, minute)) },
            initial.hour,
            initial.minute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Active Days Section
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
                TextButton(onClick = { onScheduleChange(schedule.copy(daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7))) }) {
                    Text("ALL", style = MaterialTheme.typography.labelSmall, color = palette.accent, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { onScheduleChange(schedule.copy(daysOfWeek = setOf(2, 3, 4, 5, 6))) }) {
                    Text("WEEKDAYS", style = MaterialTheme.typography.labelSmall, color = palette.ink, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { onScheduleChange(schedule.copy(daysOfWeek = setOf(1, 7))) }) {
                    Text("WEEKENDS", style = MaterialTheme.typography.labelSmall, color = palette.ink, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days square segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            daysList.forEach { (dayNum, dayLabel) ->
                val selected = schedule.daysOfWeek.contains(dayNum)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, if (selected) palette.border else palette.hairline, RoundedCornerShape(2.dp))
                        .clickable {
                            val updated = if (selected) schedule.daysOfWeek - dayNum else schedule.daysOfWeek + dayNum
                            onScheduleChange(schedule.copy(daysOfWeek = updated))
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

        Spacer(modifier = Modifier.height(20.dp))

        // Restriction Mode Toggle
        Text(
            text = "RESTRICTION MODE",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode 1: Full Blackout
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(3.dp))
                    .border(BorderStroke(if (isFullBlock) 2.dp else 1.dp, if (isFullBlock) palette.border else palette.hairline), RoundedCornerShape(3.dp))
                    .clickable {
                        onScheduleChange(
                            schedule.copy(
                                mode = ScheduleMode.BLOCK,
                                type = ScheduleType.RECURRING,
                                startTime = currentStart,
                                endTime = currentEnd
                            )
                        )
                    },
                color = if (isFullBlock) palette.surface else palette.surface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "FULL BLACKOUT",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Block apps completely during designated hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )
                }
            }

            // Mode 2: Time Allowance
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(3.dp))
                    .border(BorderStroke(if (!isFullBlock) 2.dp else 1.dp, if (!isFullBlock) palette.border else palette.hairline), RoundedCornerShape(3.dp))
                    .clickable {
                        onScheduleChange(
                            schedule.copy(
                                mode = ScheduleMode.USAGE_LIMIT,
                                type = ScheduleType.DAILY,
                                usageLimitMinutes = if (schedule.usageLimitMinutes > 0) schedule.usageLimitMinutes else 30
                            )
                        )
                    },
                color = palette.surface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "USAGE ALLOWANCE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Permit limited screen time quota before locking",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isFullBlock) {
            // PRIMARY 24-HOUR DRAG TIMELINE
            DayTimeline24hEditor(
                startTime = currentStart,
                endTime = currentEnd,
                onRangeChanged = { newStart, newEnd ->
                    onScheduleChange(schedule.copy(startTime = newStart, endTime = newEnd))
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Numeric Time Pickers as Fallback
            Text(
                text = "NUMERIC TIME PICKERS (TAP TO FINE-TUNE)",
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary,
                letterSpacing = 1.0.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, palette.hairline, RoundedCornerShape(2.dp))
                        .clickable { openTimePicker(currentStart) { onScheduleChange(schedule.copy(startTime = it)) } },
                    color = palette.surface
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = palette.inkSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START: ${currentStart.format(timeFormatter)}",
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
                        .clickable { openTimePicker(currentEnd) { onScheduleChange(schedule.copy(endTime = it)) } },
                    color = palette.surface
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = palette.inkSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "END: ${currentEnd.format(timeFormatter)}",
                            style = MaterialTheme.typography.titleSmall.tabular(),
                            fontWeight = FontWeight.Bold,
                            color = palette.ink
                        )
                    }
                }
            }
        } else {
            // USAGE ALLOWANCE SETTINGS
            Text(
                text = "DAILY QUOTA MINUTES",
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary,
                letterSpacing = 1.0.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val allowancePresets = listOf(15, 30, 45, 60, 90)
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
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Contract Plain-English Summary
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
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 4: REVIEW & PLEDGE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStep4PledgeReview(
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
            text = "CONTRACT SPECIFICATION",
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary,
            letterSpacing = 1.2.sp
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

                // Detail item 1: Apps
                Text(
                    text = "TARGET APPLICATIONS",
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

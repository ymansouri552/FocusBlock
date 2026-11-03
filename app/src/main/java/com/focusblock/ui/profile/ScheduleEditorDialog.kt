package com.focusblock.ui.profile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.R
import com.focusblock.domain.model.LimitScope
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.domain.model.ScheduleType
import com.focusblock.ui.theme.LocalCustomColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleEditorDialog(
    initialSchedule: Schedule?,
    index: Int?,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val customColors = LocalCustomColors.current

    var selectedMode by remember {
        mutableStateOf(initialSchedule?.mode ?: ScheduleMode.BLOCK)
    }

    var selectedType by remember {
        mutableStateOf(initialSchedule?.type ?: ScheduleType.RECURRING)
    }

    var usageLimitMinutes by remember {
        mutableIntStateOf(if (initialSchedule != null && initialSchedule.usageLimitMinutes > 0) initialSchedule.usageLimitMinutes else 15)
    }

    var limitScope by remember {
        mutableStateOf(initialSchedule?.limitScope ?: LimitScope.PER_APP)
    }

    var daysOfWeek by remember {
        mutableStateOf(initialSchedule?.daysOfWeek ?: setOf(1, 2, 3, 4, 5, 6, 7))
    }
    var startTime by remember {
        mutableStateOf(initialSchedule?.startTime ?: LocalTime.of(8, 0))
    }
    var endTime by remember {
        mutableStateOf(initialSchedule?.endTime ?: LocalTime.of(12, 0))
    }

    var startDateTime by remember {
        val dt = initialSchedule?.startDateTime?.atZone(zone)?.toLocalDateTime()
            ?: LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0)
        mutableStateOf(dt)
    }
    var endDateTime by remember {
        val dt = initialSchedule?.endDateTime?.atZone(zone)?.toLocalDateTime()
            ?: LocalDateTime.now().plusHours(3).withMinute(0).withSecond(0)
        mutableStateOf(dt)
    }

    var durationMinutes by remember {
        mutableIntStateOf(if (initialSchedule != null && initialSchedule.durationMinutes > 0) initialSchedule.durationMinutes else 30)
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val daysList = remember {
        listOf(
            1 to "Sun",
            2 to "Mon",
            3 to "Tue",
            4 to "Wed",
            5 to "Thu",
            6 to "Fri",
            7 to "Sat"
        )
    }

    val isOvernight = remember(startTime, endTime) {
        endTime.isBefore(startTime)
    }

    fun openTimePicker(initial: LocalTime, onSelected: (LocalTime) -> Unit) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute -> onSelected(LocalTime.of(hourOfDay, minute)) },
            initial.hour,
            initial.minute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    fun openDatePicker(initial: LocalDate, onSelected: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth -> onSelected(LocalDate.of(year, month + 1, dayOfMonth)) },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (index != null) stringResource(id = R.string.profile_edit_schedule)
                    else stringResource(id = R.string.schedule_add),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Blocking Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val modes = listOf(ScheduleMode.BLOCK, ScheduleMode.USAGE_LIMIT)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, customColors.cardBorder, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    modes.forEach { mode ->
                        val isSelected = selectedMode == mode
                        val title = when (mode) {
                            ScheduleMode.BLOCK -> stringResource(id = R.string.schedule_mode_block)
                            ScheduleMode.USAGE_LIMIT -> stringResource(id = R.string.schedule_mode_usage_limit)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable {
                                    selectedMode = mode
                                    errorMessage = null
                                    if (mode == ScheduleMode.USAGE_LIMIT && (selectedType == ScheduleType.QUICK || selectedType == ScheduleType.ONE_OFF)) {
                                        selectedType = ScheduleType.RECURRING
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedMode == ScheduleMode.USAGE_LIMIT) {
                    Text(
                        text = stringResource(id = R.string.schedule_usage_limit_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = listOf(5, 10, 15, 30, 45, 60)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presets.forEach { minutes ->
                            FilterChip(
                                selected = usageLimitMinutes == minutes,
                                onClick = { usageLimitMinutes = minutes },
                                label = { Text("$minutes min") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = if (usageLimitMinutes > 0) usageLimitMinutes.toString() else "",
                        onValueChange = {
                            usageLimitMinutes = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0
                        },
                        label = { Text("Allowed Duration (minutes)") },
                        placeholder = { Text("e.g. 15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Quota Scope",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = limitScope == LimitScope.PER_APP,
                            onClick = { limitScope = LimitScope.PER_APP },
                            label = { Text(stringResource(id = R.string.schedule_limit_scope_per_app)) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = limitScope == LimitScope.COMBINED,
                            onClick = { limitScope = LimitScope.COMBINED },
                            label = { Text(stringResource(id = R.string.schedule_limit_scope_combined)) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.schedule_reset_frequency_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val usageTypes = listOf(ScheduleType.RECURRING, ScheduleType.HOURLY, ScheduleType.DAILY)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(1.dp, customColors.cardBorder, RoundedCornerShape(10.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        usageTypes.forEach { type ->
                            val isSelected = selectedType == type
                            val title = when (type) {
                                ScheduleType.RECURRING -> stringResource(id = R.string.schedule_time_window)
                                ScheduleType.HOURLY -> stringResource(id = R.string.schedule_hourly)
                                ScheduleType.DAILY -> stringResource(id = R.string.schedule_daily)
                                else -> ""
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        selectedType = type
                                        errorMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    val staticTypes = listOf(ScheduleType.RECURRING, ScheduleType.ONE_OFF, ScheduleType.QUICK)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(1.dp, customColors.cardBorder, RoundedCornerShape(10.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        staticTypes.forEach { type ->
                            val isSelected = selectedType == type
                            val title = when (type) {
                                ScheduleType.RECURRING -> stringResource(id = R.string.schedule_recurring)
                                ScheduleType.ONE_OFF -> stringResource(id = R.string.schedule_one_off)
                                ScheduleType.QUICK -> stringResource(id = R.string.schedule_quick)
                                else -> ""
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        selectedType = type
                                        errorMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (selectedType == ScheduleType.RECURRING || selectedType == ScheduleType.HOURLY || selectedType == ScheduleType.DAILY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.schedule_days),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7); errorMessage = null }
                            ) {
                                Text("All", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(
                                onClick = { daysOfWeek = setOf(2, 3, 4, 5, 6); errorMessage = null }
                            ) {
                                Text("Weekdays", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(
                                onClick = { daysOfWeek = setOf(1, 7); errorMessage = null }
                            ) {
                                Text("Weekends", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        daysList.forEach { (dayNum, dayLabel) ->
                            val selected = daysOfWeek.contains(dayNum)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        daysOfWeek = if (selected) daysOfWeek - dayNum else daysOfWeek + dayNum
                                        errorMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = if (selected) null else BorderStroke(1.dp, customColors.cardBorder),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = dayLabel.take(2),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (selectedType == ScheduleType.RECURRING) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.schedule_start_time),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { openTimePicker(startTime) { startTime = it } },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, customColors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = startTime.format(timeFormatter),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.schedule_end_time),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { openTimePicker(endTime) { endTime = it } },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, customColors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = endTime.format(timeFormatter),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (isOvernight) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nightlight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Overnight window: ends the following morning",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (selectedType == ScheduleType.HOURLY) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Apps can be used up to $usageLimitMinutes minutes each hour. Once exceeded, apps are blocked until the top of the next hour.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (selectedType == ScheduleType.DAILY) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Apps can be used up to $usageLimitMinutes minutes per day. Once exceeded, apps are blocked for the rest of the day and reset at midnight.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (selectedType == ScheduleType.ONE_OFF) {
                    Text(
                        text = "Start Date and Time",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable {
                                    openDatePicker(startDateTime.toLocalDate()) {
                                        startDateTime = it.atTime(startDateTime.toLocalTime())
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, customColors.cardBorder)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(startDateTime.toLocalDate().format(dateFormatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    openTimePicker(startDateTime.toLocalTime()) {
                                        startDateTime = startDateTime.toLocalDate().atTime(it)
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, customColors.cardBorder)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(startDateTime.toLocalTime().format(timeFormatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "End Date and Time",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable {
                                    openDatePicker(endDateTime.toLocalDate()) {
                                        endDateTime = it.atTime(endDateTime.toLocalTime())
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, customColors.cardBorder)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(endDateTime.toLocalDate().format(dateFormatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    openTimePicker(endDateTime.toLocalTime()) {
                                        endDateTime = endDateTime.toLocalDate().atTime(it)
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, customColors.cardBorder)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(endDateTime.toLocalTime().format(timeFormatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (selectedType == ScheduleType.QUICK) {
                    Text(
                        text = "Preset Duration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val quickPresets = listOf(15, 30, 45, 60, 120)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPresets.forEach { minutes ->
                            FilterChip(
                                selected = durationMinutes == minutes,
                                onClick = { durationMinutes = minutes },
                                label = { Text("$minutes min") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = durationMinutes.toString(),
                        onValueChange = {
                            durationMinutes = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
                        },
                        label = { Text("Duration (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val previewSchedule = remember(
                    selectedType, selectedMode, usageLimitMinutes, limitScope,
                    daysOfWeek, startTime, endTime, startDateTime, endDateTime, durationMinutes
                ) {
                    Schedule(
                        type = selectedType,
                        mode = selectedMode,
                        usageLimitMinutes = if (selectedMode == ScheduleMode.USAGE_LIMIT) usageLimitMinutes else 0,
                        limitScope = limitScope,
                        daysOfWeek = daysOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        startDateTime = startDateTime.atZone(zone).toInstant(),
                        endDateTime = endDateTime.atZone(zone).toInstant(),
                        durationMinutes = durationMinutes
                    )
                }

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Rule Summary",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = com.focusblock.domain.model.ProfilePresets.getExplanation(previewSchedule, 0),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMode == ScheduleMode.USAGE_LIMIT && usageLimitMinutes <= 0) {
                        errorMessage = "Usage limit duration must be greater than 0"
                        return@Button
                    }

                    when (selectedType) {
                        ScheduleType.RECURRING -> {
                            if (daysOfWeek.isEmpty()) {
                                errorMessage = "Please select at least one day"
                                return@Button
                            }
                            if (startTime == endTime) {
                                errorMessage = context.getString(R.string.block_schedule_same_time_error)
                                return@Button
                            }
                            val schedule = Schedule(
                                type = ScheduleType.RECURRING,
                                mode = selectedMode,
                                usageLimitMinutes = if (selectedMode == ScheduleMode.USAGE_LIMIT) usageLimitMinutes else 0,
                                limitScope = limitScope,
                                daysOfWeek = daysOfWeek,
                                startTime = startTime,
                                endTime = endTime
                            )
                            onSave(schedule)
                        }
                        ScheduleType.HOURLY -> {
                            if (daysOfWeek.isEmpty()) {
                                errorMessage = "Please select at least one day"
                                return@Button
                            }
                            val schedule = Schedule(
                                type = ScheduleType.HOURLY,
                                mode = selectedMode,
                                usageLimitMinutes = if (selectedMode == ScheduleMode.USAGE_LIMIT) usageLimitMinutes else 0,
                                limitScope = limitScope,
                                daysOfWeek = daysOfWeek
                            )
                            onSave(schedule)
                        }
                        ScheduleType.DAILY -> {
                            if (daysOfWeek.isEmpty()) {
                                errorMessage = "Please select at least one day"
                                return@Button
                            }
                            val schedule = Schedule(
                                type = ScheduleType.DAILY,
                                mode = selectedMode,
                                usageLimitMinutes = if (selectedMode == ScheduleMode.USAGE_LIMIT) usageLimitMinutes else 0,
                                limitScope = limitScope,
                                daysOfWeek = daysOfWeek
                            )
                            onSave(schedule)
                        }
                        ScheduleType.ONE_OFF -> {
                            val startInstant = startDateTime.atZone(zone).toInstant()
                            val endInstant = endDateTime.atZone(zone).toInstant()
                            if (!endInstant.isAfter(startInstant)) {
                                errorMessage = "End time must be after start time"
                                return@Button
                            }
                            val schedule = Schedule(
                                type = ScheduleType.ONE_OFF,
                                mode = selectedMode,
                                usageLimitMinutes = if (selectedMode == ScheduleMode.USAGE_LIMIT) usageLimitMinutes else 0,
                                limitScope = limitScope,
                                startDateTime = startInstant,
                                endDateTime = endInstant
                            )
                            onSave(schedule)
                        }
                        ScheduleType.QUICK -> {
                            if (durationMinutes <= 0) {
                                errorMessage = "Duration must be greater than 0"
                                return@Button
                            }
                            val schedule = Schedule(
                                type = ScheduleType.QUICK,
                                mode = selectedMode,
                                usageLimitMinutes = if (selectedMode == ScheduleMode.USAGE_LIMIT) usageLimitMinutes else 0,
                                limitScope = limitScope,
                                durationMinutes = durationMinutes,
                                quickStartInstant = Instant.now()
                            )
                            onSave(schedule)
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(id = R.string.save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

fun formatScheduleSummary(schedule: Schedule): String {
    val dayNames = listOf(
        1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat"
    )
    val selectedDays = dayNames.filter { schedule.daysOfWeek.contains(it.first) }.map { it.second }
    val daysStr = if (selectedDays.isEmpty() || selectedDays.size == 7) "Every day" else selectedDays.joinToString(", ")

    if (schedule.mode == ScheduleMode.USAGE_LIMIT) {
        val limitStr = "${schedule.usageLimitMinutes}m limit"
        val scopeStr = if (schedule.limitScope == LimitScope.COMBINED) " (combined)" else ""
        return when (schedule.type) {
            ScheduleType.HOURLY -> "$limitStr: Hourly ($daysStr)$scopeStr"
            ScheduleType.DAILY -> "$limitStr: Daily ($daysStr)$scopeStr"
            ScheduleType.RECURRING -> {
                val startStr = schedule.startTime?.toString() ?: "--:--"
                val endStr = schedule.endTime?.toString() ?: "--:--"
                val overnightNote = if (schedule.startTime != null && schedule.endTime != null && !schedule.endTime.isAfter(schedule.startTime)) " (+1d)" else ""
                "$limitStr: $daysStr $startStr - $endStr$overnightNote$scopeStr"
            }
            ScheduleType.ONE_OFF -> {
                val zone = ZoneId.systemDefault()
                val formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm")
                val start = schedule.startDateTime?.atZone(zone)?.format(formatter) ?: "--"
                val end = schedule.endDateTime?.atZone(zone)?.format(formatter) ?: "--"
                "$limitStr: $start - $end$scopeStr"
            }
            ScheduleType.QUICK -> "$limitStr: Quick block (${schedule.durationMinutes}m)"
        }
    } else {
        return when (schedule.type) {
            ScheduleType.RECURRING -> {
                val startStr = schedule.startTime?.toString() ?: "--:--"
                val endStr = schedule.endTime?.toString() ?: "--:--"
                val overnightNote = if (schedule.startTime != null && schedule.endTime != null && !schedule.endTime.isAfter(schedule.startTime)) " (+1d)" else ""
                "$daysStr $startStr - $endStr$overnightNote"
            }
            ScheduleType.HOURLY -> "Every hour ($daysStr)"
            ScheduleType.DAILY -> "All day ($daysStr)"
            ScheduleType.ONE_OFF -> {
                val zone = ZoneId.systemDefault()
                val formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm")
                val start = schedule.startDateTime?.atZone(zone)?.format(formatter) ?: "--"
                val end = schedule.endDateTime?.atZone(zone)?.format(formatter) ?: "--"
                "One-off: $start - $end"
            }
            ScheduleType.QUICK -> {
                "Quick block: ${schedule.durationMinutes} min"
            }
        }
    }
}

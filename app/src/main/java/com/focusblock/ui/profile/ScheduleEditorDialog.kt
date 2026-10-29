package com.focusblock.ui.profile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.focusblock.domain.model.Schedule
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

    var selectedType by remember {
        mutableStateOf(initialSchedule?.type ?: ScheduleType.RECURRING)
    }

    // Recurring state (default Mon-Fri: 2..6, 09:00 - 17:00)
    var daysOfWeek by remember {
        mutableStateOf(initialSchedule?.daysOfWeek ?: setOf(2, 3, 4, 5, 6))
    }
    var startTime by remember {
        mutableStateOf(initialSchedule?.startTime ?: LocalTime.of(9, 0))
    }
    var endTime by remember {
        mutableStateOf(initialSchedule?.endTime ?: LocalTime.of(17, 0))
    }

    // One-off state
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

    // Quick block state
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
        !endTime.isAfter(startTime)
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
                // Tab Selector
                val types = listOf(ScheduleType.RECURRING, ScheduleType.ONE_OFF, ScheduleType.QUICK)
                TabRow(
                    selectedTabIndex = types.indexOf(selectedType),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    types.forEach { type ->
                        val (title, icon) = when (type) {
                            ScheduleType.RECURRING -> stringResource(id = R.string.schedule_recurring) to Icons.Default.Repeat
                            ScheduleType.ONE_OFF -> stringResource(id = R.string.schedule_one_off) to Icons.Default.CalendarMonth
                            ScheduleType.QUICK -> stringResource(id = R.string.schedule_quick) to Icons.Default.HourglassTop
                        }
                        Tab(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            text = { Text(text = title, maxLines = 1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                when (selectedType) {
                    ScheduleType.RECURRING -> {
                        Text(
                            text = stringResource(id = R.string.schedule_days).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Modern Circular Day Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            daysList.forEach { (dayNum, dayLabel) ->
                                val selected = daysOfWeek.contains(dayNum)
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = if (selected) null else BorderStroke(1.dp, customColors.cardBorder),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable {
                                            daysOfWeek = if (selected) daysOfWeek - dayNum else daysOfWeek + dayNum
                                            errorMessage = null
                                        }
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

                        Spacer(modifier = Modifier.height(18.dp))

                        // Time Cards Row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.schedule_start_time).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { openTimePicker(startTime) { startTime = it } },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, customColors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
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
                                    text = stringResource(id = R.string.schedule_end_time).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { openTimePicker(endTime) { endTime = it } },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, customColors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
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
                                        text = "Overnight window (ends the next day)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    ScheduleType.ONE_OFF -> {
                        Text(
                            text = "START DATE & TIME",
                            style = MaterialTheme.typography.labelSmall,
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
                                shape = RoundedCornerShape(12.dp),
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
                                shape = RoundedCornerShape(12.dp),
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
                            text = "END DATE & TIME",
                            style = MaterialTheme.typography.labelSmall,
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
                                shape = RoundedCornerShape(12.dp),
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
                                shape = RoundedCornerShape(12.dp),
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

                    ScheduleType.QUICK -> {
                        Text(
                            text = "PRESET DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val presets = listOf(15, 30, 45, 60, 120)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presets.forEach { minutes ->
                                FilterChip(
                                    selected = durationMinutes == minutes,
                                    onClick = { durationMinutes = minutes },
                                    label = { Text("$minutes min") }
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
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
                    when (selectedType) {
                        ScheduleType.RECURRING -> {
                            if (daysOfWeek.isEmpty()) {
                                errorMessage = "Please select at least one day"
                                return@Button
                            }
                            val schedule = Schedule(
                                type = ScheduleType.RECURRING,
                                daysOfWeek = daysOfWeek,
                                startTime = startTime,
                                endTime = endTime
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
    return when (schedule.type) {
        ScheduleType.RECURRING -> {
            val dayNames = listOf(
                1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat"
            )
            val selectedDays = dayNames.filter { schedule.daysOfWeek.contains(it.first) }.map { it.second }
            val daysStr = if (selectedDays.isEmpty()) "No days" else selectedDays.joinToString(", ")
            val startStr = schedule.startTime?.toString() ?: "--:--"
            val endStr = schedule.endTime?.toString() ?: "--:--"
            val overnightNote = if (schedule.startTime != null && schedule.endTime != null && !schedule.endTime.isAfter(schedule.startTime)) " (+1d)" else ""
            "$daysStr $startStr - $endStr$overnightNote"
        }
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

package com.focusblock.domain.model

import java.time.LocalTime

data class ProfilePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val defaultName: String,
    val defaultSchedule: Schedule,
    val defaultStrict: Boolean = false
)

object ProfilePresets {
    val presets = listOf(
        ProfilePreset(
            id = "deep_work",
            title = "Deep Work",
            subtitle = "Block distractions during business or study hours",
            badge = "Mon - Fri · 09:00 - 17:00",
            defaultName = "Deep Work",
            defaultSchedule = Schedule(
                type = ScheduleType.RECURRING,
                mode = ScheduleMode.BLOCK,
                daysOfWeek = setOf(2, 3, 4, 5, 6),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0)
            )
        ),
        ProfilePreset(
            id = "screen_time_budget",
            title = "Daily Screen Time Budget",
            subtitle = "Daily quota to curb excessive usage across apps",
            badge = "30 min/day · Shared",
            defaultName = "Screen Time Budget",
            defaultSchedule = Schedule(
                type = ScheduleType.DAILY,
                mode = ScheduleMode.USAGE_LIMIT,
                usageLimitMinutes = 30,
                limitScope = LimitScope.COMBINED,
                daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7)
            )
        ),
        ProfilePreset(
            id = "bedtime_wind_down",
            title = "Bedtime Wind Down",
            subtitle = "Silence late night distractions before sleep",
            badge = "Everyday · 22:00 - 07:00",
            defaultName = "Bedtime Wind Down",
            defaultSchedule = Schedule(
                type = ScheduleType.RECURRING,
                mode = ScheduleMode.BLOCK,
                daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7),
                startTime = LocalTime.of(22, 0),
                endTime = LocalTime.of(7, 0)
            )
        ),
        ProfilePreset(
            id = "screen_free_morning",
            title = "Screen-Free Morning",
            subtitle = "Protect your focus and morning routine until midday",
            badge = "Mon - Fri · 06:00 - 12:00",
            defaultName = "Screen-Free Morning",
            defaultSchedule = Schedule(
                type = ScheduleType.RECURRING,
                mode = ScheduleMode.BLOCK,
                daysOfWeek = setOf(2, 3, 4, 5, 6),
                startTime = LocalTime.of(6, 0),
                endTime = LocalTime.of(12, 0)
            )
        ),
        ProfilePreset(
            id = "custom",
            title = "Custom Profile",
            subtitle = "Build your own schedule and limits from scratch",
            badge = "Blank slate",
            defaultName = "",
            defaultSchedule = Schedule(
                type = ScheduleType.RECURRING,
                mode = ScheduleMode.BLOCK,
                daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0)
            )
        )
    )

    fun getExplanation(schedule: Schedule, appCount: Int): String {
        val appsText = when (appCount) {
            0 -> "Selected apps"
            1 -> "The selected app"
            else -> "$appCount selected apps"
        }
        val dayNames = listOf(
            1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat"
        )
        val selectedDays = dayNames.filter { schedule.daysOfWeek.contains(it.first) }.map { it.second }
        val daysText = when {
            selectedDays.size == 7 -> "every day"
            selectedDays.size == 5 && schedule.daysOfWeek.containsAll(listOf(2, 3, 4, 5, 6)) -> "every weekday (Mon to Fri)"
            selectedDays.size == 2 && schedule.daysOfWeek.containsAll(listOf(1, 7)) -> "on weekends (Sat & Sun)"
            selectedDays.isNotEmpty() -> "on " + selectedDays.joinToString(", ")
            else -> "no days selected"
        }

        return if (schedule.mode == ScheduleMode.USAGE_LIMIT) {
            val scopeText = if (schedule.limitScope == LimitScope.COMBINED) "combined across all apps" else "for each app individually"
            when (schedule.type) {
                ScheduleType.HOURLY ->
                    "$appsText can be used up to ${schedule.usageLimitMinutes} minutes each hour ($scopeText) $daysText. Once exceeded, access is blocked until the next hour starts."
                ScheduleType.DAILY ->
                    "$appsText can be used up to ${schedule.usageLimitMinutes} minutes per day ($scopeText) $daysText. Once exceeded, access is blocked until midnight."
                ScheduleType.RECURRING -> {
                    val startStr = schedule.startTime?.toString() ?: "00:00"
                    val endStr = schedule.endTime?.toString() ?: "00:00"
                    "$appsText have a ${schedule.usageLimitMinutes} minute limit ($scopeText) between $startStr and $endStr $daysText."
                }
                else ->
                    "$appsText have a ${schedule.usageLimitMinutes} minute limit $daysText."
            }
        } else {
            when (schedule.type) {
                ScheduleType.RECURRING -> {
                    val startStr = schedule.startTime?.toString() ?: "00:00"
                    val endStr = schedule.endTime?.toString() ?: "00:00"
                    val overnight = schedule.startTime != null && schedule.endTime != null && !schedule.endTime.isAfter(schedule.startTime)
                    val overnightText = if (overnight) " (ending the following morning)" else ""
                    "$appsText will be completely blocked $daysText from $startStr to $endStr$overnightText."
                }
                ScheduleType.ONE_OFF ->
                    "$appsText will be completely blocked during the selected one-off time window."
                ScheduleType.QUICK ->
                    "$appsText will be locked immediately for ${schedule.durationMinutes} minutes."
                else ->
                    "$appsText will be completely blocked $daysText."
            }
        }
    }
}

package com.focusblock.domain.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

enum class ScheduleType { RECURRING, ONE_OFF, QUICK }

data class Schedule(
    val type: ScheduleType = ScheduleType.RECURRING,
    val daysOfWeek: Set<Int> = emptySet(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val startDateTime: Instant? = null,
    val endDateTime: Instant? = null,
    val durationMinutes: Int = 0,
    val quickStartInstant: Instant? = null
) {
    fun toStorageString(): String {
        val parts = mutableListOf<String>()
        parts.add("type=${type.name}")
        parts.add("days=${daysOfWeek.joinToString(",")}")
        parts.add("startTime=${startTime ?: ""}")
        parts.add("endTime=${endTime ?: ""}")
        parts.add("startDateTime=${startDateTime ?: ""}")
        parts.add("endDateTime=${endDateTime ?: ""}")
        parts.add("durationMinutes=$durationMinutes")
        parts.add("quickStart=${quickStartInstant ?: ""}")
        return parts.joinToString(";")
    }

    companion object {
        fun fromStorageString(s: String): Schedule? {
            return runCatching {
                var type = ScheduleType.RECURRING
                var days = emptySet<Int>()
                var startTime: LocalTime? = null
                var endTime: LocalTime? = null
                var startDateTime: Instant? = null
                var endDateTime: Instant? = null
                var durationMinutes = 0
                var quickStart: Instant? = null
                for (kv in s.split(";")) {
                    val idx = kv.indexOf('=')
                    if (idx < 0) continue
                    val k = kv.substring(0, idx).trim()
                    val v = kv.substring(idx + 1).trim()
                    when (k) {
                        "type" -> type = ScheduleType.valueOf(v)
                        "days" -> days = if (v.isBlank()) emptySet() else v.split(",").map { it.toIntOrNull() }.filterNotNull().toSet()
                        "startTime" -> startTime = if (v.isBlank()) null else LocalTime.parse(v)
                        "endTime" -> endTime = if (v.isBlank()) null else LocalTime.parse(v)
                        "startDateTime" -> startDateTime = if (v.isBlank()) null else Instant.parse(v)
                        "endDateTime" -> endDateTime = if (v.isBlank()) null else Instant.parse(v)
                        "durationMinutes" -> durationMinutes = v.toIntOrNull() ?: 0
                        "quickStart" -> quickStart = if (v.isBlank()) null else Instant.parse(v)
                    }
                }
                Schedule(type, days, startTime, endTime, startDateTime, endDateTime, durationMinutes, quickStart)
            }.getOrNull()
        }
    }
}

data class ActiveProfileInfo(
    val profile: Profile,
    val schedule: Schedule,
    val endInstant: Instant
)

object ScheduleEngine {
    private val zone: ZoneId
        get() = ZoneId.systemDefault()

    fun isActive(profile: Profile, now: Instant = Instant.now()): Boolean {
        return activeProfile(profile, now) != null
    }

    fun activeProfile(profile: Profile, now: Instant = Instant.now()): ActiveProfileInfo? {
        if (!profile.enabled) return null
        val localNow = now.atZone(zone).toLocalDateTime()
        for (schedule in profile.schedules) {
            val window = activeWindow(schedule, now, localNow)
            if (window != null) {
                return ActiveProfileInfo(profile, schedule, window.second)
            }
        }
        return null
    }

    fun activeProfiles(profiles: List<Profile>, now: Instant = Instant.now()): List<ActiveProfileInfo> {
        return profiles.filter { it.enabled }.mapNotNull { activeProfile(it, now) }
    }

    fun blockedPackages(profiles: List<Profile>, now: Instant = Instant.now()): Set<String> {
        return activeProfiles(profiles, now).flatMap { it.profile.blockedApps }.toSet()
    }

    /**
     * Returns the (startInstant, endInstant) pair for [schedule] if it is currently active,
     * or null otherwise.  Handles RECURRING (including overnight windows that cross midnight),
     * ONE_OFF, and QUICK schedule types.
     *
     * For RECURRING schedules, both **today** (offset 0) and **yesterday** (offset -1) are
     * checked.  This is essential for overnight windows whose end time falls on the *next*
     * calendar day — e.g. a Monday 22:00 → Tuesday 02:00 window that is still active at
     * 01:00 on Tuesday must be detected, which the old `recurringEnd()` missed because it
     * only searched *forward* from today.
     */
    private fun activeWindow(schedule: Schedule, now: Instant, localNow: LocalDateTime): Pair<Instant, Instant>? {
        return when (schedule.type) {
            ScheduleType.RECURRING -> activeRecurringWindow(schedule, localNow)
            ScheduleType.ONE_OFF -> oneOffWindow(schedule, now)
            ScheduleType.QUICK -> quickWindow(schedule, now)
        }
    }

    private fun activeRecurringWindow(schedule: Schedule, localNow: LocalDateTime): Pair<Instant, Instant>? {
        val startTime = schedule.startTime ?: return null
        val endTime = schedule.endTime ?: return null
        val days = schedule.daysOfWeek
        if (days.isEmpty()) return null

        val today = localNow.toLocalDate()
        val nowInstant = localNow.atZone(zone).toInstant()

        // Check yesterday (offset -1) then today (offset 0) to catch overnight windows.
        for (offset in -1L..0L) {
            val candidateDate = today.plusDays(offset)
            // Convert ISO day-of-week (1=Monday) to Calendar convention (1=Sunday)
            val calendarDay = (candidateDate.dayOfWeek.value % 7) + 1
            if (!days.contains(calendarDay)) continue

            val startInstant = candidateDate.atTime(startTime).atZone(zone).toInstant()
            val endInstant = if (!endTime.isAfter(startTime)) {
                // endTime is on the *next* day (overnight window)
                candidateDate.plusDays(1).atTime(endTime).atZone(zone).toInstant()
            } else {
                candidateDate.atTime(endTime).atZone(zone).toInstant()
            }

            if (!nowInstant.isBefore(startInstant) && nowInstant.isBefore(endInstant)) {
                return startInstant to endInstant
            }
        }
        return null
    }

    private fun oneOffWindow(schedule: Schedule, now: Instant): Pair<Instant, Instant>? {
        val start = schedule.startDateTime
        val end = schedule.endDateTime
        if (start != null && end != null && !now.isBefore(start) && now.isBefore(end)) {
            return start to end
        }
        return null
    }

    private fun quickWindow(schedule: Schedule, now: Instant): Pair<Instant, Instant>? {
        val start = schedule.quickStartInstant ?: return null
        val end = start.plus(schedule.durationMinutes.toLong(), ChronoUnit.MINUTES)
        if (!now.isBefore(start) && now.isBefore(end)) {
            return start to end
        }
        return null
    }
}
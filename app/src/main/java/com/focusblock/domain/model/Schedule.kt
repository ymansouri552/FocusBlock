package com.focusblock.domain.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

enum class ScheduleType {
    RECURRING, // Specific Time Window (e.g. 08:00 - 12:00) with daysOfWeek
    HOURLY,    // Every hour (resets at :00) with daysOfWeek (or all days)
    DAILY,     // Every day (resets at midnight) with daysOfWeek (or all days)
    ONE_OFF,   // Specific date-time range
    QUICK      // Countdown block
}

enum class ScheduleMode {
    BLOCK,       // Static full block during the window
    USAGE_LIMIT  // Allowed up to usageLimitMinutes, then blocked for the remainder of the window
}

enum class LimitScope {
    PER_APP,   // Each app in profile gets the quota individually
    COMBINED   // All apps in profile share the quota
}

data class Schedule(
    val type: ScheduleType = ScheduleType.RECURRING,
    val mode: ScheduleMode = ScheduleMode.BLOCK,
    val usageLimitMinutes: Int = 0,
    val limitScope: LimitScope = LimitScope.PER_APP,
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
        parts.add("mode=${mode.name}")
        parts.add("usageLimitMinutes=$usageLimitMinutes")
        parts.add("limitScope=${limitScope.name}")
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
                var mode = ScheduleMode.BLOCK
                var usageLimitMinutes = 0
                var limitScope = LimitScope.PER_APP
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
                        "type" -> type = runCatching { ScheduleType.valueOf(v) }.getOrDefault(ScheduleType.RECURRING)
                        "mode" -> mode = runCatching { ScheduleMode.valueOf(v) }.getOrDefault(ScheduleMode.BLOCK)
                        "usageLimitMinutes" -> usageLimitMinutes = v.toIntOrNull() ?: 0
                        "limitScope" -> limitScope = runCatching { LimitScope.valueOf(v) }.getOrDefault(LimitScope.PER_APP)
                        "days" -> days = if (v.isBlank()) emptySet() else v.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                        "startTime" -> startTime = if (v.isBlank()) null else LocalTime.parse(v)
                        "endTime" -> endTime = if (v.isBlank()) null else LocalTime.parse(v)
                        "startDateTime" -> startDateTime = if (v.isBlank()) null else Instant.parse(v)
                        "endDateTime" -> endDateTime = if (v.isBlank()) null else Instant.parse(v)
                        "durationMinutes" -> durationMinutes = v.toIntOrNull() ?: 0
                        "quickStart" -> quickStart = if (v.isBlank()) null else Instant.parse(v)
                    }
                }
                Schedule(
                    type = type,
                    mode = mode,
                    usageLimitMinutes = usageLimitMinutes,
                    limitScope = limitScope,
                    daysOfWeek = days,
                    startTime = startTime,
                    endTime = endTime,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    durationMinutes = durationMinutes,
                    quickStartInstant = quickStart
                )
            }.getOrNull()
        }
    }
}

data class ActiveProfileInfo(
    val profile: Profile,
    val schedule: Schedule,
    val endInstant: Instant,
    val startInstant: Instant = Instant.EPOCH
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
                return ActiveProfileInfo(profile, schedule, window.second, window.first)
            }
        }
        return null
    }

    fun activeProfiles(profiles: List<Profile>, now: Instant = Instant.now()): List<ActiveProfileInfo> {
        return profiles.filter { it.enabled }.mapNotNull { activeProfile(it, now) }
    }

    /**
     * All active profiles with all matching active schedules (in case a profile has multiple active schedules).
     */
    fun allActiveProfileInfos(profiles: List<Profile>, now: Instant = Instant.now()): List<ActiveProfileInfo> {
        val result = mutableListOf<ActiveProfileInfo>()
        val localNow = now.atZone(zone).toLocalDateTime()
        for (profile in profiles) {
            if (!profile.enabled) continue
            for (schedule in profile.schedules) {
                val window = activeWindow(schedule, now, localNow)
                if (window != null) {
                    result.add(ActiveProfileInfo(profile, schedule, window.second, window.first))
                }
            }
        }
        return result
    }

    /**
     * Packages that are blocked immediately by static (non-usage-limit) schedules.
     */
    fun staticBlockedPackages(profiles: List<Profile>, now: Instant = Instant.now()): Set<String> {
        return activeProfiles(profiles, now)
            .filter { it.schedule.mode == ScheduleMode.BLOCK }
            .flatMap { it.profile.blockedApps }
            .toSet()
    }

    fun blockedPackages(profiles: List<Profile>, now: Instant = Instant.now()): Set<String> {
        return staticBlockedPackages(profiles, now)
    }

    /**
     * Returns the (startInstant, endInstant) pair for [schedule] if it is currently active,
     * or null otherwise.
     */
    fun activeWindow(schedule: Schedule, now: Instant, localNow: LocalDateTime): Pair<Instant, Instant>? {
        return when (schedule.type) {
            ScheduleType.RECURRING -> activeRecurringWindow(schedule, localNow)
            ScheduleType.HOURLY -> activeHourlyWindow(schedule, localNow)
            ScheduleType.DAILY -> activeDailyWindow(schedule, localNow)
            ScheduleType.ONE_OFF -> oneOffWindow(schedule, now)
            ScheduleType.QUICK -> quickWindow(schedule, now)
        }
    }

    private fun activeHourlyWindow(schedule: Schedule, localNow: LocalDateTime): Pair<Instant, Instant>? {
        val days = schedule.daysOfWeek
        val today = localNow.toLocalDate()
        val calendarDay = (today.dayOfWeek.value % 7) + 1
        if (days.isNotEmpty() && !days.contains(calendarDay)) return null

        val startLocal = localNow.truncatedTo(ChronoUnit.HOURS)
        val endLocal = startLocal.plusHours(1)
        val startInstant = startLocal.atZone(zone).toInstant()
        val endInstant = endLocal.atZone(zone).toInstant()
        return startInstant to endInstant
    }

    private fun activeDailyWindow(schedule: Schedule, localNow: LocalDateTime): Pair<Instant, Instant>? {
        val days = schedule.daysOfWeek
        val today = localNow.toLocalDate()
        val calendarDay = (today.dayOfWeek.value % 7) + 1
        if (days.isNotEmpty() && !days.contains(calendarDay)) return null

        val startLocal = today.atStartOfDay()
        val endLocal = today.plusDays(1).atStartOfDay()
        val startInstant = startLocal.atZone(zone).toInstant()
        val endInstant = endLocal.atZone(zone).toInstant()
        return startInstant to endInstant
    }

    private fun activeRecurringWindow(schedule: Schedule, localNow: LocalDateTime): Pair<Instant, Instant>? {
        val startTime = schedule.startTime ?: return null
        val endTime = schedule.endTime ?: return null
        if (startTime == endTime) return null
        val days = schedule.daysOfWeek
        if (days.isEmpty()) return null

        val today = localNow.toLocalDate()
        val nowInstant = localNow.atZone(zone).toInstant()

        // Check yesterday (offset -1) then today (offset 0) to catch overnight windows.
        for (offset in -1L..0L) {
            val candidateDate = today.plusDays(offset)
            val calendarDay = (candidateDate.dayOfWeek.value % 7) + 1
            if (!days.contains(calendarDay)) continue

            val startInstant = candidateDate.atTime(startTime).atZone(zone).toInstant()
            val endInstant = if (endTime.isBefore(startTime)) {
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

package com.focusblock.domain.model

import java.time.Duration
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
            return Schedule(type, days, startTime, endTime, startDateTime, endDateTime, durationMinutes, quickStart)
        }
    }
}

data class ActiveProfileInfo(
    val profile: Profile,
    val schedule: Schedule,
    val endInstant: Instant
)

object ScheduleEngine {
    private val zone = ZoneId.systemDefault()

    fun isActive(profile: Profile, now: Instant = Instant.now()): Boolean {
        return activeProfile(profile, now) != null
    }

    fun activeProfile(profile: Profile, now: Instant = Instant.now()): ActiveProfileInfo? {
        if (!profile.enabled) return null
        val localNow = now.atZone(zone).toLocalDateTime()
        for (schedule in profile.schedules) {
            val start = startInstant(schedule, localNow)
            val end = endInstant(schedule, localNow)
            if (start != null && end != null && !now.isBefore(start) && now.isBefore(end)) {
                return ActiveProfileInfo(profile, schedule, end)
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

    private fun startInstant(schedule: Schedule, localNow: LocalDateTime): Instant? {
        return when (schedule.type) {
            ScheduleType.RECURRING -> recurringStart(schedule, localNow)
            ScheduleType.ONE_OFF -> oneOffStart(schedule)
            ScheduleType.QUICK -> quickStart(schedule)
        }
    }

    private fun endInstant(schedule: Schedule, localNow: LocalDateTime): Instant? {
        return when (schedule.type) {
            ScheduleType.RECURRING -> recurringEnd(schedule, localNow)
            ScheduleType.ONE_OFF -> oneOffEnd(schedule)
            ScheduleType.QUICK -> quickEnd(schedule)
        }
    }

    private fun recurringStart(schedule: Schedule, localNow: LocalDateTime): Instant? {
        val startTime = schedule.startTime ?: return null
        val days = schedule.daysOfWeek
        if (days.isEmpty()) return null
        var d = localNow.toLocalDate()
        for (i in 0..6) {
            val candidate = d.minusDays(i.toLong())
            if (days.contains(candidate.dayOfWeek.getValue())) {
                return candidate.atTime(startTime).atZone(zone).toInstant()
            }
        }
        return null
    }

    private fun recurringEnd(schedule: Schedule, localNow: LocalDateTime): Instant? {
        val startTime = schedule.startTime ?: return null
        val endTime = schedule.endTime ?: return null
        val days = schedule.daysOfWeek
        if (days.isEmpty()) return null
        var d = localNow.toLocalDate()
        for (i in 0..13) {
            val candidate = d.plusDays(i.toLong())
            if (!days.contains(candidate.dayOfWeek.getValue())) continue
            val startInstant = candidate.atTime(startTime).atZone(zone).toInstant()
            val endInstant = if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                candidate.plusDays(1).atTime(endTime).atZone(zone).toInstant()
            } else {
                candidate.atTime(endTime).atZone(zone).toInstant()
            }
            val nowInstant = localNow.atZone(zone).toInstant()
            if ((nowInstant.isAfter(startInstant) || nowInstant.equals(startInstant)) && nowInstant.isBefore(endInstant)) {
                return endInstant
            }
        }
        return null
    }

    private fun oneOffStart(schedule: Schedule): Instant? = schedule.startDateTime
    private fun oneOffEnd(schedule: Schedule): Instant? = schedule.endDateTime
    private fun quickStart(schedule: Schedule): Instant? = schedule.quickStartInstant
    private fun quickEnd(schedule: Schedule): Instant? {
        val start = schedule.quickStartInstant ?: return null
        return start.plus(schedule.durationMinutes.toLong(), ChronoUnit.MINUTES)
    }
}
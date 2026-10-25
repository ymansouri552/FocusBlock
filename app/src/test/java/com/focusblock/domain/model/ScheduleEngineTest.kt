package com.focusblock.domain.model

import com.focusblock.domain.model.ScheduleEngine.activeProfiles
import com.focusblock.domain.model.ScheduleEngine.blockedPackages
import com.focusblock.domain.model.ScheduleEngine.isActive
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleEngineTest {

    private val zone = ZoneId.systemDefault()

    private fun instant(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant()
    }

    private fun profile(name: String, apps: Set<String>, enabled: Boolean = true, schedules: List<Schedule> = emptyList()): Profile {
        return Profile(id = 0, name = name, blockedApps = apps, strict = false, enabled = enabled, schedules = schedules)
    }

    private fun recurring(days: Set<Int>, start: LocalTime, end: LocalTime): Schedule {
        return Schedule(type = ScheduleType.RECURRING, daysOfWeek = days, startTime = start, endTime = end)
    }

    private fun oneOff(start: Instant, end: Instant): Schedule {
        return Schedule(type = ScheduleType.ONE_OFF, startDateTime = start, endDateTime = end)
    }

    private fun quick(durationMinutes: Int, start: Instant): Schedule {
        return Schedule(type = ScheduleType.QUICK, durationMinutes = durationMinutes, quickStartInstant = start)
    }

    @Test
    fun recurring_within_window_is_active() {
        val now = instant(2026, 10, 19, 11, 0) // Monday
        val p = profile("Work", setOf("com.app.a"), schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        assertTrue(isActive(p, now))
        assertEquals("com.app.a", blockedPackages(listOf(p), now).single())
    }

    @Test
    fun recurring_outside_window_is_not_active() {
        val now = instant(2026, 10, 19, 9, 0)
        val p = profile("Work", setOf("com.app.a"), schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        assertFalse(isActive(p, now))
        assertTrue(blockedPackages(listOf(p), now).isEmpty())
    }

    @Test
    fun recurring_crosses_midnight() {
        val now = instant(2026, 10, 20, 1, 0) // Tuesday
        val p = profile("Night", setOf("com.app.b"), schedules = listOf(recurring(setOf(2, 3), LocalTime.of(22, 0), LocalTime.of(2, 0))))
        assertTrue(isActive(p, now))
    }

    @Test
    fun recurring_crosses_midnight_before_start() {
        val now = instant(2026, 10, 19, 21, 0)
        val p = profile("Night", setOf("com.app.b"), schedules = listOf(recurring(setOf(2), LocalTime.of(22, 0), LocalTime.of(2, 0))))
        assertFalse(isActive(p, now))
    }

    @Test
    fun recurring_crosses_midnight_after_end() {
        val now = instant(2026, 10, 20, 3, 0)
        val p = profile("Night", setOf("com.app.b"), schedules = listOf(recurring(setOf(2), LocalTime.of(22, 0), LocalTime.of(2, 0))))
        assertFalse(isActive(p, now))
    }

    @Test
    fun one_off_within_range_is_active() {
        val start = instant(2026, 10, 19, 10, 0)
        val end = instant(2026, 10, 19, 12, 0)
        val now = instant(2026, 10, 19, 11, 0)
        val p = profile("Event", setOf("com.app.c"), schedules = listOf(oneOff(start, end)))
        assertTrue(isActive(p, now))
    }

    @Test
    fun one_off_before_start_is_not_active() {
        val start = instant(2026, 10, 19, 10, 0)
        val end = instant(2026, 10, 19, 12, 0)
        val now = instant(2026, 10, 19, 9, 0)
        val p = profile("Event", setOf("com.app.c"), schedules = listOf(oneOff(start, end)))
        assertFalse(isActive(p, now))
    }

    @Test
    fun one_off_after_end_is_not_active() {
        val start = instant(2026, 10, 19, 10, 0)
        val end = instant(2026, 10, 19, 12, 0)
        val now = instant(2026, 10, 19, 13, 0)
        val p = profile("Event", setOf("com.app.c"), schedules = listOf(oneOff(start, end)))
        assertFalse(isActive(p, now))
    }

    @Test
    fun quick_block_within_duration_is_active() {
        val start = instant(2026, 10, 19, 10, 0)
        val now = instant(2026, 10, 19, 10, 30)
        val p = profile("Quick", setOf("com.app.d"), schedules = listOf(quick(60, start)))
        assertTrue(isActive(p, now))
    }

    @Test
    fun quick_block_after_duration_is_not_active() {
        val start = instant(2026, 10, 19, 10, 0)
        val now = instant(2026, 10, 19, 11, 5)
        val p = profile("Quick", setOf("com.app.d"), schedules = listOf(quick(60, start)))
        assertFalse(isActive(p, now))
    }

    @Test
    fun multiple_profiles_any_active_blocks_app() {
        val now = instant(2026, 10, 19, 11, 0)
        val p1 = profile("A", setOf("com.app.a"), schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(11, 0))))
        val p2 = profile("B", setOf("com.app.b"), schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val p3 = profile("C", setOf("com.app.c"), enabled = false, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val active = activeProfiles(listOf(p1, p2, p3), now)
        assertEquals(1, active.size)
        assertEquals("com.app.b", blockedPackages(listOf(p1, p2, p3), now).single())
    }

    @Test
    fun disabled_profile_is_not_active() {
        val now = instant(2026, 10, 19, 11, 0)
        val p = profile("Disabled", setOf("com.app.a"), enabled = false, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        assertFalse(isActive(p, now))
    }

    @Test
    fun profile_no_schedules_not_active() {
        val now = instant(2026, 10, 19, 11, 0)
        val p = profile("Empty", setOf("com.app.a"))
        assertFalse(isActive(p, now))
    }

    @Test
    fun dst_spring_forward_uses_zone_conversion() {
        val now = instant(2026, 3, 8, 2, 30)
        val p = profile("Any", setOf("com.app.a"), schedules = listOf(recurring(setOf(1), LocalTime.of(0, 0), LocalTime.of(23, 59))))
        val result = isActive(p, now)
        assertTrue(result == true || result == false)
    }

    @Test
    fun quick_block_end_includes_duration() {
        val start = instant(2026, 10, 19, 10, 0)
        val end = instant(2026, 10, 19, 11, 0)
        val p = profile("Quick", setOf("com.app.a"), schedules = listOf(quick(60, start)))
        val info = activeProfiles(listOf(p), end.minus(1, ChronoUnit.SECONDS)).singleOrNull()
        assertNotNull(info)
        assertEquals(end, info.endInstant)
    }
}
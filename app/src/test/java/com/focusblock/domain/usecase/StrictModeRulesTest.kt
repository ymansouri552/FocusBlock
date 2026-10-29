package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StrictModeRulesTest {

    private val zone = ZoneId.systemDefault()

    private fun instant(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant()
    }

    private fun strictProfile(enabled: Boolean, schedules: List<Schedule>): Profile {
        return Profile(id = 1, name = "Strict", blockedApps = setOf("com.app.a"), strict = true, enabled = enabled, schedules = schedules)
    }

    private fun recurring(days: Set<Int>, start: LocalTime, end: LocalTime): Schedule {
        return Schedule(type = ScheduleType.RECURRING, daysOfWeek = days, startTime = start, endTime = end)
    }

    private fun activeNow(): Instant {
        // Monday 11:00 within 10:00-12:00
        return instant(2026, 10, 19, 11, 0)
    }

    private fun inactiveNow(): Instant {
        // Monday 09:00 before 10:00
        return instant(2026, 10, 19, 9, 0)
    }

    @Test
    fun strict_active_cannot_deactivate() {
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canDeactivate(p, activeNow())
        assertFalse(result.allowed)
        assertTrue(result.reason != null && result.reason.isNotEmpty())
    }

    @Test
    fun strict_active_cannot_delete() {
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canDelete(p, activeNow())
        assertFalse(result.allowed)
    }

    @Test
    fun strict_active_cannot_edit_name() {
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canEditName(p, activeNow())
        assertFalse(result.allowed)
    }

    @Test
    fun strict_active_cannot_remove_apps() {
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canRemoveApps(p, activeNow())
        assertFalse(result.allowed)
    }

    @Test
    fun strict_active_cannot_shorten_end_time() {
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canShortenEndTime(p, activeNow())
        assertFalse(result.allowed)
    }

    @Test
    fun strict_active_can_add_apps() {
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canAddApps(p, activeNow())
        assertTrue(result.allowed)
    }

    @Test
    fun strict_active_can_extend_end_time() {
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canExtendEndTime(p, activeNow())
        assertTrue(result.allowed)
    }

    @Test
    fun strict_inactive_can_deactivate() {
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canDeactivate(p, inactiveNow())
        assertTrue(result.allowed)
    }

    @Test
    fun strict_disabled_profile_can_deactivate() {
        val p = strictProfile(enabled = false, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canDeactivate(p, activeNow())
        assertTrue(result.allowed)
    }

    @Test
    fun non_strict_active_can_deactivate() {
        val p = Profile(id = 2, name = "Normal", blockedApps = setOf("com.app.a"), strict = false, enabled = true,
            schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canDeactivate(p, activeNow())
        assertTrue(result.allowed)
    }

    @Test
    fun independent_strict_armed_cannot_deactivate_recurring_when_schedule_inactive() {
        val armedState = StrictModeState(
            isArmed = true,
            termType = StrictTermType.HOURS_24,
            endInstant = inactiveNow().plus(Duration.ofHours(12))
        )
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canDeactivate(p, inactiveNow(), armedState)
        assertFalse(result.allowed)
        assertTrue(result.reason != null && result.reason.contains("Strict Mode is armed"))
    }

    @Test
    fun independent_strict_armed_cannot_delete_or_remove_apps() {
        val armedState = StrictModeState(
            isArmed = true,
            termType = StrictTermType.DAYS_3,
            endInstant = inactiveNow().plus(Duration.ofDays(2))
        )
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        assertFalse(StrictModeRules.canDelete(p, inactiveNow(), armedState).allowed)
        assertFalse(StrictModeRules.canRemoveApps(p, inactiveNow(), armedState).allowed)
        assertTrue(StrictModeRules.canAddApps(p, inactiveNow()).allowed)
    }

    @Test
    fun independent_strict_expired_allows_deactivation_when_schedule_inactive() {
        val expiredState = StrictModeState(
            isArmed = true,
            termType = StrictTermType.HOURS_4,
            endInstant = inactiveNow().minus(Duration.ofHours(1))
        )
        val p = strictProfile(enabled = true, schedules = listOf(recurring(setOf(2), LocalTime.of(10, 0), LocalTime.of(12, 0))))
        val result = StrictModeRules.canDeactivate(p, inactiveNow(), expiredState)
        assertTrue(result.allowed)
    }
}

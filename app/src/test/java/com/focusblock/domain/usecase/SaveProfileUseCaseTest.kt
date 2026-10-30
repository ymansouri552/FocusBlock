package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class SaveProfileUseCaseTest {

    private lateinit var useCase: SaveProfileUseCase
    private val zone = ZoneId.systemDefault()

    private fun instant(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant()
    }

    private fun activeNow(): Instant {
        // Monday (dayOfWeek = 2 in Calendar-like mapping / Monday) 11:00 within 10:00-12:00
        return instant(2026, 10, 19, 11, 0)
    }

    private val fakeRepository = object : ProfileRepository {
        override fun getAllProfiles(): Flow<List<Profile>> = emptyFlow()
        override fun getProfileById(id: Int): Flow<Profile?> = emptyFlow()
        override suspend fun insertProfile(profile: Profile): Long = 1L
        override suspend fun updateProfile(profile: Profile): Int = 1
        override suspend fun deleteProfile(profile: Profile): Int = 1
        override suspend fun editDisabled(profile: Profile): Int = 1
        override suspend fun enableProfile(id: Int, enabled: Boolean): Int = 1
    }

    @Before
    fun setUp() {
        useCase = SaveProfileUseCase(fakeRepository)
    }

    @Test
    fun canSave_newProfile_isAllowed() {
        val newProfile = Profile(id = 0, name = "New", blockedApps = setOf("com.app.a"), strict = true)
        val result = useCase.canSave(newProfile, newProfile, activeNow())
        assertTrue(result.allowed)
    }

    @Test
    fun canSave_scheduleShiftOutsideActiveWindow_isBlocked() {
        // Currently active 10:00 to 12:00 on Monday
        val current = Profile(
            id = 1,
            name = "Strict",
            blockedApps = setOf("com.app.a"),
            strict = true,
            enabled = true,
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0))
            )
        )
        // User shifts the schedule to 19:00 - 21:00 (which is not active at 11:00)
        val updated = current.copy(
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(19, 0), endTime = LocalTime.of(21, 0))
            )
        )

        val result = useCase.canSave(current, updated, activeNow())
        assertFalse("Schedule shift outside of active time must be blocked", result.allowed)
    }

    @Test
    fun canSave_shortenEndTime_isBlocked() {
        val current = Profile(
            id = 1,
            name = "Strict",
            blockedApps = setOf("com.app.a"),
            strict = true,
            enabled = true,
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(10, 0), endTime = LocalTime.of(14, 0))
            )
        )
        // User shortens end time to 12:00
        val updated = current.copy(
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0))
            )
        )

        val result = useCase.canSave(current, updated, activeNow())
        assertFalse("Shortening end time during active strict session must be blocked", result.allowed)
    }

    @Test
    fun canSave_extendEndTime_isAllowed() {
        val current = Profile(
            id = 1,
            name = "Strict",
            blockedApps = setOf("com.app.a"),
            strict = true,
            enabled = true,
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0))
            )
        )
        // User extends end time to 15:00
        val updated = current.copy(
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(10, 0), endTime = LocalTime.of(15, 0))
            )
        )

        val result = useCase.canSave(current, updated, activeNow())
        assertTrue("Extending end time must be allowed", result.allowed)
    }

    @Test
    fun canSave_removeApps_isBlocked() {
        val current = Profile(
            id = 1,
            name = "Strict",
            blockedApps = setOf("com.app.a", "com.app.b"),
            strict = true,
            enabled = true,
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0))
            )
        )
        val updated = current.copy(blockedApps = setOf("com.app.a"))

        val result = useCase.canSave(current, updated, activeNow())
        assertFalse("Removing apps during active strict session must be blocked", result.allowed)
    }

    @Test
    fun canSave_addApps_isAllowed() {
        val current = Profile(
            id = 1,
            name = "Strict",
            blockedApps = setOf("com.app.a"),
            strict = true,
            enabled = true,
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0))
            )
        )
        val updated = current.copy(blockedApps = setOf("com.app.a", "com.app.b"))

        val result = useCase.canSave(current, updated, activeNow())
        assertTrue("Adding apps during active strict session must be allowed", result.allowed)
    }

    @Test
    fun canSave_disableProfile_isBlocked() {
        val current = Profile(
            id = 1,
            name = "Strict",
            blockedApps = setOf("com.app.a"),
            strict = true,
            enabled = true,
            schedules = listOf(
                Schedule(type = ScheduleType.RECURRING, daysOfWeek = setOf(2), startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0))
            )
        )
        val updated = current.copy(enabled = false)

        val result = useCase.canSave(current, updated, activeNow())
        assertFalse("Disabling active strict profile must be blocked", result.allowed)
    }
}

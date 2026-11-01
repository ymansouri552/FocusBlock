package com.focusblock.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
class HandleTimeChangeUseCaseTest {

    private lateinit var context: Context
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var trustedClock: TrustedClock
    private lateinit var profileRepository: FakeProfileRepository
    private lateinit var useCase: HandleTimeChangeUseCase

    private class FakeProfileRepository : ProfileRepository {
        val profiles = MutableStateFlow<List<Profile>>(emptyList())

        override fun getAllProfiles(): Flow<List<Profile>> = profiles
        override fun getProfileById(id: Int): Flow<Profile?> = MutableStateFlow(profiles.value.find { it.id == id })
        override suspend fun insertProfile(profile: Profile): Long = 1L
        override suspend fun updateProfile(profile: Profile): Int = 1
        override suspend fun deleteProfile(profile: Profile): Int = 1
        override suspend fun editDisabled(profile: Profile): Int = 1
        override suspend fun enableProfile(id: Int, enabled: Boolean): Int = 1
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        settingsDataStore = SettingsDataStore(context)
        trustedClock = TrustedClock(context, settingsDataStore)
        profileRepository = FakeProfileRepository()
        useCase = HandleTimeChangeUseCase(settingsDataStore, profileRepository, trustedClock)
        StrictModeRules.independentState = StrictModeState()
    }

    @Test
    fun no_action_when_strict_mode_not_armed(): Unit = runBlocking {
        settingsDataStore.disarmStrictMode()
        val result = useCase.handleTimeChanged(
            currentWallTime = Instant.now(),
            currentElapsedRealtimeMs = 100_000L
        )
        assertIs<TimeChangeResult.NoActionNeeded>(result)
    }

    @Test
    fun detects_clock_roll_forward_tampering_and_keeps_true_end_time(): Unit = runBlocking {
        val startWall = Instant.parse("2026-10-30T10:00:00Z")
        val scheduledEnd = startWall.plus(Duration.ofHours(4)) // 14:00
        val startElapsed = 10_000_000L

        trustedClock.reanchor(startWall, startElapsed)

        // Arm strict mode passing startWall
        settingsDataStore.armStrictMode(
            termType = StrictTermType.HOURS_4,
            customEndInstant = scheduledEnd,
            now = startWall,
            elapsedRealtimeMs = startElapsed
        )

        // Simulate: only 30 minutes of real time passed (1,800,000 ms elapsed)
        val currentElapsed = startElapsed + 1_800_000L // 3.5 hours remaining

        // But user advanced the device clock to 15:00 (past scheduledEnd 14:00!)
        val tamperedWall = Instant.parse("2026-10-30T15:00:00Z")

        val result = useCase.handleTimeChanged(
            currentWallTime = tamperedWall,
            currentElapsedRealtimeMs = currentElapsed
        )

        assertIs<TimeChangeResult.ClockTamperCorrected>(result)
        // Expected remaining real time: 3.5 hours = 12,600,000 ms
        val expectedRemaining = Duration.ofHours(4).toMillis() - 1_800_000L
        assertEquals(expectedRemaining, result.remainingRealMs)

        // The term keeps its true end (14:00), not the tampered wall-clock end.
        assertEquals(scheduledEnd, result.correctedEnd)

        // Verify DataStore and StrictModeRules were updated
        val updatedDataStore = settingsDataStore.getStrictModeState()
        assertEquals(scheduledEnd, updatedDataStore.endInstant)
        assertEquals(scheduledEnd, StrictModeRules.independentState.endInstant)
    }

    @Test
    fun detects_clock_roll_backward_tampering_and_corrects_end_time(): Unit = runBlocking {
        val startWall = Instant.parse("2026-10-30T10:00:00Z")
        val scheduledEnd = startWall.plus(Duration.ofHours(2)) // 12:00
        val startElapsed = 10_000_000L

        trustedClock.reanchor(startWall, startElapsed)

        settingsDataStore.armStrictMode(
            termType = StrictTermType.HOURS_4,
            customEndInstant = scheduledEnd,
            now = startWall,
            elapsedRealtimeMs = startElapsed
        )

        // 1 hour of real time has passed
        val currentElapsed = startElapsed + 3_600_000L // 1 hour remaining

        // User rolled clock back before the start time to 08:00
        val tamperedWall = Instant.parse("2026-10-30T08:00:00Z")

        val result = useCase.handleTimeChanged(
            currentWallTime = tamperedWall,
            currentElapsedRealtimeMs = currentElapsed
        )

        assertIs<TimeChangeResult.ClockTamperCorrected>(result)
        val expectedRemaining = 3_600_000L // 1 hour
        assertEquals(expectedRemaining, result.remainingRealMs)
        assertEquals(scheduledEnd, result.correctedEnd)
    }

    @Test
    fun completes_term_when_actual_real_time_has_fully_elapsed(): Unit = runBlocking {
        val startWall = Instant.parse("2026-10-30T10:00:00Z")
        val scheduledEnd = startWall.plus(Duration.ofHours(2))
        val startElapsed = 10_000_000L

        trustedClock.reanchor(startWall, startElapsed)

        settingsDataStore.armStrictMode(
            termType = StrictTermType.HOURS_4,
            customEndInstant = scheduledEnd,
            now = startWall,
            elapsedRealtimeMs = startElapsed
        )

        // Full 2 hours of real time passed
        val currentElapsed = startElapsed + Duration.ofHours(2).toMillis() + 1000L
        val currentWall = startWall.plus(Duration.ofHours(2)).plusSeconds(1)

        val result = useCase.handleTimeChanged(
            currentWallTime = currentWall,
            currentElapsedRealtimeMs = currentElapsed
        )

        assertIs<TimeChangeResult.TermCompleted>(result)
        assertFalse(settingsDataStore.getStrictModeState().isArmed)
        assertFalse(StrictModeRules.independentState.isArmed)
    }

    @Test
    fun per_profile_strict_survives_clock_roll_forward(): Unit = runBlocking {
        val zone = ZoneId.systemDefault()
        val startWall = ZonedDateTime.of(2026, 10, 19, 10, 0, 0, 0, zone).toInstant()
        val startElapsed = 50_000_000L

        trustedClock.reanchor(startWall, startElapsed)

        val strictProfile = Profile(
            id = 1,
            name = "Strict",
            blockedApps = setOf("com.app.a"),
            strict = true,
            enabled = true,
            schedules = listOf(
                Schedule(
                    type = ScheduleType.RECURRING,
                    daysOfWeek = setOf(2), // Monday
                    startTime = LocalTime.of(10, 0),
                    endTime = LocalTime.of(12, 0)
                )
            )
        )
        profileRepository.profiles.value = listOf(strictProfile)

        // Only 15 real minutes passed, but the device clock was moved to 20:00.
        val tamperedWall = ZonedDateTime.of(2026, 10, 19, 20, 0, 0, 0, zone).toInstant()
        val result = useCase.handleTimeChanged(
            currentWallTime = tamperedWall,
            currentElapsedRealtimeMs = startElapsed + 900_000L
        )

        assertIs<TimeChangeResult.StrictSessionProtected>(result)
    }
}

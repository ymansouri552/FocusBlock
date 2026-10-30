package com.focusblock.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
class HandleTimeChangeUseCaseTest {

    private lateinit var context: Context
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var useCase: HandleTimeChangeUseCase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        settingsDataStore = SettingsDataStore(context)
        useCase = HandleTimeChangeUseCase(settingsDataStore)
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
    fun detects_clock_roll_forward_tampering_and_extends_end_time(): Unit = runBlocking {
        val startWall = Instant.parse("2026-10-30T10:00:00Z")
        val scheduledEnd = startWall.plus(Duration.ofHours(4)) // 14:00
        val startElapsed = 10_000_000L

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

        // New end instant should be tamperedWall + 3.5 hours = 18:30
        val expectedCorrectedEnd = tamperedWall.plusMillis(expectedRemaining)
        assertEquals(expectedCorrectedEnd, result.correctedEnd)

        // Verify DataStore and StrictModeRules were updated
        val updatedDataStore = settingsDataStore.getStrictModeState()
        assertEquals(expectedCorrectedEnd, updatedDataStore.endInstant)
        assertEquals(expectedCorrectedEnd, StrictModeRules.independentState.endInstant)
    }

    @Test
    fun detects_clock_roll_backward_tampering_and_corrects_end_time(): Unit = runBlocking {
        val startWall = Instant.parse("2026-10-30T10:00:00Z")
        val scheduledEnd = startWall.plus(Duration.ofHours(2)) // 12:00
        val startElapsed = 10_000_000L

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
        assertEquals(tamperedWall.plusMillis(expectedRemaining), result.correctedEnd)
    }

    @Test
    fun completes_term_when_actual_real_time_has_fully_elapsed(): Unit = runBlocking {
        val startWall = Instant.parse("2026-10-30T10:00:00Z")
        val scheduledEnd = startWall.plus(Duration.ofHours(2))
        val startElapsed = 10_000_000L

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
}

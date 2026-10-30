package com.focusblock.domain.usecase

import android.os.SystemClock
import com.focusblock.data.local.datastore.SettingsDataStore
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

sealed interface TimeChangeResult {
    data object NoActionNeeded : TimeChangeResult
    data object TermCompleted : TimeChangeResult
    data class ClockTamperCorrected(
        val previousEnd: Instant,
        val correctedEnd: Instant,
        val remainingRealMs: Long
    ) : TimeChangeResult
}

@Singleton
class HandleTimeChangeUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    /**
     * Handles system TIME_CHANGED or TIMEZONE_CHANGED broadcasts.
     * Uses elapsedRealtime (which cannot be manipulated by device clock settings)
     * to detect if wall-clock time was rolled forward or backward during an active strict session.
     */
    suspend fun handleTimeChanged(
        currentWallTime: Instant = Instant.now(),
        currentElapsedRealtimeMs: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L)
    ): TimeChangeResult {
        val info = settingsDataStore.getStrictElapsedInfo()
        if (!info.isArmed) return TimeChangeResult.NoActionNeeded

        val startedElapsed = info.startedElapsedRealtimeMs ?: return TimeChangeResult.NoActionNeeded
        val expectedDuration = info.expectedDurationMs ?: return TimeChangeResult.NoActionNeeded
        val endInstant = info.endInstant ?: return TimeChangeResult.NoActionNeeded

        val realElapsedSinceStart = currentElapsedRealtimeMs - startedElapsed
        if (realElapsedSinceStart < 0) {
            // Device reboot occurred or clock anomaly
            return TimeChangeResult.NoActionNeeded
        }

        // 1. Check if the true physical duration has completed
        if (realElapsedSinceStart >= expectedDuration) {
            settingsDataStore.disarmStrictMode()
            val disarmed = StrictModeRules.independentState.copy(isArmed = false)
            StrictModeRules.independentState = disarmed
            return TimeChangeResult.TermCompleted
        }

        // 2. Strict term is still supposed to be active
        val remainingRealMs = expectedDuration - realElapsedSinceStart

        // Did the user roll wall-clock time forward (so currentWallTime is now past endInstant)?
        // Or did they roll wall-clock time backward (currentWallTime < startedAt)?
        val wallClockTampered = !currentWallTime.isBefore(endInstant) ||
                (info.startedAt != null && currentWallTime.isBefore(info.startedAt))

        if (wallClockTampered) {
            // Re-anchor the wall-clock end instant to: currentWallTime + remainingRealMs
            val correctedEnd = currentWallTime.plusMillis(remainingRealMs)
            settingsDataStore.updateStrictEndInstant(correctedEnd)

            val updatedState = StrictModeRules.independentState.copy(
                isArmed = true,
                endInstant = correctedEnd
            )
            StrictModeRules.independentState = updatedState

            return TimeChangeResult.ClockTamperCorrected(
                previousEnd = endInstant,
                correctedEnd = correctedEnd,
                remainingRealMs = remainingRealMs
            )
        }

        return TimeChangeResult.NoActionNeeded
    }
}

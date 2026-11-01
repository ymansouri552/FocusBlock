package com.focusblock.domain.usecase

import android.os.SystemClock
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

sealed interface TimeChangeResult {
    data object NoActionNeeded : TimeChangeResult
    data object TermCompleted : TimeChangeResult

    /** A clock jump was detected while a strict session was active and was ignored. */
    data object StrictSessionProtected : TimeChangeResult

    data class ClockTamperCorrected(
        val previousEnd: Instant,
        val correctedEnd: Instant,
        val remainingRealMs: Long
    ) : TimeChangeResult
}

@Singleton
class HandleTimeChangeUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val profileRepository: ProfileRepository,
    private val trustedClock: TrustedClock
) {
    /**
     * Handles system TIME_CHANGED or TIMEZONE_CHANGED broadcasts.
     *
     * `elapsedRealtime` (which cannot be manipulated by device clock settings) is the source of
     * truth. It protects both independent strict mode and strict profile schedules: the trusted
     * clock keeps advancing from its anchor even when the user rolls the wall clock backwards or
     * forwards, and the schedule engine is evaluated against that trusted time.
     */
    suspend fun handleTimeChanged(
        currentWallTime: Instant = Instant.now(),
        currentElapsedRealtimeMs: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L),
        bootCount: Int = settingsDataStore.currentBootCount()
    ): TimeChangeResult {
        val trustedNow = trustedClock.now(currentWallTime, currentElapsedRealtimeMs, bootCount)
        val wallClockChanged = abs(currentWallTime.toEpochMilli() - trustedNow.toEpochMilli()) >
            TrustedClock.DEFAULT_TOLERANCE_MS

        val info = settingsDataStore.getStrictElapsedInfo()
        val profiles = runCatching { profileRepository.getAllProfiles().first() }.getOrDefault(emptyList())

        val strictProfileActive = profiles.any {
            it.strict && it.enabled && ScheduleEngine.isActive(it, trustedNow)
        }
        val independentStillActive = info.isArmed &&
            (info.endInstant == null || trustedNow.isBefore(info.endInstant))

        if (!strictProfileActive && !independentStillActive) {
            // No committed strict term: accept a legitimate clock change as the new baseline.
            if (wallClockChanged) {
                trustedClock.reanchor(currentWallTime, currentElapsedRealtimeMs, bootCount)
            }
            if (info.isArmed) {
                settingsDataStore.disarmStrictMode()
                StrictModeRules.independentState = StrictModeRules.independentState.copy(isArmed = false)
                return TimeChangeResult.TermCompleted
            }
            return TimeChangeResult.NoActionNeeded
        }

        trustedClock.recordStrictProgress(currentElapsedRealtimeMs, bootCount)

        if (!info.isArmed) {
            // Only per-profile strict is running; the trusted clock already keeps it on real time.
            return if (wallClockChanged) TimeChangeResult.StrictSessionProtected
            else TimeChangeResult.NoActionNeeded
        }

        val startedElapsed = info.startedElapsedRealtimeMs
        val expectedDuration = info.expectedDurationMs
        val endInstant = info.endInstant

        if (startedElapsed == null || expectedDuration == null || endInstant == null) {
            // Ongoing/legacy term: nothing to correct, the trusted clock does the protection.
            return if (wallClockChanged) TimeChangeResult.StrictSessionProtected
            else TimeChangeResult.NoActionNeeded
        }

        val realElapsedSinceStart = currentElapsedRealtimeMs - startedElapsed
        if (realElapsedSinceStart < 0) {
            // Reboot occurred; RestoreBlockStateUseCase re-anchors the elapsed baseline.
            return TimeChangeResult.NoActionNeeded
        }

        if (realElapsedSinceStart >= expectedDuration) {
            settingsDataStore.disarmStrictMode()
            StrictModeRules.independentState = StrictModeRules.independentState.copy(isArmed = false)
            return TimeChangeResult.TermCompleted
        }

        val remainingRealMs = expectedDuration - realElapsedSinceStart
        val correctedEnd = trustedNow.plusMillis(remainingRealMs)

        if (wallClockChanged || correctedEnd != endInstant) {
            settingsDataStore.updateStrictEndInstant(correctedEnd)
            StrictModeRules.independentState = StrictModeRules.independentState.copy(
                isArmed = true,
                endInstant = correctedEnd
            )
            return TimeChangeResult.ClockTamperCorrected(
                previousEnd = endInstant,
                correctedEnd = correctedEnd,
                remainingRealMs = remainingRealMs
            )
        }

        return TimeChangeResult.NoActionNeeded
    }
}

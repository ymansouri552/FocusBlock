package com.focusblock.domain.usecase

import android.os.SystemClock
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.repository.BlockLogRepository
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreBlockStateUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val profileRepository: ProfileRepository,
    private val trustedClock: TrustedClock,
    private val blockLogRepository: BlockLogRepository? = null
) {
    /**
     * Re-evaluates and restores block and strict-mode states upon device boot or app update.
     *
     * The elapsedRealtime-based term is authoritative. If the device rebooted, progress is
     * frozen at the last persisted observation and the wall-clock end is re-anchored. This
     * prevents rolling the clock forward while the device is off from silently disarming a
     * committed strict term.
     */
    suspend fun restore(
        now: Instant = Instant.now(),
        currentElapsedRealtimeMs: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L),
        bootCount: Int = settingsDataStore.currentBootCount()
    ): StrictModeState {
        val trustedNow = trustedClock.now(now, currentElapsedRealtimeMs, bootCount)

        // Prune expired quick block profiles and logs older than 30 days
        runCatching {
            val allProfiles = profileRepository.getAllProfiles().first()
            allProfiles.filter { p ->
                p.schedules.isNotEmpty() &&
                p.schedules.all { it.type == ScheduleType.QUICK } &&
                !ScheduleEngine.isActive(p, trustedNow)
            }.forEach { profileRepository.deleteProfile(it) }

            val thirtyDaysAgo = trustedNow.minus(Duration.ofDays(30)).toEpochMilli()
            blockLogRepository?.deleteLogsOlderThan(thirtyDaysAgo)
        }

        val strictState = settingsDataStore.getStrictModeState()

        if (!strictState.isArmed) {
            profileRepository.getAllProfiles().first()
            return strictState
        }

        val info = settingsDataStore.getStrictElapsedInfo()
        val end = strictState.endInstant
        val startedElapsed = info.startedElapsedRealtimeMs
        val expectedDuration = info.expectedDurationMs

        if (end != null && startedElapsed != null && expectedDuration != null) {
            val rebooted = (info.bootCount != null && info.bootCount != bootCount) ||
                currentElapsedRealtimeMs < startedElapsed

            if (rebooted) {
                // Freeze progress at the last observation instead of trusting the new wall clock.
                val observed = (info.lastObservedElapsedSinceStartMs ?: 0L).coerceAtLeast(0L)
                val remaining = expectedDuration - observed
                if (remaining <= 0L) {
                    return disarm(strictState)
                }
                val newStartedElapsed = currentElapsedRealtimeMs - observed
                val correctedEnd = trustedNow.plusMillis(remaining)
                settingsDataStore.reanchorStrictElapsed(
                    startedElapsedRealtimeMs = newStartedElapsed,
                    bootCount = bootCount,
                    newEndInstant = correctedEnd,
                    lastObservedElapsedSinceStartMs = observed
                )
                val corrected = strictState.copy(endInstant = correctedEnd)
                StrictModeRules.independentState = corrected
                profileRepository.getAllProfiles().first()
                return corrected
            }

            val realElapsed = currentElapsedRealtimeMs - startedElapsed
            val remaining = expectedDuration - realElapsed
            if (remaining <= 0L || !trustedNow.isBefore(end)) {
                return disarm(strictState)
            }

            val correctedEnd = trustedNow.plusMillis(remaining)
            if (correctedEnd != end) {
                settingsDataStore.updateStrictEndInstant(correctedEnd)
                val corrected = strictState.copy(endInstant = correctedEnd)
                StrictModeRules.independentState = corrected
                profileRepository.getAllProfiles().first()
                return corrected
            }
        } else if (end != null && !trustedNow.isBefore(end)) {
            // Legacy term without elapsed metadata: fall back to wall clock.
            return disarm(strictState)
        }

        // Still within strict term (or ongoing); enforce strict state.
        StrictModeRules.independentState = strictState
        profileRepository.getAllProfiles().first()
        return strictState
    }

    private suspend fun disarm(strictState: StrictModeState): StrictModeState {
        settingsDataStore.disarmStrictMode()
        val disarmed = strictState.copy(isArmed = false)
        StrictModeRules.independentState = disarmed
        profileRepository.getAllProfiles().first()
        return disarmed
    }
}

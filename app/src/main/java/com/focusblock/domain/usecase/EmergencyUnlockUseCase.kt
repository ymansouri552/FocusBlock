package com.focusblock.domain.usecase

import android.os.SystemClock
import com.focusblock.data.local.datastore.SettingsDataStore
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

sealed interface EmergencyUnlockStatus {
    data object Disabled : EmergencyUnlockStatus
    data object NotRequested : EmergencyUnlockStatus
    data class Waiting(
        val requestTime: Instant,
        val availableAt: Instant,
        val remainingSeconds: Long
    ) : EmergencyUnlockStatus
    data object Available : EmergencyUnlockStatus
}

@Singleton
class EmergencyUnlockUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    /**
     * The waiting period is measured with elapsedRealtime, so moving the device clock forward
     * does not make an emergency unlock available early.
     */
    suspend fun getStatus(
        now: Instant = Instant.now(),
        currentElapsedRealtimeMs: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L),
        bootCount: Int = settingsDataStore.currentBootCount()
    ): EmergencyUnlockStatus {
        val enabled = settingsDataStore.getEmergencyUnlockEnabled()
        if (!enabled) return EmergencyUnlockStatus.Disabled

        val requestEpochMs = settingsDataStore.getEmergencyUnlockRequestEpochMs()
            ?: return EmergencyUnlockStatus.NotRequested

        val waitMinutes = settingsDataStore.getEmergencyUnlockMinutes()
        val waitMs = Duration.ofMinutes(waitMinutes.toLong()).toMillis()
        val requestInstant = Instant.ofEpochMilli(requestEpochMs)
        val availableAt = requestInstant.plusMillis(waitMs)

        val requestElapsed = settingsDataStore.getEmergencyUnlockRequestElapsedRealtimeMs()
        val requestBootCount = settingsDataStore.getEmergencyUnlockRequestBootCount()

        val elapsedSinceRequest = if (
            requestElapsed != null &&
            requestBootCount != null &&
            requestBootCount == bootCount &&
            currentElapsedRealtimeMs >= requestElapsed
        ) {
            currentElapsedRealtimeMs - requestElapsed
        } else {
            // Legacy request or reboot: fall back to wall clock.
            Duration.between(requestInstant, now).toMillis()
        }

        val remainingMs = waitMs - elapsedSinceRequest
        return if (remainingMs > 0L) {
            EmergencyUnlockStatus.Waiting(
                requestTime = requestInstant,
                availableAt = availableAt,
                remainingSeconds = (remainingMs + 999L) / 1000L
            )
        } else {
            EmergencyUnlockStatus.Available
        }
    }

    suspend fun requestUnlock(
        now: Instant = Instant.now(),
        currentElapsedRealtimeMs: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L),
        bootCount: Int = settingsDataStore.currentBootCount()
    ): Boolean {
        val enabled = settingsDataStore.getEmergencyUnlockEnabled()
        if (!enabled) return false

        settingsDataStore.setEmergencyUnlockRequest(
            epochMs = now.toEpochMilli(),
            elapsedRealtimeMs = currentElapsedRealtimeMs,
            bootCount = bootCount
        )
        return true
    }

    suspend fun cancelUnlock() {
        settingsDataStore.setEmergencyUnlockRequestEpochMs(null)
    }

    suspend fun confirmUnlock(
        now: Instant = Instant.now(),
        currentElapsedRealtimeMs: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L),
        bootCount: Int = settingsDataStore.currentBootCount()
    ): Boolean {
        val status = getStatus(now, currentElapsedRealtimeMs, bootCount)
        if (status !is EmergencyUnlockStatus.Available) {
            return false
        }

        // Legitimate unlock: disarm strict mode and reset request
        settingsDataStore.disarmStrictMode()
        StrictModeRules.independentState = StrictModeRules.independentState.copy(isArmed = false)
        settingsDataStore.setEmergencyUnlockRequestEpochMs(null)
        return true
    }
}

package com.focusblock.domain.usecase

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
    suspend fun getStatus(now: Instant = Instant.now()): EmergencyUnlockStatus {
        val enabled = settingsDataStore.getEmergencyUnlockEnabled()
        if (!enabled) return EmergencyUnlockStatus.Disabled

        val requestEpochMs = settingsDataStore.getEmergencyUnlockRequestEpochMs()
            ?: return EmergencyUnlockStatus.NotRequested

        val waitMinutes = settingsDataStore.getEmergencyUnlockMinutes()
        val requestInstant = Instant.ofEpochMilli(requestEpochMs)
        val availableAt = requestInstant.plus(Duration.ofMinutes(waitMinutes.toLong()))

        val remainingSec = Duration.between(now, availableAt).seconds
        return if (remainingSec > 0) {
            EmergencyUnlockStatus.Waiting(
                requestTime = requestInstant,
                availableAt = availableAt,
                remainingSeconds = remainingSec
            )
        } else {
            EmergencyUnlockStatus.Available
        }
    }

    suspend fun requestUnlock(now: Instant = Instant.now()): Boolean {
        val enabled = settingsDataStore.getEmergencyUnlockEnabled()
        if (!enabled) return false

        settingsDataStore.setEmergencyUnlockRequestEpochMs(now.toEpochMilli())
        return true
    }

    suspend fun cancelUnlock() {
        settingsDataStore.setEmergencyUnlockRequestEpochMs(null)
    }

    suspend fun confirmUnlock(now: Instant = Instant.now()): Boolean {
        val status = getStatus(now)
        if (status !is EmergencyUnlockStatus.Available) {
            return false
        }

        // Legitimate unlock: disarm strict mode and reset request
        settingsDataStore.disarmStrictMode()
        val disarmed = StrictModeRules.independentState.copy(isArmed = false)
        StrictModeRules.independentState = disarmed
        settingsDataStore.setEmergencyUnlockRequestEpochMs(null)
        return true
    }
}

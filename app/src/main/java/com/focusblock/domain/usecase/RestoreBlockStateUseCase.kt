package com.focusblock.domain.usecase

import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreBlockStateUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val profileRepository: ProfileRepository
) {
    /**
     * Re-evaluates and restores block and strict-mode states upon device boot or app update.
     * If an armed strict term expired while the device was powered down, it disarms gracefully.
     */
    suspend fun restore(now: Instant = Instant.now()): StrictModeState {
        val strictState = settingsDataStore.getStrictModeState()

        if (strictState.isArmed) {
            val end = strictState.endInstant
            if (end != null && !now.isBefore(end)) {
                // Term completed while device was powered off
                settingsDataStore.disarmStrictMode()
                val disarmed = strictState.copy(isArmed = false)
                StrictModeRules.independentState = disarmed
                return disarmed
            } else {
                // Still within strict term; enforce strict state
                StrictModeRules.independentState = strictState
            }
        }

        // Warm up and query profile repository to ensure Room DB is accessible
        profileRepository.getAllProfiles().first()

        return strictState
    }
}

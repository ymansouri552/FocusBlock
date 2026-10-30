package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.repository.ProfileRepository
import java.time.Instant
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    sealed interface SaveResult {
        data class Success(val id: Long) : SaveResult
        data class Blocked(val reason: String) : SaveResult
    }

    suspend fun invoke(profile: Profile): Long = repository.insertProfile(profile)

    suspend fun update(profile: Profile): Int = repository.updateProfile(profile)

    /**
     * Whether saving [profile] is allowed under strict-mode rules.
     * While a strict profile is active only "additive" changes are permitted:
     * adding apps or extending the end time.
     */
    fun canSave(current: Profile, updated: Profile, now: Instant = Instant.now()): StrictModeRules.EditResult {
        if (current.id == 0) return StrictModeRules.EditResult(true, null)
        if (!StrictModeRules.isStrictActive(current, now)) return StrictModeRules.EditResult(true, null)
        val endBefore = ScheduleEngine.activeProfile(current, now)?.endInstant
        val endAfter = ScheduleEngine.activeProfile(updated, now)?.endInstant
        val scheduleAllowed = if (endBefore != null) {
            endAfter != null && !endAfter.isBefore(endBefore)
        } else {
            true
        }
        val allowed =
            updated.strict &&
                updated.enabled &&
                updated.blockedApps.containsAll(current.blockedApps) &&
                scheduleAllowed
        return if (allowed) StrictModeRules.EditResult(true, null)
        else StrictModeRules.EditResult(false, "Strict profile is active: only adding apps or extending the end time is allowed.")
    }
}

package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
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
        val endNotShortened = if (endBefore != null) {
            endAfter != null && !endAfter.isBefore(endBefore)
        } else {
            true
        }
        val schedulesNotWeakened = current.schedules.size == updated.schedules.size &&
            current.schedules.zip(updated.schedules).all { (before, after) -> scheduleNotWeakened(before, after) }
        val pauseNotExtended = when {
            updated.pausedUntilEpochMs == null -> true
            current.pausedUntilEpochMs == null -> false
            else -> updated.pausedUntilEpochMs <= current.pausedUntilEpochMs
        }

        val allowed =
            updated.strict &&
                updated.enabled &&
                updated.name == current.name &&
                updated.blockedApps.containsAll(current.blockedApps) &&
                endNotShortened &&
                schedulesNotWeakened &&
                pauseNotExtended
        return if (allowed) StrictModeRules.EditResult(true, null)
        else StrictModeRules.EditResult(false, "Strict profile is active: only adding apps or extending the end time is allowed.")
    }

    /** Everything except the end time must stay identical while a strict session is committed. */
    private fun scheduleNotWeakened(before: Schedule, after: Schedule): Boolean {
        if (before.type != after.type) return false
        if (before.mode != after.mode) return false
        if (before.limitScope != after.limitScope) return false
        if (before.daysOfWeek != after.daysOfWeek) return false
        if (before.startTime != after.startTime) return false
        if (before.startDateTime != after.startDateTime) return false
        if (before.quickStartInstant != after.quickStartInstant) return false
        if (after.usageLimitMinutes < before.usageLimitMinutes) return false
        if (after.durationMinutes < before.durationMinutes) return false
        if (before.endTime != null && (after.endTime == null || after.endTime.isBefore(before.endTime))) return false
        if (before.endDateTime != null && (after.endDateTime == null || after.endDateTime.isBefore(before.endDateTime))) return false
        return true
    }
}

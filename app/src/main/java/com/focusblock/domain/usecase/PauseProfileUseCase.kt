package com.focusblock.domain.usecase

import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Lifts a profile's blocking temporarily. The profile stays enabled; its pause deadline
 * is evaluated against the trusted clock on every blocking decision, so enforcement resumes
 * automatically once the pause expires.
 */
class PauseProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
    private val trustedClock: TrustedClock
) {
    sealed interface PauseResult {
        data class Success(val untilEpochMs: Long) : PauseResult
        data class Failure(val reason: String) : PauseResult
    }

    companion object {
        val DURATION_PRESETS_MINUTES = listOf(5, 10, 15, 30, 60)
    }

    suspend fun invoke(id: Int, durationMinutes: Int): PauseResult {
        val profile = repository.getProfileById(id).first()
            ?: return PauseResult.Failure("Profile not found")
        if (!profile.enabled) {
            return PauseResult.Failure("Enable the profile before pausing it")
        }

        val now = trustedClock.now()
        val canPause = StrictModeRules.canPause(profile, now)
        if (!canPause.allowed) {
            return PauseResult.Failure(canPause.reason ?: "Strict profile cannot be paused")
        }

        val durationMs = durationMinutes.coerceIn(1, 24 * 60).toLong() * 60_000L
        val untilEpochMs = now.toEpochMilli() + durationMs
        repository.updateProfile(profile.copy(pausedUntilEpochMs = untilEpochMs))
        return PauseResult.Success(untilEpochMs)
    }
}

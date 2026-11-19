package com.focusblock.domain.usecase

import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Ends a temporary pause immediately, restoring enforcement on the next blocking decision. */
class ResumeProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    sealed interface ResumeResult {
        data object Success : ResumeResult
        data class Failure(val reason: String) : ResumeResult
    }

    suspend fun invoke(id: Int): ResumeResult {
        val profile = repository.getProfileById(id).first()
            ?: return ResumeResult.Failure("Profile not found")
        if (profile.pausedUntilEpochMs == null) return ResumeResult.Success
        repository.updateProfile(profile.copy(pausedUntilEpochMs = null))
        return ResumeResult.Success
    }
}

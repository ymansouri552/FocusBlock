package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ToggleProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    
    /** Result of toggle operation */
    sealed interface ToggleResult {
        data class Success(val rowsAffected: Int) : ToggleResult
        data class Failure(val reason: String) : ToggleResult
    }

    suspend fun invoke(id: Int, enabled: Boolean): ToggleResult {
        // Get the profile to check strict mode rules
        val profile = repository.getProfileById(id).first() ?: return ToggleResult.Failure("Profile not found")
        
        // If trying to disable a profile, check strict mode rules
        if (!enabled) {
            val canDeactivate = StrictModeRules.canDeactivate(profile)
            if (!canDeactivate.allowed) {
                return ToggleResult.Failure(canDeactivate.reason ?: "Strict profile cannot be deactivated")
            }
        }
        
        val rowsAffected = repository.enableProfile(id, enabled)
        return ToggleResult.Success(rowsAffected)
    }
}
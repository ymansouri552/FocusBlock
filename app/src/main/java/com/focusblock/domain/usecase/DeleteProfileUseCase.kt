package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.repository.ProfileRepository
import javax.inject.Inject

class DeleteProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend fun invoke(profile: Profile): Int = repository.deleteProfile(profile)
}

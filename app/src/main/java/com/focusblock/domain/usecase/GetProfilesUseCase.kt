package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfilesUseCase @Inject constructor(private val repository: ProfileRepository) {
    fun invoke(): Flow<List<Profile>> = repository.getAllProfiles()
}
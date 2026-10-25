package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.repository.ProfileRepository
import com.focusblock.domain.model.ScheduleEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class BlockedAppsUseCase @Inject constructor(private val repository: ProfileRepository) {
    fun blockedPackagesNow(): Flow<Set<String>> =
        repository.getAllProfiles().map { profiles ->
            ScheduleEngine.blockedPackages(profiles)
        }
}
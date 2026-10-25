package com.focusblock.domain.repository

import com.focusblock.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<Profile>>
    fun getProfileById(id: Int): Flow<Profile?>
    suspend fun insertProfile(profile: Profile): Long
    suspend fun updateProfile(profile: Profile): Int
    suspend fun deleteProfile(profile: Profile): Int
    suspend fun editDisabled(profile: Profile): Int
    suspend fun enableProfile(id: Int, enabled: Boolean): Int
}

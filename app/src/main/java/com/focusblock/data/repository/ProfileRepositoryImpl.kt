package com.focusblock.data.repository

import com.focusblock.data.local.room.ProfileDao
import com.focusblock.domain.model.Profile
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl(private val dao: ProfileDao) : ProfileRepository {
    override fun getAllProfiles(): Flow<List<Profile>> = dao.getAllProfiles()
    override fun getProfileById(id: Int): Flow<Profile?> = dao.getProfileById(id)
    override suspend fun insertProfile(profile: Profile): Long = dao.insertProfile(profile)
    override suspend fun updateProfile(profile: Profile): Int = dao.updateProfile(profile)
    override suspend fun deleteProfile(profile: Profile): Int = dao.deleteProfile(profile)
    override suspend fun editDisabled(profile: Profile): Int = dao.updateProfile(profile.copy(enabled = false))
    override suspend fun enableProfile(id: Int, enabled: Boolean): Int = dao.enableProfile(id, enabled)
}

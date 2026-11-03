package com.focusblock.domain.repository

import com.focusblock.domain.model.AppUsageRecord
import kotlinx.coroutines.flow.Flow

interface AppUsageRepository {
    suspend fun recordUsage(profileId: Int, packageName: String, startTimeMs: Long, endTimeMs: Long)
    suspend fun getUsageForPackage(packageName: String, windowStartMs: Long, windowEndMs: Long): Long
    suspend fun getUsageForProfile(profileId: Int, windowStartMs: Long, windowEndMs: Long): Long
    suspend fun getUsageForPackages(packages: List<String>, windowStartMs: Long, windowEndMs: Long): Long
    suspend fun cleanupOldRecords(olderThanMs: Long)
    fun getRecentRecords(limit: Int = 100): Flow<List<AppUsageRecord>>
}

package com.focusblock.data.repository

import com.focusblock.data.local.room.AppUsageDao
import com.focusblock.domain.model.AppUsageRecord
import com.focusblock.domain.repository.AppUsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageRepositoryImpl @Inject constructor(
    private val appUsageDao: AppUsageDao
) : AppUsageRepository {

    override suspend fun recordUsage(
        profileId: Int,
        packageName: String,
        startTimeMs: Long,
        endTimeMs: Long
    ) {
        if (endTimeMs <= startTimeMs) return
        val durationMs = endTimeMs - startTimeMs
        appUsageDao.insert(
            AppUsageRecord(
                profileId = profileId,
                packageName = packageName,
                startTimeMs = startTimeMs,
                endTimeMs = endTimeMs,
                durationMs = durationMs
            )
        )
    }

    override suspend fun getUsageForPackage(
        packageName: String,
        windowStartMs: Long,
        windowEndMs: Long
    ): Long {
        return appUsageDao.getUsageForPackage(packageName, windowStartMs, windowEndMs)
    }

    override suspend fun getUsageForProfile(
        profileId: Int,
        windowStartMs: Long,
        windowEndMs: Long
    ): Long {
        return appUsageDao.getUsageForProfile(profileId, windowStartMs, windowEndMs)
    }

    override suspend fun getUsageForPackages(
        packages: List<String>,
        windowStartMs: Long,
        windowEndMs: Long
    ): Long {
        if (packages.isEmpty()) return 0L
        return appUsageDao.getUsageForPackages(packages, windowStartMs, windowEndMs)
    }

    override suspend fun cleanupOldRecords(olderThanMs: Long) {
        appUsageDao.deleteOldRecords(olderThanMs)
    }

    override fun getRecentRecords(limit: Int): Flow<List<AppUsageRecord>> {
        return appUsageDao.getRecentRecords(limit)
    }
}

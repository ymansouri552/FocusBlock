package com.focusblock.domain.repository

import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.UsageStatsOverview
import java.time.LocalDate

interface UsageStatsRepository {
    fun hasUsagePermission(): Boolean
    suspend fun getDailyStats(date: LocalDate): UsageStatsOverview
    suspend fun getWeeklyStats(dateInWeek: LocalDate): UsageStatsOverview
    suspend fun getAppUsageForPeriod(packageName: String, startMs: Long, endMs: Long): AppUsageItem?
    suspend fun getUsageDuration(packageName: String, startMs: Long, endMs: Long): Long
    suspend fun getCombinedUsageDuration(packages: Set<String>, startMs: Long, endMs: Long): Long
}

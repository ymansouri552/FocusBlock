package com.focusblock.domain.usecase

import com.focusblock.domain.model.AppSortOrder
import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.StatsPeriod
import com.focusblock.domain.model.UsageStatsOverview
import com.focusblock.domain.repository.UsageStatsRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetUsageStatsUseCase @Inject constructor(
    private val repository: UsageStatsRepository
) {
    fun hasPermission(): Boolean {
        return repository.hasUsagePermission()
    }

    suspend fun getStats(
        period: StatsPeriod,
        targetDate: LocalDate = LocalDate.now(),
        sortOrder: AppSortOrder = AppSortOrder.MOST_USED,
        searchQuery: String = ""
    ): UsageStatsOverview {
        val overview = when (period) {
            StatsPeriod.DAILY -> repository.getDailyStats(targetDate)
            StatsPeriod.WEEKLY -> repository.getWeeklyStats(targetDate)
        }

        val filteredApps = filterAndSortApps(
            apps = overview.appUsageList,
            sortOrder = sortOrder,
            query = searchQuery
        )

        return overview.copy(appUsageList = filteredApps)
    }

    private fun filterAndSortApps(
        apps: List<AppUsageItem>,
        sortOrder: AppSortOrder,
        query: String
    ): List<AppUsageItem> {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(trimmed, ignoreCase = true) ||
                it.packageName.contains(trimmed, ignoreCase = true)
            }
        }

        return when (sortOrder) {
            AppSortOrder.MOST_USED -> filtered.sortedByDescending { it.totalForegroundMs }
            AppSortOrder.MOST_LAUNCHED -> filtered.sortedByDescending { it.launchCount }
            AppSortOrder.NAME -> filtered.sortedBy { it.appName.lowercase() }
        }
    }
}

package com.focusblock.domain.model

import java.time.LocalDate

enum class StatsPeriod {
    DAILY,
    WEEKLY
}

enum class AppSortOrder {
    MOST_USED,
    MOST_LAUNCHED,
    NAME
}

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val totalForegroundMs: Long,
    val launchCount: Int = 0,
    val lastTimeUsedMs: Long = 0L,
    val usagePercentage: Float = 0f,
    val isBlocked: Boolean = false,
    val profileNames: List<String> = emptyList()
) {
    val formattedDuration: String
        get() = formatDuration(totalForegroundMs)

    companion object {
        fun formatDuration(durationMs: Long): String {
            if (durationMs <= 0L) return "0m"
            val totalSeconds = durationMs / 1000L
            val hours = totalSeconds / 3600L
            val minutes = (totalSeconds % 3600L) / 60L
            return when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "< 1m"
            }
        }
    }
}

data class DailyUsageBucket(
    val slotIndex: Int,
    val hourLabel: String,
    val durationMs: Long,
    val isCurrent: Boolean = false
) {
    val formattedDuration: String
        get() = AppUsageItem.formatDuration(durationMs)
}

data class WeeklyDayBucket(
    val date: LocalDate,
    val dayLabel: String,
    val durationMs: Long,
    val isToday: Boolean = false,
    val isSelected: Boolean = false
) {
    val formattedDuration: String
        get() = AppUsageItem.formatDuration(durationMs)
}

data class UsageStatsOverview(
    val period: StatsPeriod = StatsPeriod.DAILY,
    val selectedDate: LocalDate = LocalDate.now(),
    val startDate: LocalDate = selectedDate,
    val endDate: LocalDate = selectedDate,
    val totalScreenTimeMs: Long = 0L,
    val dailyAverageMs: Long = 0L,
    val mostUsedApp: AppUsageItem? = null,
    val peakTimeLabel: String = "",
    val totalLaunches: Int = 0,
    val appsUsedCount: Int = 0,
    val hourlyBuckets: List<DailyUsageBucket> = emptyList(),
    val weeklyBuckets: List<WeeklyDayBucket> = emptyList(),
    val appUsageList: List<AppUsageItem> = emptyList(),
    val hasPermission: Boolean = false
) {
    val formattedTotalTime: String
        get() = AppUsageItem.formatDuration(totalScreenTimeMs)

    val formattedDailyAverage: String
        get() = AppUsageItem.formatDuration(dailyAverageMs)
}

package com.focusblock.domain.usecase

import com.focusblock.domain.model.AppSortOrder
import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.DailyUsageBucket
import com.focusblock.domain.model.StatsPeriod
import com.focusblock.domain.model.UsageStatsOverview
import com.focusblock.domain.model.WeeklyDayBucket
import com.focusblock.domain.repository.UsageStatsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetUsageStatsUseCaseTest {

    private lateinit var fakeRepository: FakeUsageStatsRepository
    private lateinit var useCase: GetUsageStatsUseCase

    private val sampleApps = listOf(
        AppUsageItem(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            totalForegroundMs = 3600_000L, // 1 hour
            launchCount = 12,
            isBlocked = true
        ),
        AppUsageItem(
            packageName = "com.instagram.android",
            appName = "Instagram",
            totalForegroundMs = 5400_000L, // 1.5 hours
            launchCount = 35,
            isBlocked = false
        ),
        AppUsageItem(
            packageName = "com.android.chrome",
            appName = "Chrome",
            totalForegroundMs = 1800_000L, // 30 minutes
            launchCount = 20,
            isBlocked = false
        )
    )

    @Before
    fun setUp() {
        fakeRepository = FakeUsageStatsRepository(sampleApps)
        useCase = GetUsageStatsUseCase(fakeRepository)
    }

    @Test
    fun hasPermission_delegatesToRepository() {
        fakeRepository.permissionGranted = true
        assertTrue(useCase.hasPermission())

        fakeRepository.permissionGranted = false
        assertFalse(useCase.hasPermission())
    }

    @Test
    fun getStats_daily_returnsDailyOverview() = runTest {
        val today = LocalDate.of(2026, 11, 5)
        val result = useCase.getStats(
            period = StatsPeriod.DAILY,
            targetDate = today,
            sortOrder = AppSortOrder.MOST_USED
        )

        assertEquals(StatsPeriod.DAILY, result.period)
        assertEquals(today, result.selectedDate)
        assertEquals(3, result.appUsageList.size)
        // Most used first: Instagram (5400s), YouTube (3600s), Chrome (1800s)
        assertEquals("Instagram", result.appUsageList[0].appName)
        assertEquals("YouTube", result.appUsageList[1].appName)
        assertEquals("Chrome", result.appUsageList[2].appName)
    }

    @Test
    fun getStats_sortByMostLaunched_ordersCorrectly() = runTest {
        val result = useCase.getStats(
            period = StatsPeriod.DAILY,
            sortOrder = AppSortOrder.MOST_LAUNCHED
        )

        // Most launched: Instagram (35), Chrome (20), YouTube (12)
        assertEquals("Instagram", result.appUsageList[0].appName)
        assertEquals("Chrome", result.appUsageList[1].appName)
        assertEquals("YouTube", result.appUsageList[2].appName)
    }

    @Test
    fun getStats_sortByName_ordersAlphabetically() = runTest {
        val result = useCase.getStats(
            period = StatsPeriod.DAILY,
            sortOrder = AppSortOrder.NAME
        )

        // Alphabetical: Chrome, Instagram, YouTube
        assertEquals("Chrome", result.appUsageList[0].appName)
        assertEquals("Instagram", result.appUsageList[1].appName)
        assertEquals("YouTube", result.appUsageList[2].appName)
    }

    @Test
    fun getStats_searchQuery_filtersMatchingApps() = runTest {
        val result = useCase.getStats(
            period = StatsPeriod.DAILY,
            searchQuery = "you"
        )

        assertEquals(1, result.appUsageList.size)
        assertEquals("YouTube", result.appUsageList[0].appName)
    }

    @Test
    fun getStats_searchQueryByPackage_filtersMatchingApps() = runTest {
        val result = useCase.getStats(
            period = StatsPeriod.DAILY,
            searchQuery = "chrome"
        )

        assertEquals(1, result.appUsageList.size)
        assertEquals("Chrome", result.appUsageList[0].appName)
    }

    @Test
    fun getStats_searchQueryNonExistent_returnsEmptyList() = runTest {
        val result = useCase.getStats(
            period = StatsPeriod.DAILY,
            searchQuery = "NonExistentAppXYZ"
        )

        assertTrue(result.appUsageList.isEmpty())
    }

    @Test
    fun getStats_weekly_returnsWeeklyOverview() = runTest {
        val date = LocalDate.of(2026, 11, 5)
        val result = useCase.getStats(
            period = StatsPeriod.WEEKLY,
            targetDate = date
        )

        assertEquals(StatsPeriod.WEEKLY, result.period)
        assertNotNull(result.weeklyBuckets)
    }

    private class FakeUsageStatsRepository(
        private val apps: List<AppUsageItem>
    ) : UsageStatsRepository {
        var permissionGranted = true

        override fun hasUsagePermission(): Boolean = permissionGranted

        override suspend fun getDailyStats(date: LocalDate): UsageStatsOverview {
            return UsageStatsOverview(
                period = StatsPeriod.DAILY,
                selectedDate = date,
                totalScreenTimeMs = apps.sumOf { it.totalForegroundMs },
                dailyAverageMs = apps.sumOf { it.totalForegroundMs },
                appUsageList = apps,
                appsUsedCount = apps.size,
                hasPermission = permissionGranted
            )
        }

        override suspend fun getWeeklyStats(dateInWeek: LocalDate): UsageStatsOverview {
            return UsageStatsOverview(
                period = StatsPeriod.WEEKLY,
                selectedDate = dateInWeek,
                totalScreenTimeMs = apps.sumOf { it.totalForegroundMs } * 7,
                dailyAverageMs = apps.sumOf { it.totalForegroundMs },
                weeklyBuckets = (0..6).map {
                    WeeklyDayBucket(
                        date = dateInWeek.plusDays(it.toLong()),
                        dayLabel = "Day$it",
                        durationMs = 3600_000L
                    )
                },
                appUsageList = apps,
                appsUsedCount = apps.size,
                hasPermission = permissionGranted
            )
        }

        override suspend fun getAppUsageForPeriod(
            packageName: String,
            startMs: Long,
            endMs: Long
        ): AppUsageItem? {
            return apps.firstOrNull { it.packageName == packageName }
        }

        override suspend fun getUsageDuration(packageName: String, startMs: Long, endMs: Long): Long {
            return apps.firstOrNull { it.packageName == packageName }?.totalForegroundMs ?: 0L
        }

        override suspend fun getCombinedUsageDuration(packages: Set<String>, startMs: Long, endMs: Long): Long {
            return apps.filter { packages.contains(it.packageName) }.sumOf { it.totalForegroundMs }
        }
    }
}

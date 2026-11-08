package com.focusblock.domain.usecase

import com.focusblock.domain.model.ActiveProfileInfo
import com.focusblock.domain.model.AppUsageRecord
import com.focusblock.domain.model.LimitScope
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.repository.AppUsageRepository
import com.focusblock.domain.repository.UsageStatsRepository
import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.UsageStatsOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeAppUsageRepository : AppUsageRepository {
    val records = mutableListOf<AppUsageRecord>()

    override suspend fun recordUsage(
        profileId: Int,
        packageName: String,
        startTimeMs: Long,
        endTimeMs: Long
    ) {
        records.add(
            AppUsageRecord(
                id = records.size.toLong() + 1,
                profileId = profileId,
                packageName = packageName,
                startTimeMs = startTimeMs,
                endTimeMs = endTimeMs,
                durationMs = endTimeMs - startTimeMs
            )
        )
    }

    override suspend fun getUsageForPackage(
        packageName: String,
        windowStartMs: Long,
        windowEndMs: Long
    ): Long {
        return records.filter { it.packageName == packageName && it.endTimeMs > windowStartMs && it.startTimeMs < windowEndMs }
            .sumOf { rec ->
                val start = maxOf(rec.startTimeMs, windowStartMs)
                val end = minOf(rec.endTimeMs, windowEndMs)
                (end - start).coerceAtLeast(0L)
            }
    }

    override suspend fun getUsageForProfile(
        profileId: Int,
        windowStartMs: Long,
        windowEndMs: Long
    ): Long {
        return records.filter { it.profileId == profileId && it.endTimeMs > windowStartMs && it.startTimeMs < windowEndMs }
            .sumOf { rec ->
                val start = maxOf(rec.startTimeMs, windowStartMs)
                val end = minOf(rec.endTimeMs, windowEndMs)
                (end - start).coerceAtLeast(0L)
            }
    }

    override suspend fun getUsageForPackages(
        packages: List<String>,
        windowStartMs: Long,
        windowEndMs: Long
    ): Long {
        return records.filter { it.packageName in packages && it.endTimeMs > windowStartMs && it.startTimeMs < windowEndMs }
            .sumOf { rec ->
                val start = maxOf(rec.startTimeMs, windowStartMs)
                val end = minOf(rec.endTimeMs, windowEndMs)
                (end - start).coerceAtLeast(0L)
            }
    }

    override suspend fun cleanupOldRecords(olderThanMs: Long) {
        records.removeAll { it.endTimeMs < olderThanMs }
    }

    override fun getRecentRecords(limit: Int): Flow<List<AppUsageRecord>> {
        return flowOf(records.takeLast(limit))
    }
}

class AppUsageTrackerTest {

    private val fakeRepo = FakeAppUsageRepository()
    private val tracker = AppUsageTracker(fakeRepo)

    @Test
    fun app_within_limit_is_not_exceeded() = runBlocking {
        val now = Instant.now()
        val windowStart = now.minus(2, ChronoUnit.HOURS)
        val windowEnd = now.plus(2, ChronoUnit.HOURS)

        // 10 minutes used out of 15 minutes limit
        fakeRepo.recordUsage(
            profileId = 1,
            packageName = "com.google.android.youtube",
            startTimeMs = windowStart.plus(10, ChronoUnit.MINUTES).toEpochMilli(),
            endTimeMs = windowStart.plus(20, ChronoUnit.MINUTES).toEpochMilli()
        )

        val schedule = Schedule(
            type = ScheduleType.RECURRING,
            mode = ScheduleMode.USAGE_LIMIT,
            usageLimitMinutes = 15,
            limitScope = LimitScope.PER_APP,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(12, 0)
        )
        val profile = Profile(
            id = 1,
            name = "Media",
            blockedApps = setOf("com.google.android.youtube"),
            enabled = true,
            schedules = listOf(schedule)
        )
        val activeInfo = ActiveProfileInfo(
            profile = profile,
            schedule = schedule,
            startInstant = windowStart,
            endInstant = windowEnd
        )

        val status = tracker.evaluateUsageLimit("com.google.android.youtube", listOf(activeInfo), now)
        assertNotNull(status)
        assertFalse(status.isExceeded)
        assertEquals(15 * 60 * 1000L, status.limitMs)
        assertEquals(10 * 60 * 1000L, status.usedMs)
        assertEquals(5 * 60 * 1000L, status.remainingQuotaMs)
    }

    @Test
    fun app_exceeding_limit_is_exceeded() = runBlocking {
        val now = Instant.now()
        val windowStart = now.minus(2, ChronoUnit.HOURS)
        val windowEnd = now.plus(2, ChronoUnit.HOURS)

        // 16 minutes used out of 15 minutes limit
        fakeRepo.recordUsage(
            profileId = 1,
            packageName = "com.supercell.clashofclans",
            startTimeMs = windowStart.plus(10, ChronoUnit.MINUTES).toEpochMilli(),
            endTimeMs = windowStart.plus(26, ChronoUnit.MINUTES).toEpochMilli()
        )

        val schedule = Schedule(
            type = ScheduleType.RECURRING,
            mode = ScheduleMode.USAGE_LIMIT,
            usageLimitMinutes = 15,
            limitScope = LimitScope.PER_APP
        )
        val profile = Profile(
            id = 1,
            name = "Games",
            blockedApps = setOf("com.supercell.clashofclans"),
            enabled = true,
            schedules = listOf(schedule)
        )
        val activeInfo = ActiveProfileInfo(
            profile = profile,
            schedule = schedule,
            startInstant = windowStart,
            endInstant = windowEnd
        )

        val status = tracker.evaluateUsageLimit("com.supercell.clashofclans", listOf(activeInfo), now)
        assertNotNull(status)
        assertTrue(status.isExceeded)
        assertEquals(0L, status.remainingQuotaMs)
    }

    @Test
    fun combined_scope_pools_usage_across_apps() = runBlocking {
        val now = Instant.now()
        val windowStart = now.minus(1, ChronoUnit.HOURS)
        val windowEnd = now.plus(1, ChronoUnit.HOURS)

        // YouTube: 8 minutes
        fakeRepo.recordUsage(
            profileId = 1,
            packageName = "com.google.android.youtube",
            startTimeMs = windowStart.plus(5, ChronoUnit.MINUTES).toEpochMilli(),
            endTimeMs = windowStart.plus(13, ChronoUnit.MINUTES).toEpochMilli()
        )
        // Clash of Clans: 8 minutes
        fakeRepo.recordUsage(
            profileId = 1,
            packageName = "com.supercell.clashofclans",
            startTimeMs = windowStart.plus(20, ChronoUnit.MINUTES).toEpochMilli(),
            endTimeMs = windowStart.plus(28, ChronoUnit.MINUTES).toEpochMilli()
        )
        // Total combined = 16 minutes > 15 minutes limit!

        val schedule = Schedule(
            type = ScheduleType.HOURLY,
            mode = ScheduleMode.USAGE_LIMIT,
            usageLimitMinutes = 15,
            limitScope = LimitScope.COMBINED
        )
        val profile = Profile(
            id = 1,
            name = "Distractions",
            blockedApps = setOf("com.google.android.youtube", "com.supercell.clashofclans"),
            enabled = true,
            schedules = listOf(schedule)
        )
        val activeInfo = ActiveProfileInfo(
            profile = profile,
            schedule = schedule,
            startInstant = windowStart,
            endInstant = windowEnd
        )

        val statusYouTube = tracker.evaluateUsageLimit("com.google.android.youtube", listOf(activeInfo), now)
        assertNotNull(statusYouTube)
        assertTrue(statusYouTube.isExceeded)

        val statusClash = tracker.evaluateUsageLimit("com.supercell.clashofclans", listOf(activeInfo), now)
        assertNotNull(statusClash)
        assertTrue(statusClash.isExceeded)
    }

    @Test
    fun unmonitored_app_returns_null() = runBlocking {
        val now = Instant.now()
        val schedule = Schedule(
            type = ScheduleType.DAILY,
            mode = ScheduleMode.USAGE_LIMIT,
            usageLimitMinutes = 15
        )
        val profile = Profile(
            id = 1,
            name = "Test",
            blockedApps = setOf("com.app.a"),
            enabled = true,
            schedules = listOf(schedule)
        )
        val activeInfo = ActiveProfileInfo(
            profile = profile,
            schedule = schedule,
            startInstant = now.minus(1, ChronoUnit.HOURS),
            endInstant = now.plus(1, ChronoUnit.HOURS)
        )

        val status = tracker.evaluateUsageLimit("com.app.unmonitored", listOf(activeInfo), now)
        assertNull(status)
    }

    @Test
    fun actual_system_usage_exceeding_limit_blocks_app_even_if_app_usage_repo_is_empty() = runBlocking {
        val now = Instant.now()
        val windowStart = now.minus(2, ChronoUnit.HOURS)
        val windowEnd = now.plus(2, ChronoUnit.HOURS)

        // Local repository has 0 records, but system UsageStats reports 45 minutes used
        val emptyLocalRepo = FakeAppUsageRepository()
        val fakeUsageStatsRepo = FakeSystemUsageStatsRepository().apply {
            setDuration("com.supercell.clashofclans", 45 * 60 * 1000L)
        }
        val systemAwareTracker = AppUsageTracker(emptyLocalRepo, fakeUsageStatsRepo)

        val schedule = Schedule(
            type = ScheduleType.DAILY,
            mode = ScheduleMode.USAGE_LIMIT,
            usageLimitMinutes = 30,
            limitScope = LimitScope.PER_APP
        )
        val profile = Profile(
            id = 2,
            name = "Gaming",
            blockedApps = setOf("com.supercell.clashofclans"),
            enabled = true,
            schedules = listOf(schedule)
        )
        val activeInfo = ActiveProfileInfo(
            profile = profile,
            schedule = schedule,
            startInstant = windowStart,
            endInstant = windowEnd
        )

        val status = systemAwareTracker.evaluateUsageLimit("com.supercell.clashofclans", listOf(activeInfo), now)
        assertNotNull(status)
        assertTrue(status.isExceeded)
        assertEquals(30 * 60 * 1000L, status.limitMs)
        assertEquals(45 * 60 * 1000L, status.usedMs)
        assertEquals(0L, status.remainingQuotaMs)
    }

    @Test
    fun combined_scope_uses_actual_system_combined_usage() = runBlocking {
        val now = Instant.now()
        val windowStart = now.minus(2, ChronoUnit.HOURS)
        val windowEnd = now.plus(2, ChronoUnit.HOURS)

        val emptyLocalRepo = FakeAppUsageRepository()
        val fakeUsageStatsRepo = FakeSystemUsageStatsRepository().apply {
            setDuration("com.instagram.android", 15 * 60 * 1000L)
            setDuration("com.zhiliaoapp.musically", 20 * 60 * 1000L)
        }
        val systemAwareTracker = AppUsageTracker(emptyLocalRepo, fakeUsageStatsRepo)

        val schedule = Schedule(
            type = ScheduleType.DAILY,
            mode = ScheduleMode.USAGE_LIMIT,
            usageLimitMinutes = 30, // 30 min limit, 35 min combined usage
            limitScope = LimitScope.COMBINED
        )
        val profile = Profile(
            id = 3,
            name = "Social",
            blockedApps = setOf("com.instagram.android", "com.zhiliaoapp.musically"),
            enabled = true,
            schedules = listOf(schedule)
        )
        val activeInfo = ActiveProfileInfo(
            profile = profile,
            schedule = schedule,
            startInstant = windowStart,
            endInstant = windowEnd
        )

        val instagramStatus = systemAwareTracker.evaluateUsageLimit("com.instagram.android", listOf(activeInfo), now)
        assertNotNull(instagramStatus)
        assertTrue(instagramStatus.isExceeded)
        assertEquals(35 * 60 * 1000L, instagramStatus.usedMs)

        val tiktokStatus = systemAwareTracker.evaluateUsageLimit("com.zhiliaoapp.musically", listOf(activeInfo), now)
        assertNotNull(tiktokStatus)
        assertTrue(tiktokStatus.isExceeded)
        assertEquals(35 * 60 * 1000L, tiktokStatus.usedMs)
    }
}

class FakeSystemUsageStatsRepository : UsageStatsRepository {
    private val durations = mutableMapOf<String, Long>()
    var permission: Boolean = true

    fun setDuration(pkg: String, ms: Long) {
        durations[pkg] = ms
    }

    override fun hasUsagePermission(): Boolean = permission

    override suspend fun getDailyStats(date: java.time.LocalDate): UsageStatsOverview {
        return UsageStatsOverview(
            period = com.focusblock.domain.model.StatsPeriod.DAILY,
            selectedDate = date,
            totalScreenTimeMs = durations.values.sum(),
            dailyAverageMs = durations.values.sum(),
            hasPermission = permission
        )
    }

    override suspend fun getWeeklyStats(dateInWeek: java.time.LocalDate): UsageStatsOverview {
        return UsageStatsOverview(
            period = com.focusblock.domain.model.StatsPeriod.WEEKLY,
            selectedDate = dateInWeek,
            totalScreenTimeMs = durations.values.sum(),
            dailyAverageMs = durations.values.sum(),
            hasPermission = permission
        )
    }

    override suspend fun getAppUsageForPeriod(packageName: String, startMs: Long, endMs: Long): AppUsageItem? = null

    override suspend fun getUsageDuration(packageName: String, startMs: Long, endMs: Long): Long {
        return durations[packageName] ?: 0L
    }

    override suspend fun getCombinedUsageDuration(packages: Set<String>, startMs: Long, endMs: Long): Long {
        return packages.sumOf { durations[it] ?: 0L }
    }
}

package com.focusblock.domain.usecase

import com.focusblock.domain.model.BlockLog
import com.focusblock.domain.repository.BlockLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetBlockStatsUseCaseTest {

    private class FakeBlockLogRepository : BlockLogRepository {
        val logs = mutableListOf<BlockLog>()
        private val _flow = MutableStateFlow<List<BlockLog>>(logs)

        fun emit(newLogs: List<BlockLog>) {
            logs.clear()
            logs.addAll(newLogs)
            _flow.value = ArrayList(logs)
        }

        override fun insert(log: BlockLog): Long {
            logs.add(log)
            _flow.value = ArrayList(logs)
            return log.id.toLong()
        }

        override fun getLogsForDay(day: Long): Flow<List<BlockLog>> = _flow.asStateFlow()
        override fun getLogsForPackage(packageName: String): Flow<List<BlockLog>> = _flow.asStateFlow()
        override fun getAllLogs(): Flow<List<BlockLog>> = _flow.asStateFlow()
        override fun clearAllLogs(): Int {
            val count = logs.size
            logs.clear()
            _flow.value = emptyList()
            return count
        }
    }

    private lateinit var fakeRepo: FakeBlockLogRepository
    private lateinit var useCase: GetBlockStatsUseCase

    @BeforeTest
    fun setup() {
        fakeRepo = FakeBlockLogRepository()
        useCase = GetBlockStatsUseCase(fakeRepo)
    }

    @Test
    fun computes_block_stats_aggregating_today_and_top_apps() = runBlocking {
        val zone = ZoneId.of("UTC")
        val now = Instant.parse("2026-10-30T14:00:00Z")
        val todayEarly = Instant.parse("2026-10-30T02:00:00Z").toEpochMilli()
        val yesterday = Instant.parse("2026-10-29T20:00:00Z").toEpochMilli()

        val sampleLogs = listOf(
            BlockLog(id = 1, profileId = 1, packageName = "com.instagram.android", appName = "Instagram", timestamp = todayEarly),
            BlockLog(id = 2, profileId = 1, packageName = "com.instagram.android", appName = "Instagram", timestamp = todayEarly + 1000),
            BlockLog(id = 3, profileId = 1, packageName = "com.twitter.android", appName = "X", timestamp = todayEarly + 2000),
            BlockLog(id = 4, profileId = 1, packageName = "com.twitter.android", appName = "X", timestamp = yesterday),
            BlockLog(id = 5, profileId = 1, packageName = "com.reddit.frontpage", appName = "Reddit", timestamp = yesterday)
        )
        fakeRepo.emit(sampleLogs)

        val stats = useCase.getStats(now, zone).first()

        // 3 logs are from today (2 Instagram + 1 X)
        assertEquals(3, stats.totalBlocksToday)
        // 5 logs all-time
        assertEquals(5, stats.totalBlocksAllTime)

        // Top apps should have Instagram (2) and X (2)
        assertEquals(3, stats.topBlockedApps.size)
        val topApp = stats.topBlockedApps.first()
        assertEquals(2, topApp.count)
    }
}

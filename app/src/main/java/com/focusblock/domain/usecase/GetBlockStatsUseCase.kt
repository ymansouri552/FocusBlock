package com.focusblock.domain.usecase

import com.focusblock.domain.model.BlockLog
import com.focusblock.domain.repository.BlockLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class BlockStats(
    val totalBlocksToday: Int = 0,
    val totalBlocksAllTime: Int = 0,
    val topBlockedApps: List<AppBlockCount> = emptyList(),
    val recentLogs: List<BlockLog> = emptyList()
)

data class AppBlockCount(
    val packageName: String,
    val appName: String,
    val count: Int
)

@Singleton
class GetBlockStatsUseCase @Inject constructor(
    private val blockLogRepository: BlockLogRepository
) {
    fun getStats(now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): Flow<BlockStats> {
        val startOfToday = now.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

        return blockLogRepository.getAllLogs().map { allLogs ->
            val todayLogs = allLogs.filter { it.timestamp >= startOfToday }
            val topApps = allLogs
                .groupBy { it.packageName }
                .map { (pkg, logs) ->
                    val appName = logs.firstOrNull()?.appName ?: pkg.substringAfterLast('.')
                    AppBlockCount(packageName = pkg, appName = appName, count = logs.size)
                }
                .sortedByDescending { it.count }
                .take(5)

            BlockStats(
                totalBlocksToday = todayLogs.size,
                totalBlocksAllTime = allLogs.size,
                topBlockedApps = topApps,
                recentLogs = allLogs.sortedByDescending { it.timestamp }.take(20)
            )
        }
    }
}

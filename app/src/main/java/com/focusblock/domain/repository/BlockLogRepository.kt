package com.focusblock.domain.repository

import com.focusblock.domain.model.BlockLog
import kotlinx.coroutines.flow.Flow

interface BlockLogRepository {
    fun insert(log: BlockLog): Long
    fun getLogsForDay(day: Long): Flow<List<BlockLog>>
    fun getLogsForPackage(packageName: String): Flow<List<BlockLog>>
    fun getAllLogs(): Flow<List<BlockLog>>
    fun clearAllLogs(): Int
}
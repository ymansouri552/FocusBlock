package com.focusblock.data.repository

import com.focusblock.domain.model.BlockLog
import com.focusblock.domain.repository.BlockLogRepository
import com.focusblock.data.local.room.BlockLogDao
import kotlinx.coroutines.flow.Flow

class BlockLogRepositoryImpl(private val dao: BlockLogDao) : BlockLogRepository {
    override fun insert(log: BlockLog): Long = dao.insert(log)
    override fun getLogsForDay(day: Long): Flow<List<BlockLog>> = dao.getLogsForDay(day)
    override fun getLogsForPackage(packageName: String): Flow<List<BlockLog>> = dao.getLogsForPackage(packageName)
    override fun getAllLogs(): Flow<List<BlockLog>> = dao.getAllLogs()
    override fun clearAllLogs(): Int = dao.clearAllLogs()
}
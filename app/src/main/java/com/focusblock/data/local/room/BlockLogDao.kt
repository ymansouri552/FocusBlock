package com.focusblock.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusblock.domain.model.BlockLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(log: BlockLog): Long

    @Query("SELECT * FROM block_log WHERE date(timestamp / 1000, 'unixepoch') = date(:day / 1000, 'unixepoch') ORDER BY timestamp DESC")
    fun getLogsForDay(day: Long): Flow<List<BlockLog>>

    @Query("SELECT * FROM block_log WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getLogsForPackage(packageName: String): Flow<List<BlockLog>>

    @Query("SELECT * FROM block_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<BlockLog>>

    @Query("DELETE FROM block_log")
    fun clearAllLogs(): Int
}
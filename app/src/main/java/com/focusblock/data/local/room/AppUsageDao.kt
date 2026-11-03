package com.focusblock.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusblock.domain.model.AppUsageRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AppUsageRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AppUsageRecord>)

    /**
     * Total milliseconds a package was used that overlap [windowStartMs, windowEndMs].
     */
    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN endTimeMs > :windowEndMs AND startTimeMs < :windowStartMs THEN (:windowEndMs - :windowStartMs)
                WHEN endTimeMs > :windowEndMs THEN (:windowEndMs - startTimeMs)
                WHEN startTimeMs < :windowStartMs THEN (endTimeMs - :windowStartMs)
                ELSE durationMs
            END
        ), 0)
        FROM app_usage_records
        WHERE packageName = :packageName
          AND endTimeMs > :windowStartMs
          AND startTimeMs < :windowEndMs
    """)
    suspend fun getUsageForPackage(packageName: String, windowStartMs: Long, windowEndMs: Long): Long

    /**
     * Total milliseconds all packages in a profile were used that overlap [windowStartMs, windowEndMs].
     */
    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN endTimeMs > :windowEndMs AND startTimeMs < :windowStartMs THEN (:windowEndMs - :windowStartMs)
                WHEN endTimeMs > :windowEndMs THEN (:windowEndMs - startTimeMs)
                WHEN startTimeMs < :windowStartMs THEN (endTimeMs - :windowStartMs)
                ELSE durationMs
            END
        ), 0)
        FROM app_usage_records
        WHERE profileId = :profileId
          AND endTimeMs > :windowStartMs
          AND startTimeMs < :windowEndMs
    """)
    suspend fun getUsageForProfile(profileId: Int, windowStartMs: Long, windowEndMs: Long): Long

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN endTimeMs > :windowEndMs AND startTimeMs < :windowStartMs THEN (:windowEndMs - :windowStartMs)
                WHEN endTimeMs > :windowEndMs THEN (:windowEndMs - startTimeMs)
                WHEN startTimeMs < :windowStartMs THEN (endTimeMs - :windowStartMs)
                ELSE durationMs
            END
        ), 0)
        FROM app_usage_records
        WHERE packageName IN (:packages)
          AND endTimeMs > :windowStartMs
          AND startTimeMs < :windowEndMs
    """)
    suspend fun getUsageForPackages(packages: List<String>, windowStartMs: Long, windowEndMs: Long): Long

    @Query("DELETE FROM app_usage_records WHERE endTimeMs < :olderThanMs")
    suspend fun deleteOldRecords(olderThanMs: Long)

    @Query("SELECT * FROM app_usage_records ORDER BY startTimeMs DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 100): Flow<List<AppUsageRecord>>
}

package com.focusblock.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_usage_records",
    indices = [
        Index(value = ["packageName", "startTimeMs"]),
        Index(value = ["profileId", "startTimeMs"]),
        Index(value = ["endTimeMs"])
    ]
)
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Int = 0,
    val packageName: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long
)

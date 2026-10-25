package com.focusblock.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "block_log",
    foreignKeys = [ForeignKey(entity = Profile::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("timestamp"), Index("profileId")]
)
data class BlockLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val packageName: String,
    val appName: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val blocked: Boolean = true
)
package com.focusblock.domain.model

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    @TypeConverters(ProfileConverters::class)
    val blockedApps: Set<String> = emptySet(),
    val strict: Boolean = false,
    val enabled: Boolean = false,
    val createdAt: Long = Date().time,
    @TypeConverters(ProfileConverters::class)
    val schedules: List<Schedule> = emptyList()
)

object ProfileConverters {
    @TypeConverter
    @JvmStatic
    fun blockedAppsToString(apps: Set<String>?): String? =
        apps?.joinToString(",") { it }

    @TypeConverter
    @JvmStatic
    fun blockedAppsFromString(value: String?): Set<String> =
        value?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()

    @TypeConverter
    @JvmStatic
    fun schedulesToString(schedules: List<Schedule>?): String? =
        schedules?.joinToString("|") { it.toStorageString() }

    @TypeConverter
    @JvmStatic
    fun schedulesFromString(value: String?): List<Schedule> =
        value?.split("|")?.filter { it.isNotBlank() }?.mapNotNull { Schedule.fromStorageString(it) } ?: emptyList()
}
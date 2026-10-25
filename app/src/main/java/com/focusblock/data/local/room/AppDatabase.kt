package com.focusblock.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.focusblock.domain.model.BlockLog
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ProfileConverters

@Database(entities = [Profile::class, BlockLog::class], version = 2, exportSchema = true)
@TypeConverters(ProfileConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun blockLogDao(): BlockLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "focus_block.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
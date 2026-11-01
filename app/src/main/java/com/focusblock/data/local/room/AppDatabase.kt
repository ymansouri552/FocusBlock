package com.focusblock.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

        /**
         * Non-destructive upgrade from the early v1 schema. The v1 database did not track
         * strict/enabled/schedules on profiles nor appName/blocked on logs, so missing columns
         * are added in place and existing rows are preserved.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (db.tableExists("profiles")) {
                    val columns = db.columnNames("profiles")
                    if ("blockedApps" !in columns) {
                        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `blockedApps` TEXT NOT NULL DEFAULT ''")
                    }
                    if ("strict" !in columns) {
                        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `strict` INTEGER NOT NULL DEFAULT 0")
                    }
                    if ("enabled" !in columns) {
                        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `enabled` INTEGER NOT NULL DEFAULT 0")
                    }
                    if ("createdAt" !in columns) {
                        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                    }
                    if ("schedules" !in columns) {
                        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `schedules` TEXT NOT NULL DEFAULT ''")
                    }
                } else {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `profiles` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`blockedApps` TEXT NOT NULL, " +
                            "`strict` INTEGER NOT NULL, " +
                            "`enabled` INTEGER NOT NULL, " +
                            "`createdAt` INTEGER NOT NULL, " +
                            "`schedules` TEXT NOT NULL)"
                    )
                }

                if (db.tableExists("block_log")) {
                    val columns = db.columnNames("block_log")
                    if ("appName" !in columns) {
                        db.execSQL("ALTER TABLE `block_log` ADD COLUMN `appName` TEXT")
                    }
                    if ("blocked" !in columns) {
                        db.execSQL("ALTER TABLE `block_log` ADD COLUMN `blocked` INTEGER NOT NULL DEFAULT 1")
                    }
                } else {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `block_log` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`profileId` INTEGER NOT NULL, " +
                            "`packageName` TEXT NOT NULL, " +
                            "`appName` TEXT, " +
                            "`timestamp` INTEGER NOT NULL, " +
                            "`blocked` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                    )
                }

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_block_log_timestamp` ON `block_log` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_block_log_profileId` ON `block_log` (`profileId`)")
            }
        }

        private fun SupportSQLiteDatabase.tableExists(table: String): Boolean {
            query("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(table)).use { cursor ->
                return cursor.moveToFirst()
            }
        }

        private fun SupportSQLiteDatabase.columnNames(table: String): Set<String> {
            val names = mutableSetOf<String>()
            query("PRAGMA table_info(`$table`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex >= 0) {
                    while (cursor.moveToNext()) {
                        names.add(cursor.getString(nameIndex))
                    }
                }
            }
            return names
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "focus_block.db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

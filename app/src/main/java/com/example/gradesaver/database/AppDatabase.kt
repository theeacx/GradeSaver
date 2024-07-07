package com.example.gradesaver.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.database.entities.*

@Database(
    entities = [
        User::class,
        Course::class,
        Activity::class,
        Enrollment::class,
        ReminderSchedule::class,
        Reminder::class,
        UserActivity::class,
        PersonalActivity::class,
        CheckedActivity::class,
        ExportedActivity::class // Add the new entity here
    ],
    version = 5, // Update the version number
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `exportedActivities` (`exportedActivityId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `activityId` INTEGER NOT NULL, `activityType` TEXT NOT NULL)")
            }
        }
    }
}

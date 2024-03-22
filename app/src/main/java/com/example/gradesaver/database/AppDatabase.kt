package com.example.gradesaver.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gradesaver.database.entities.*
import com.example.gradesaver.database.dao.AppDao
@Database(
    entities = [
        User::class,
        Course::class,
        Activity::class,
        Enrollment::class,
        ReminderSchedule::class,
        Reminder::class,
        UserActivity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // If the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // Migration strategies can be added here for database version updates
                    .fallbackToDestructiveMigration() // Use this to reset the database instead of migrating if no migration object is available
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
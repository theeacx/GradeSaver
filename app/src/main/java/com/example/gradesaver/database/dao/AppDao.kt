package com.example.gradesaver.database.dao
import androidx.room.*
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.Enrollment
import com.example.gradesaver.database.entities.Reminder
import com.example.gradesaver.database.entities.ReminderSchedule
import com.example.gradesaver.database.entities.User
import com.example.gradesaver.database.entities.UserActivity
import java.util.*
@Dao
interface AppDao {
    // Users CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Int): User

    // Courses CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<Course>

    @Query("SELECT * FROM courses WHERE courseId = :courseId")
    suspend fun getCourseById(courseId: Int): Course?

    // Activities CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Update
    suspend fun updateActivity(activity: Activity)

    @Delete
    suspend fun deleteActivity(activity: Activity)

    @Query("SELECT * FROM activities")
    suspend fun getAllActivities(): List<Activity>

    // Enrollments CRUD and Queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: Enrollment)

    @Delete
    suspend fun deleteEnrollment(enrollment: Enrollment)

    @Query("SELECT * FROM enrollments WHERE courseId = :courseId")
    suspend fun getEnrollmentsByCourse(courseId: Int): List<Enrollment>

    @Query("SELECT * FROM enrollments WHERE studentId = :studentId")
    suspend fun getEnrollmentsByStudent(studentId: Int): List<Enrollment>

    // ReminderSchedules CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderSchedule(reminderSchedule: ReminderSchedule)

    @Update
    suspend fun updateReminderSchedule(reminderSchedule: ReminderSchedule)

    @Delete
    suspend fun deleteReminderSchedule(reminderSchedule: ReminderSchedule)

    @Query("SELECT * FROM reminderSchedules")
    suspend fun getAllReminderSchedules(): List<ReminderSchedule>

    // Reminders CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE reminderScheduleId = :reminderScheduleId")
    suspend fun getRemindersBySchedule(reminderScheduleId: Int): List<Reminder>

    // UserActivities CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserActivity(userActivity: UserActivity)

    @Update
    suspend fun updateUserActivity(userActivity: UserActivity)

    @Delete
    suspend fun deleteUserActivity(userActivity: UserActivity)

    @Query("SELECT * FROM userActivities WHERE studentId = :studentId")
    suspend fun getUserActivitiesByStudent(studentId: Int): List<UserActivity>

    // Complex Queries
    // Example: Get all courses taught by a specific professor
    @Transaction
    @Query("SELECT * FROM courses WHERE professorId = :professorId")
    suspend fun getCoursesByProfessor(professorId: Int): MutableList<Course>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?




}
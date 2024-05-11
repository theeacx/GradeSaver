package com.example.gradesaver.database.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gradesaver.dataClasses.ActivityCount
import com.example.gradesaver.dataClasses.EnrollmentCountByCourse
import com.example.gradesaver.dataClasses.MonthlyActivityCount
import com.example.gradesaver.dataClasses.PendingActivityCountByStudent
import com.example.gradesaver.dataClasses.ReminderCountByActivity
import com.example.gradesaver.dataClasses.ScheduleCountByActivityAndUser
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
    suspend fun insertReminderSchedule(reminderSchedule: ReminderSchedule): Long



    @Update
    suspend fun updateReminderSchedule(reminderSchedule: ReminderSchedule)

    @Delete
    suspend fun deleteReminderSchedule(reminderSchedule: ReminderSchedule)

    @Query("SELECT * FROM reminderSchedules")
    suspend fun getAllReminderSchedules(): List<ReminderSchedule>

    // Reminders CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

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

    @Query("SELECT * FROM activities WHERE courseId = :courseId")
    suspend fun getActivitiesByCourse(courseId: Int): List<Activity>

    // Activities filters
    @Query("SELECT * FROM activities WHERE courseId = :courseId AND activityId IN (SELECT activityId FROM reminderSchedules)")
    suspend fun getActivitiesWithRemindersByCourse(courseId: Int): List<Activity>

    @Query("SELECT * FROM activities WHERE courseId = :courseId AND activityId NOT IN (SELECT activityId FROM reminderSchedules)")
    suspend fun getActivitiesWithoutRemindersByCourse(courseId: Int): List<Activity>

    // Get activities due within the next 7 days
    @Query("""
        SELECT * FROM activities 
        WHERE courseId = :courseId AND 
        dueDate BETWEEN :start AND :end
    """)
    suspend fun getUpcomingActivitiesByCourse(courseId: Int, start: Date, end: Date): List<Activity>

    // Get past deadline activities
    @Query("""
        SELECT * FROM activities 
        WHERE courseId = :courseId AND 
        dueDate < :currentDate
    """)
    suspend fun getPastDeadlineActivitiesByCourse(courseId: Int, currentDate: Date): List<Activity>

    @Transaction
    @Query("""
    SELECT r.* FROM reminders r
    INNER JOIN reminderSchedules rs ON r.reminderScheduleId = rs.reminderScheduleId
    WHERE rs.activityId = :activityId
""")
    suspend fun getRemindersByActivity(activityId: Int): List<Reminder>

    @Query("SELECT * FROM reminderSchedules WHERE studentId = :userId AND activityId = :activityId ORDER BY reminderScheduleId DESC LIMIT 1")
    suspend fun getLatestReminderScheduleForUser(userId: Int, activityId: Int): ReminderSchedule?

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("SELECT * FROM reminderSchedules WHERE reminderScheduleId = :reminderScheduleId LIMIT 1")
    suspend fun getReminderScheduleById(reminderScheduleId: Int): ReminderSchedule?

    @Query("SELECT * FROM activities WHERE courseId = :courseId")
    fun fetchActivitiesByCourseAsLiveData(courseId: Int): LiveData<List<Activity>>




    @Query("SELECT * FROM activities WHERE activityId = :activityId")
    fun getActivityById(activityId: Int): Activity?

    @Query("""
    SELECT strftime('%m', datetime(dueDate / 1000, 'unixepoch')) AS month, COUNT(*) AS count
    FROM activities
    WHERE courseId IN (SELECT courseId FROM courses WHERE professorId = :professorId)
    GROUP BY strftime('%m', datetime(dueDate / 1000, 'unixepoch'))
    ORDER BY strftime('%m', datetime(dueDate / 1000, 'unixepoch'))
""")
    suspend fun getActivityCountsByMonth(professorId: Int): List<MonthlyActivityCount>




    @Query("""
    SELECT activityType, COUNT(*) as activityCount
    FROM activities
    WHERE courseId IN (SELECT courseId FROM courses WHERE professorId = :professorId)
    GROUP BY activityType
""")
    suspend fun getActivityCountsByType(professorId: Int): List<ActivityCount>
    @Query("""
    SELECT a.activityName, COUNT(*) as reminderCount
    FROM reminders r
    JOIN reminderSchedules rs ON r.reminderScheduleId = rs.reminderScheduleId
    JOIN activities a ON rs.activityId = a.activityId
    WHERE a.courseId IN (SELECT courseId FROM courses WHERE professorId = :professorId)
    GROUP BY a.activityId
""")
    suspend fun getReminderCountsByActivity(professorId: Int): List<ReminderCountByActivity>

    @Query("""
    SELECT a.activityName, u.email, COUNT(*) as scheduleCount
    FROM reminderSchedules rs
    JOIN activities a ON rs.activityId = a.activityId
    JOIN users u ON rs.studentId = u.userId
    WHERE a.courseId IN (SELECT courseId FROM courses WHERE professorId = :professorId)
    GROUP BY rs.activityId, rs.studentId
""")
    suspend fun getScheduleCountsByActivityAndUser(professorId: Int): List<ScheduleCountByActivityAndUser>


    @Query("""
    SELECT c.courseName, COUNT(*) as enrolledCount
    FROM enrollments e
    JOIN courses c ON e.courseId = c.courseId
    WHERE c.professorId = :professorId
    GROUP BY c.courseId
""")
    suspend fun getEnrollmentCountsByCourse(professorId: Int): List<EnrollmentCountByCourse>


    @Query("""
    SELECT a.activityName, u.email, COUNT(ua.userActivityId) as pendingActivities
    FROM userActivities ua
    JOIN activities a ON ua.activityId = a.activityId
    JOIN users u ON ua.studentId = u.userId
    WHERE a.dueDate > CURRENT_DATE AND ua.isCompleted = 0
    AND a.courseId IN (SELECT courseId FROM courses WHERE professorId = :professorId)
    GROUP BY a.activityId, ua.studentId
""")
    suspend fun getPendingActivitiesByStudent(professorId: Int): List<PendingActivityCountByStudent>


//    @Query("""
//        SELECT strftime('%m', dueDate) as month, COUNT(*) as totalDeadlines
//        FROM activities
//        WHERE courseId IN (SELECT courseId FROM courses)
//        GROUP BY strftime('%m', dueDate)
//    """)
//    suspend fun getTotalDeadlinesByMonth(): List<MonthlyDeadlineCount>


}
package com.example.gradesaver.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gradesaver.dataClasses.*
import com.example.gradesaver.database.entities.*
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
    suspend fun insertActivity(activity: Activity): Long

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

    @Query("SELECT * FROM activities WHERE activityId = :activityId LIMIT 1")
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

    @Query("""
    SELECT a.activityId,
           a.activityName,
           strftime('%d', datetime(a.dueDate / 1000, 'unixepoch')) AS day,
           strftime('%m', datetime(a.dueDate / 1000, 'unixepoch')) AS month
    FROM activities a
    WHERE a.courseId = :courseId
    """)
    suspend fun getActivityDeadlinesByDayAndMonth(courseId: Int): List<ActivityExclusionInfo>

    @Query("""
    SELECT a.activityId,
           a.activityName,
           strftime('%d', datetime(a.dueDate / 1000, 'unixepoch')) AS day,
           strftime('%m', datetime(a.dueDate / 1000, 'unixepoch')) AS month
    FROM activities a
    WHERE a.courseId != :courseId
    """)
    suspend fun getAllActivitiesExceptSelectedCourse(courseId: Int): List<ActivityExclusionInfo>

    @Query("""
    SELECT a.activityName, COUNT(r.reminderId) as reminderCount
    FROM activities a
    LEFT JOIN reminderSchedules rs ON a.activityId = rs.activityId
    LEFT JOIN reminders r ON rs.reminderScheduleId = r.reminderScheduleId
    WHERE a.courseId = :courseId
    GROUP BY a.activityId
    """)
    suspend fun getRemindersCountByActivity(courseId: Int): List<ActivityReminderCount>

    @Query("""
    SELECT a.activityType, COUNT(r.reminderId) AS numberOfReminders
    FROM enrollments e
    JOIN activities a ON e.courseId = a.courseId
    LEFT JOIN reminderSchedules rs ON a.activityId = rs.activityId
    LEFT JOIN reminders r ON rs.reminderScheduleId = r.reminderScheduleId
    WHERE e.studentId = :studentId
    GROUP BY a.activityType
    """)
    suspend fun getReminderCountByActivityType(studentId: Int): List<ActivityReminders>

    @Query("""
    SELECT strftime('%Y-%m', datetime(a.dueDate / 1000, 'unixepoch')) AS month, COUNT(*) AS numberOfDeadlines
    FROM activities a
    JOIN enrollments e ON a.courseId = e.courseId
    WHERE e.studentId = :studentId
    GROUP BY strftime('%Y-%m', datetime(a.dueDate / 1000, 'unixepoch'))
    """)
    suspend fun getActivityDeadlinesByMonth(studentId: Int): List<MonthlyActivityDeadlines>

    @Query("""
    SELECT c.courseName, COUNT(a.activityId) AS numberOfActivities
    FROM courses c
    JOIN activities a ON c.courseId = a.courseId
    JOIN enrollments e ON c.courseId = e.courseId
    WHERE e.studentId = :studentId
    GROUP BY c.courseId
    """)
    suspend fun getActivityCountByCourse(studentId: Int): List<CourseActivityCount>

    @Insert
    suspend fun insertPersonalActivity(personalActivity: PersonalActivity): Long

    @Query("SELECT * FROM personalActivities WHERE userId = :userId")
    suspend fun getPersonalActivitiesByUser(userId: Int): List<PersonalActivity>

    @Delete
    suspend fun deletePersonalActivity(personalActivity: PersonalActivity): Int

    @Query("SELECT * FROM personalActivities WHERE userId = :userId AND dueDate BETWEEN :dayStart AND :dayEnd")
    suspend fun getPersonalActivitiesByDay(userId: Int, dayStart: Date, dayEnd: Date): List<PersonalActivity>

    @Query("SELECT * FROM activities WHERE courseId = :courseId AND dueDate BETWEEN :dayStart AND :dayEnd")
    fun getActivitiesForDay(courseId: Int, dayStart: Date, dayEnd: Date): List<Activity>

    // Function to fetch activities for all courses by a professor on a specific day
    @Query("""
        SELECT a.* FROM activities a
        JOIN courses c ON a.courseId = c.courseId
        WHERE c.professorId = :professorId AND 
              a.dueDate BETWEEN :dayStart AND :dayEnd
    """)
    suspend fun getActivitiesForProfessorByDay(professorId: Int, dayStart: Date, dayEnd: Date): List<Activity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckedActivity(checkedActivity: CheckedActivity): Long

    @Update
    suspend fun updateCheckedActivity(checkedActivity: CheckedActivity)

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId AND activityId = :activityId LIMIT 1")
    suspend fun getCheckedActivityByUserAndActivity(userId: Int, activityId: Int): CheckedActivity?

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId AND personalActivityId = :personalActivityId LIMIT 1")
    suspend fun getCheckedActivityByUserAndPersonalActivity(userId: Int, personalActivityId: Int): CheckedActivity?

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId AND reminderId = :reminderId LIMIT 1")
    suspend fun getCheckedActivityByUserAndReminder(userId: Int, reminderId: Int): CheckedActivity?

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId AND activityId = :activityId")
    fun getCheckedActivityLiveData(userId: Int, activityId: Int): LiveData<CheckedActivity>

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId AND personalActivityId = :personalActivityId")
    fun getCheckedPersonalActivityLiveData(userId: Int, personalActivityId: Int): LiveData<CheckedActivity>

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId")
    suspend fun getCheckedActivitiesByUser(userId: Int): List<CheckedActivity>

    @Delete
    suspend fun deleteCheckedActivity(checkedActivity: CheckedActivity)

    @Update
    suspend fun updatePersonalActivity(personalActivity: PersonalActivity)

    @Query("DELETE FROM checkedActivities WHERE userId = :userId AND activityId = :activityId")
    suspend fun deleteCheckedActivityByActivity(userId: Int, activityId: Int)

    @Query("DELETE FROM checkedActivities WHERE userId = :userId AND personalActivityId = :personalActivityId")
    suspend fun deleteCheckedActivityByPersonalActivity(userId: Int, personalActivityId: Int)

    @Query("DELETE FROM checkedActivities WHERE userId = :userId AND reminderId = :reminderId")
    suspend fun deleteCheckedActivityByReminder(userId: Int, reminderId: Int)

    @Query("SELECT * FROM personalActivities WHERE personalActivityId = :personalActivityId")
    suspend fun getPersonalActivityById(personalActivityId: Int): PersonalActivity?

    @Query("""
        SELECT a.*
        FROM activities a
        JOIN courses c ON a.courseId = c.courseId
        JOIN enrollments e ON c.courseId = e.courseId
        WHERE e.studentId = :userId
    """)
    suspend fun getActivitiesByUser(userId: Int): List<Activity>

    @Query("""
        SELECT a.*
        FROM activities a
        JOIN courses c ON a.courseId = c.courseId
        JOIN enrollments e ON c.courseId = e.courseId
        WHERE e.studentId = :userId AND a.dueDate BETWEEN :startOfDay AND :endOfDay
    """)
    suspend fun getTodaysActivitiesByUser(userId: Int, startOfDay: Date, endOfDay: Date): List<Activity>

    @Query("SELECT * FROM personalActivities WHERE userId = :userId AND dueDate BETWEEN :startOfDay AND :endOfDay")
    suspend fun getTodaysPersonalActivitiesByUser(userId: Int, startOfDay: Date, endOfDay: Date): List<PersonalActivity>

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId AND personalActivityId = :personalActivityId")
    fun getCheckedPersonalActivity(userId: Int, personalActivityId: Int): CheckedActivity?

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId AND activityId = :activityId")
    fun getCheckedActivity(userId: Int, activityId: Int): CheckedActivity?

    @Query("SELECT * FROM checkedActivities WHERE userId = :userId AND reminderId = :reminderId")
    fun getCheckedReminder(userId: Int, reminderId: Int): CheckedActivity?

    @Query("""
        SELECT r.*, rs.activityId
        FROM reminders r
        INNER JOIN reminderSchedules rs ON r.reminderScheduleId = rs.reminderScheduleId
        WHERE rs.studentId = :userId AND r.reminderDate BETWEEN :startOfDay AND :endOfDay
    """)
    suspend fun getRemindersWithActivityForUserByDay(userId: Int, startOfDay: Date, endOfDay: Date): List<ReminderWithActivity>

    @Query("SELECT * FROM reminders WHERE reminderId = :reminderId LIMIT 1")
    suspend fun getReminderById(reminderId: Int): Reminder?

    @Insert
    fun insertUsers(users: List<User>): List<Long>

    @Insert
    fun insertCourses(courses: List<Course>): List<Long>

    @Insert
    fun insertEnrollments(enrollments: List<Enrollment>): List<Long>

    @Insert
    fun insertActivities(activities: List<Activity>): List<Long>

    @Insert
    fun insertPersonalActivities(personalActivities: List<PersonalActivity>): List<Long>

    @Insert
    fun insertReminderSchedules(reminderSchedules: List<ReminderSchedule>): List<Long>

    @Insert
    fun insertReminders(reminders: List<Reminder>): List<Long>

    @Insert
    fun insertCheckedActivities(checkedActivities: List<CheckedActivity>): List<Long>

    @Query("DELETE FROM checkedActivities")
    fun deleteAllCheckedActivities()

    @Query("DELETE FROM reminders")
    fun deleteAllReminders()

    @Query("DELETE FROM reminderSchedules")
    fun deleteAllReminderSchedules()

    @Query("DELETE FROM personalActivities")
    fun deleteAllPersonalActivities()

    @Query("DELETE FROM activities")
    fun deleteAllActivities()

    @Query("DELETE FROM enrollments")
    fun deleteAllEnrollments()

    @Query("DELETE FROM courses")
    fun deleteAllCourses()

    @Query("DELETE FROM users")
    fun deleteAllUsers()
    @Query("SELECT * FROM reminderSchedules WHERE activityId = :activityId AND studentId = :studentId LIMIT 1")
    fun getReminderSchedule(activityId: Int, studentId: Int): ReminderSchedule?

    @Query("SELECT * FROM reminders WHERE reminderScheduleId IN (SELECT reminderScheduleId FROM reminderSchedules WHERE activityId = :activityId AND studentId = :studentId)")
    fun getRemindersByActivityAndUser(activityId: Int, studentId: Int): List<Reminder>

    @Query("""
    SELECT activityType, COUNT(*) as activityCount
    FROM activities
    WHERE courseId = :courseId
    GROUP BY activityType
""")
    suspend fun getActivityCountsByTypeForCourse(courseId: Int): List<ActivityCount>

    @Query("""
    SELECT strftime('%d', datetime(dueDate / 1000, 'unixepoch')) AS day,
           COUNT(*) AS count
    FROM activities
    WHERE courseId = :courseId
    GROUP BY strftime('%d', datetime(dueDate / 1000, 'unixepoch'))
    ORDER BY strftime('%d', datetime(dueDate / 1000, 'unixepoch'))
""")
    suspend fun getActivityDeadlinesByDay(courseId: Int): List<ActivityDeadlineCount>

    @Query("""
        SELECT strftime('%d', datetime(dueDate / 1000, 'unixepoch')) AS day,
               COUNT(*) AS count
        FROM activities
        WHERE courseId != :courseId
        GROUP BY strftime('%d', datetime(dueDate / 1000, 'unixepoch'))
        ORDER BY strftime('%d', datetime(dueDate / 1000, 'unixepoch'))
    """)
    suspend fun getAllActivityDeadlinesByDayExceptCourse(courseId: Int): List<ActivityDeadlineCount>

    @Query("""
    SELECT a.*, c.courseName 
    FROM activities a 
    JOIN courses c ON a.courseId = c.courseId 
    JOIN enrollments e ON c.courseId = e.courseId 
    WHERE e.studentId = :userId AND a.dueDate > :currentDate
""")
    suspend fun getUpcomingActivities(userId: Int, currentDate: Date): List<ActivityWithCourse>


    @Query("SELECT * FROM activities WHERE courseId IN (SELECT courseId FROM enrollments WHERE studentId = :userId)")
    suspend fun getAllActivitiesByUser(userId: Int): List<Activity>

    @Query("SELECT * FROM personalActivities WHERE userId = :userId")
    suspend fun getAllPersonalActivitiesByUser(userId: Int): List<PersonalActivity>

    @Query("SELECT reminders.* FROM reminders INNER JOIN reminderSchedules ON reminders.reminderScheduleId = reminderSchedules.reminderScheduleId WHERE reminderSchedules.studentId = :userId")
    suspend fun getAllRemindersByUser(userId: Int): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExportedActivity(exportedActivity: ExportedActivity)

    @Query("SELECT * FROM exportedActivities")
    suspend fun getAllExportedActivities(): List<ExportedActivity>
}

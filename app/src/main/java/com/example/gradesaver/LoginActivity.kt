//package com.example.gradesaver
//
//import android.app.AlarmManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.example.gradesaver.database.AppDatabase
//import com.example.gradesaver.database.dao.AppDao
//import com.example.gradesaver.notifications.NotificationReceiver
//import com.example.gradesaver.security.Hash.Companion.toSHA256
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.threeten.bp.LocalDate
//import org.threeten.bp.ZoneId
//import java.util.Date
//
//class LoginActivity : AppCompatActivity() {
//    lateinit var username: EditText
//    lateinit var password: EditText
//    lateinit var loginButton: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        loginButton = findViewById(R.id.loginButton)
//        username = findViewById(R.id.username)
//        password = findViewById(R.id.password)
//        val dao = AppDatabase.getInstance(this).appDao()
//        val signUpText: TextView = findViewById(R.id.signUpText)
//
//        signUpText.setOnClickListener {
//            val intent = Intent(this, SignUpActivity::class.java)
//            startActivity(intent)
//        }
//
//        loginButton.setOnClickListener {
//            val inputUsername = username.text.toString().trim()
//            val inputPassword = password.text.toString().toSHA256()
//
//            when {
//                inputUsername.isEmpty() -> Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show()
//                inputPassword.isEmpty() -> Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show()
//                else -> loginUser(inputUsername, inputPassword, dao)
//            }
//        }
//    }
//
//    private fun loginUser(inputUsername: String, inputPassword: String, dao: AppDao) {
//        lifecycleScope.launch {
//            val user = dao.getUserByEmail(inputUsername)
//            if (user != null && user.password == inputPassword) {
//                // Save the user ID to shared preferences
//                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//                with(sharedPreferences.edit()) {
//                    putInt("user_id", user.userId)
//                    apply()
//                }
//
//                // Schedule notifications for activities
//                scheduleTodaysNotificationsForUser(user.userId, dao)
//
//                val nextActivity = when (user.role) {
//                    "Professor" -> ProfessorMainScreenActivity::class.java
//                    "Student" -> StudentMainScreenActivity::class.java
//                    else -> null
//                }
//
//                nextActivity?.let {
//                    val intent = Intent(this@LoginActivity, it)
//                    intent.putExtra("USER_DETAILS", user)
//                    startActivity(intent)
//                } ?: run {
//                    Toast.makeText(this@LoginActivity, "Invalid user role.", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this@LoginActivity, "Login Failed! Please sign up!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun scheduleTodaysNotificationsForUser(userId: Int, dao: AppDao) {
//        lifecycleScope.launch {
//            val today = LocalDate.now()
//            val startOfDay = Date.from(java.time.Instant.ofEpochMilli(today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()))
//            val endOfDay = Date.from(java.time.Instant.ofEpochMilli(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()))
//
//            val activities = dao.getTodaysActivitiesByUser(userId, startOfDay, endOfDay)
//            val personalActivities = dao.getTodaysPersonalActivitiesByUser(userId, startOfDay, endOfDay)
//
//            withContext(Dispatchers.Main) {
//                for (activity in activities) {
//                    scheduleNotification(activity.activityName, activity.activityType, activity.dueDate, false)
//                }
//                for (personalActivity in personalActivities) {
//                    scheduleNotification(personalActivity.activityName, personalActivity.activityType, personalActivity.dueDate, true)
//                }
//            }
//        }
//    }
//
//    private fun scheduleNotification(activityName: String, activityType: String, dueDate: Date, isPersonal: Boolean) {
//        val notificationIntent = Intent(this, NotificationReceiver::class.java).apply {
//            putExtra("activityName", activityName)
//            putExtra("activityType", activityType)
//            putExtra("isPersonal", isPersonal)
//        }
//        val pendingIntent = PendingIntent.getBroadcast(
//            this,
//            activityName.hashCode(),
//            notificationIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmManager.setExact(
//            AlarmManager.RTC_WAKEUP,
//            dueDate.time,
//            pendingIntent
//        )
//    }
//}

package com.example.gradesaver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.CheckedActivity
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.Enrollment
import com.example.gradesaver.database.entities.PersonalActivity
import com.example.gradesaver.database.entities.Reminder
import com.example.gradesaver.database.entities.ReminderSchedule
import com.example.gradesaver.database.entities.User
import com.example.gradesaver.security.Hash.Companion.toSHA256
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class LoginActivity : AppCompatActivity() {
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.loginButton)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        val dao = AppDatabase.getInstance(this).appDao()
        val signUpText: TextView = findViewById(R.id.signUpText)

        signUpText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val inputUsername = username.text.toString().trim()
            val inputPassword = password.text.toString().toSHA256()

            when {
                inputUsername.isEmpty() -> Toast.makeText(
                    this,
                    "Please enter your email!",
                    Toast.LENGTH_SHORT
                ).show()

                inputPassword.isEmpty() -> Toast.makeText(
                    this,
                    "Please enter your password!",
                    Toast.LENGTH_SHORT
                ).show()

                else -> loginUser(inputUsername, inputPassword, dao)
            }
        }

        // Populate the database with initial data
        //purgeDatabase(dao)
        //populateDatabase(dao)
    }

    private fun loginUser(inputUsername: String, inputPassword: String, dao: AppDao) {
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { dao.getUserByEmail(inputUsername) }
            if (user != null && user.password == inputPassword) {
                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt("user_id", user.userId)
                    apply()
                }

                // Schedule notifications for activities
                (application as? GradeSaverApplication)?.scheduleTodaysNotificationsForUser(
                    user.userId,
                    dao
                )

                val nextActivity = when (user.role) {
                    "Professor" -> ProfessorMainScreenActivity::class.java
                    "Student" -> StudentMainScreenActivity::class.java
                    else -> null
                }

                nextActivity?.let {
                    val intent = Intent(this@LoginActivity, it)
                    intent.putExtra("USER_DETAILS", user)
                    startActivity(intent)
                    finish()
                } ?: run {
                    Toast.makeText(this@LoginActivity, "Invalid user role.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Login Failed! Please sign up!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun populateDatabase(dao: AppDao) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@LoginActivity).runInTransaction {
                    // Hash password
                    val password = "1234".toSHA256()

                    // Insert Professors
                    val professorEmails = listOf("prof1@csie.ase.ro", "prof2@csie.ase.ro", "prof3@csie.ase.ro", "prof4@csie.ase.ro", "prof5@csie.ase.ro")
                    val professors = professorEmails.map { email ->
                        User(email = email, password = password, role = "Professor")
                    }
                    val professorIds = dao.insertUsers(professors)
                    println("Inserted Professors: $professorIds")

                    // Insert Students
                    val studentEmails = listOf("stud1@stud.ase.ro", "stud2@stud.ase.ro", "stud3@stud.ase.ro", "stud4@stud.ase.ro", "stud5@stud.ase.ro")
                    val students = studentEmails.map { email ->
                        User(email = email, password = password, role = "Student")
                    }
                    val studentIds = dao.insertUsers(students)
                    println("Inserted Students: $studentIds")

                    // Insert Courses
                    val courses = mutableListOf<Course>()
                    professorIds.forEachIndexed { index, professorId ->
                        for (j in 1..4) {
                            courses.add(Course(courseName = "Course $j for Prof ${index + 1}", professorId = professorId.toInt(), enrollmentCode = "1234"))
                        }
                    }
                    val courseIds = dao.insertCourses(courses)
                    println("Inserted Courses: $courseIds")

                    // Insert Enrollments
                    val enrollments = mutableListOf<Enrollment>()
                    studentIds.forEachIndexed { studentIndex, studentId ->
                        courseIds.forEachIndexed { courseIndex, courseId ->
                            // Enroll each student in the first 4 courses
                            if (courseIndex < 4) {
                                enrollments.add(Enrollment(courseId = courseId.toInt(), studentId = studentId.toInt()))
                            }
                        }
                    }
                    dao.insertEnrollments(enrollments)
                    println("Inserted Enrollments")

                    // Insert Activities
                    val activities = mutableListOf<Activity>()
                    var activityCounter = 1
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
                    courseIds.forEachIndexed { courseIndex, courseId ->
                        for (j in 1..5) {
                            calendar.set(Calendar.MONTH, Calendar.APRIL + ((activityCounter - 1) % 3))
                            calendar.set(Calendar.DAY_OF_MONTH, 1 + (activityCounter - 1) % 28)
                            activities.add(Activity(courseId = courseId.toInt(), activityName = "Activity $j for Course ${courseIndex + 1}", activityType = "Lecture", dueDate = calendar.time))
                            activityCounter++
                        }
                    }
                    val activityIds = dao.insertActivities(activities)
                    println("Inserted Activities: $activityIds")

                    // Insert Personal Activities for Students and Professors
                    val personalActivities = mutableListOf<PersonalActivity>()
                    studentIds.forEachIndexed { studentIndex, studentId ->
                        for (j in 1..5) {
                            calendar.set(Calendar.MONTH, Calendar.APRIL + ((j - 1) % 3))
                            calendar.set(Calendar.DAY_OF_MONTH, 1 + (j - 1) % 28)
                            personalActivities.add(PersonalActivity(userId = studentId.toInt(), activityName = "Personal Activity $j for Student ${studentIndex + 1}", activityType = "Personal", dueDate = calendar.time))
                        }
                    }
                    professorIds.forEachIndexed { professorIndex, professorId ->
                        for (j in 1..5) {
                            calendar.set(Calendar.MONTH, Calendar.APRIL + ((j - 1) % 3))
                            calendar.set(Calendar.DAY_OF_MONTH, 1 + (j - 1) % 28)
                            personalActivities.add(PersonalActivity(userId = professorId.toInt(), activityName = "Personal Activity $j for Professor ${professorIndex + 1}", activityType = "Personal", dueDate = calendar.time))
                        }
                    }
                    val personalActivityIds = dao.insertPersonalActivities(personalActivities)
                    println("Inserted Personal Activities: $personalActivityIds")

                    // Insert Reminder Schedules
                    val reminderSchedules = mutableListOf<ReminderSchedule>()
                    activityIds.forEachIndexed { activityIndex, activityId ->
                        studentIds.forEach { studentId ->
                            reminderSchedules.add(ReminderSchedule(activityId = activityId.toInt(), studentId = studentId.toInt(), reminderType = "Email", startDate = Date(), endDate = null, numberOfReminders = 3))
                        }
                    }
                    val reminderScheduleIds = dao.insertReminderSchedules(reminderSchedules)
                    println("Inserted Reminder Schedules: $reminderScheduleIds")

                    // Insert Reminders (these get inserted by default, so we will simulate this step here)
                    val reminders = mutableListOf<Reminder>()
                    reminderScheduleIds.forEachIndexed { scheduleIndex, scheduleId ->
                        reminders.add(Reminder(reminderScheduleId = scheduleId.toInt(), reminderDate = Date(), reminderMessage = "Reminder for Schedule ${scheduleIndex + 1}"))
                    }
                    val reminderIds = dao.insertReminders(reminders)
                    println("Inserted Reminders: $reminderIds")

                    // Insert Checked Activities
                    val checkedActivities = mutableListOf<CheckedActivity>()
                    activityIds.forEachIndexed { activityIndex, activityId ->
                        studentIds.forEach { studentId ->
                            checkedActivities.add(CheckedActivity(userId = studentId.toInt(), activityId = activityId.toInt(), isChecked = (activityIndex % 2 == 0)))
                        }
                    }
                    personalActivityIds.forEachIndexed { personalActivityIndex, personalActivityId ->
                        val isChecked = personalActivityIndex % 2 == 0
                        val personalActivity = personalActivities[personalActivityIndex]
                        checkedActivities.add(CheckedActivity(userId = personalActivity.userId, personalActivityId = personalActivityId.toInt(), isChecked = isChecked))
                    }
                    reminderIds.forEachIndexed { reminderIndex, reminderId ->
                        studentIds.forEach { studentId ->
                            checkedActivities.add(CheckedActivity(userId = studentId.toInt(), reminderId = reminderId.toInt(), isChecked = (reminderIndex % 2 == 0)))
                        }
                    }
                    dao.insertCheckedActivities(checkedActivities)
                    println("Inserted Checked Activities")
                }
            }
        }
    }

    private fun purgeDatabase(dao: AppDao) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@LoginActivity).runInTransaction {
//                    dao.deleteAllCheckedActivities()
//                    dao.deleteAllReminders()
//                    dao.deleteAllReminderSchedules()
//                    dao.deleteAllPersonalActivities()
//                    dao.deleteAllActivities()
//                    dao.deleteAllEnrollments()
//                    dao.deleteAllCourses()
                   // dao.deleteAllUsers()
                }
            }
        }
    }

}


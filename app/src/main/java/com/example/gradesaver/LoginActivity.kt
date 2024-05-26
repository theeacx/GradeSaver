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
import com.example.gradesaver.security.Hash.Companion.toSHA256
import kotlinx.coroutines.launch

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
                inputUsername.isEmpty() -> Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show()
                inputPassword.isEmpty() -> Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show()
                else -> loginUser(inputUsername, inputPassword, dao)
            }
        }
    }

    private fun loginUser(inputUsername: String, inputPassword: String, dao: AppDao) {
        lifecycleScope.launch {
            val user = dao.getUserByEmail(inputUsername)
            if (user != null && user.password == inputPassword) {
                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt("user_id", user.userId)
                    apply()
                }

                // Schedule notifications for activities
                (application as? GradeSaverApplication)?.scheduleTodaysNotificationsForUser(user.userId, dao)

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
                    Toast.makeText(this@LoginActivity, "Invalid user role.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@LoginActivity, "Login Failed! Please sign up!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


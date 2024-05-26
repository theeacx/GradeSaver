//package com.example.gradesaver
//
//import android.Manifest
//import android.app.AlertDialog
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Color
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.CheckBox
//import android.widget.ImageButton
//import android.widget.LinearLayout
//import android.widget.ScrollView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import com.example.gradesaver.database.AppDatabase
//import com.example.gradesaver.database.dao.AppDao
//import com.example.gradesaver.database.entities.Activity
//import com.example.gradesaver.database.entities.CheckedActivity
//import com.example.gradesaver.database.entities.PersonalActivity
//import com.example.gradesaver.database.entities.User
//import com.jakewharton.threetenabp.AndroidThreeTen
//import com.prolificinteractive.materialcalendarview.CalendarDay
//import com.prolificinteractive.materialcalendarview.MaterialCalendarView
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.threeten.bp.LocalDate
//import org.threeten.bp.ZoneId
//import org.threeten.bp.format.DateTimeFormatter
//import java.text.ParseException
//import java.util.Calendar
//import java.util.Date
//
//class ProfessorCalendarActivity : AppCompatActivity() {
//    private var user: User? = null
//    private lateinit var dao: AppDao
//    private val TAG = "ProfessorCalendarActivity"
//    private var lastClickTime: Long = 0
//    private lateinit var calendarView: MaterialCalendarView
//
//    companion object {
//        private const val REQUEST_NOTIFICATION_PERMISSION = 1
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        AndroidThreeTen.init(this)
//        setContentView(R.layout.activity_professor_calendar)
//
//        user = intent.getSerializableExtra("USER_DETAILS") as? User
//        if (user == null) {
//            finish()  // Close activity if no user data is found
//            return
//        }
//
//        dao = AppDatabase.getInstance(applicationContext).appDao()
//
//        calendarView = findViewById(R.id.calendarView)
//        val hourlyLayout = findViewById<LinearLayout>(R.id.hourlyLayout)
//        val scrollView = findViewById<ScrollView>(R.id.scrollView)
//        val reminderTitle = findViewById<TextView>(R.id.reminderTitle)
//        val addPersonalActivityButton = findViewById<Button>(R.id.addPersonalActivityButton)
//
//        val today = LocalDate.now()
//        val calendarDay = CalendarDay.from(today)
//        calendarView.setCurrentDate(calendarDay)
//        calendarView.selectedDate = calendarDay
//
//        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
//        reminderTitle.text = "The activities for: ${formatter.format(today)}"
//
//        calendarView.setOnDateChangedListener { _, date, _ ->
//            val selectedDate = date.date
//            reminderTitle.text = "The activities for: ${formatter.format(selectedDate)}"
//            loadActivitiesForDate(selectedDate)
//        }
//
//        loadActivitiesForDate(today)
//
//        addPersonalActivityButton.setOnClickListener {
//            val intent = Intent(this, AddPersonalActivityActivity::class.java)
//            intent.putExtra("USER_ID", user?.userId)
//            startActivity(intent)
//        }
//
//        createNotificationChannel()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
//            }
//        }
//    }
//
//    private fun loadActivitiesForDate(date: LocalDate) {
//        Log.d(TAG, "Loading activities for date: $date")
//        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
//        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
//
//        user?.let { user ->
//            lifecycleScope.launch {
//                val activities = dao.getActivitiesForProfessorByDay(user.userId, Date(startOfDay), Date(endOfDay))
//                val personalActivities = dao.getPersonalActivitiesByDay(user.userId, Date(startOfDay), Date(endOfDay))
//                val checkedActivities = dao.getCheckedActivitiesByUser(user.userId)
//                withContext(Dispatchers.Main) {
//                    val scrollView = findViewById<ScrollView>(R.id.scrollView)
//                    val reminderTitle = findViewById<TextView>(R.id.reminderTitle)
//                    if (activities.isEmpty() && personalActivities.isEmpty()) {
//                        Log.d(TAG, "No activities found for this day.")
//                        Toast.makeText(this@ProfessorCalendarActivity, "No activities for this day.", Toast.LENGTH_SHORT).show()
//                        scrollView.visibility = View.GONE
//                        reminderTitle.visibility = View.GONE
//                    } else {
//                        Log.d(TAG, "Activities loaded: ${activities.size}, Personal activities loaded: ${personalActivities.size}")
//                        Toast.makeText(this@ProfessorCalendarActivity, "Activities loaded for the day.", Toast.LENGTH_SHORT).show()
//                        scrollView.visibility = View.VISIBLE
//                        reminderTitle.visibility = View.VISIBLE
//                        updateUIWithActivities(activities, personalActivities, checkedActivities)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun updateUIWithActivities(activities: List<Activity>, personalActivities: List<PersonalActivity>, checkedActivities: List<CheckedActivity>) {
//        Log.d(TAG, "Updating UI with activities")
//        val hourlyLayout = findViewById<LinearLayout>(R.id.hourlyLayout)
//        hourlyLayout.removeAllViews()
//
//        val mergedActivities = mutableListOf<Any>().apply {
//            addAll(activities)
//            addAll(personalActivities)
//        }.sortedBy { activity ->
//            when (activity) {
//                is Activity -> activity.dueDate.time
//                is PersonalActivity -> activity.dueDate.time
//                else -> null
//            }
//        }
//
//        mergedActivities.forEach { activity ->
//            try {
//                val dueDate = when (activity) {
//                    is Activity -> activity.dueDate
//                    is PersonalActivity -> activity.dueDate
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//                val calendar = Calendar.getInstance()
//                calendar.time = dueDate
//
//                val hour = calendar.get(Calendar.HOUR_OF_DAY)
//                val minute = calendar.get(Calendar.MINUTE)
//
//                val activityName = when (activity) {
//                    is Activity -> activity.activityName
//                    is PersonalActivity -> activity.activityName
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//                val activityType = when (activity) {
//                    is Activity -> activity.activityType
//                    is PersonalActivity -> activity.activityType
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//
//                Log.d(TAG, "Activity: $activityName, Hour: $hour")
//
//                val activityItemLayout = LinearLayout(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    ).also {
//                        it.setMargins(8, 8, 8, 8)
//                    }
//                    orientation = LinearLayout.HORIZONTAL
//                    setPadding(20, 20, 20, 20)
//                    setBackgroundColor(ContextCompat.getColor(context, getActivityColor(activityType)))
//                    setOnClickListener { handleDoubleClick(activity) }
//                }
//
//                val checkBox = CheckBox(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    val isChecked = when (activity) {
//                        is Activity -> checkedActivities.any { it.activityId == activity.activityId && it.isChecked }
//                        is PersonalActivity -> checkedActivities.any { it.personalActivityId == activity.personalActivityId && it.isChecked }
//                        else -> false
//                    }
//                    this.isChecked = isChecked
//
//                    setOnCheckedChangeListener { _, isChecked ->
//                        lifecycleScope.launch {
//                            val checkedActivity = when (activity) {
//                                is Activity -> CheckedActivity(userId = user?.userId ?: 0, activityId = activity.activityId, isChecked = isChecked)
//                                is PersonalActivity -> CheckedActivity(userId = user?.userId ?: 0, personalActivityId = activity.personalActivityId, isChecked = isChecked)
//                                else -> null
//                            }
//                            if (checkedActivity != null) {
//                                if (isChecked) {
//                                    dao.insertCheckedActivity(checkedActivity)
//                                } else {
//                                    when (activity) {
//                                        is Activity -> dao.deleteCheckedActivityByActivity(checkedActivity.userId, activity.activityId)
//                                        is PersonalActivity -> dao.deleteCheckedActivityByPersonalActivity(checkedActivity.userId, activity.personalActivityId)
//                                    }
//                                }
//                                loadActivitiesForDate(calendarView.selectedDate?.date ?: LocalDate.now())
//                            }
//                        }
//                    }
//                }
//
//                val textView = TextView(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        0,
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        1f
//                    ).also {
//                        it.setMargins(16, 0, 16, 0)
//                    }
//                    text = String.format("%02d:%02d - %s", hour, minute, activityName)
//                    textSize = 16f
//                    setTextColor(ContextCompat.getColor(context, R.color.white))
//                }
//
//                val deleteButton = ImageButton(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    setImageResource(R.drawable.baseline_trash)
//                    setBackgroundColor(Color.TRANSPARENT)
//                    setOnClickListener {
//                        if (activity is PersonalActivity) {
//                            showDeleteConfirmationDialog(activity)
//                        }
//                    }
//                }
//
//                val postponeButton = ImageButton(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    setImageResource(R.drawable.baseline_postpone)
//                    setBackgroundColor(Color.TRANSPARENT)
//                    setOnClickListener {
//                        if (activity is PersonalActivity) {
//                            postponePersonalActivity(activity)
//                        }
//                    }
//                }
//
//                activityItemLayout.addView(checkBox)
//                activityItemLayout.addView(textView)
//                if (activity is PersonalActivity) {
//                    activityItemLayout.addView(deleteButton)
//                    activityItemLayout.addView(postponeButton)
//                }
//
//                hourlyLayout.addView(activityItemLayout)
//            } catch (e: ParseException) {
//                Log.e(TAG, "Error parsing date: ${activity}", e)
//                Toast.makeText(this, "Error parsing date: ${activity}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    private fun handleDoubleClick(activity: Any) {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastClickTime < 300) {
//            showCourseInfoPopup(activity)
//        }
//        lastClickTime = currentTime
//    }
//
//    private fun showCourseInfoPopup(activity: Any) {
//        val courseName = when (activity) {
//            is Activity -> lifecycleScope.launch {
//                val course = dao.getCourseById(activity.courseId)
//                withContext(Dispatchers.Main) {
//                    showInfoDialog(course?.courseName ?: "Unknown Course")
//                }
//            }
//            else -> return
//        }
//    }
//
//    private fun showInfoDialog(courseName: String) {
//        AlertDialog.Builder(this).apply {
//            setTitle("Course Information")
//            setMessage("Course Name: $courseName")
//            setPositiveButton("OK", null)
//        }.show()
//    }
//
//    private fun showDeleteConfirmationDialog(activity: PersonalActivity) {
//        AlertDialog.Builder(this).apply {
//            setTitle("Delete Personal Activity")
//            setMessage("Are you sure you want to delete this personal activity?")
//            setPositiveButton("Yes") { _, _ ->
//                lifecycleScope.launch {
//                    dao.deletePersonalActivity(activity)
//                    loadActivitiesForDate(LocalDate.now())
//                }
//            }
//            setNegativeButton("No", null)
//        }.show()
//    }
//
//    private fun postponePersonalActivity(activity: PersonalActivity) {
//        lifecycleScope.launch {
//            val newDueDate = Calendar.getInstance().apply {
//                time = activity.dueDate
//                add(Calendar.DAY_OF_YEAR, 1)
//            }.time
//
//            val updatedActivity = activity.copy(dueDate = newDueDate)
//            dao.updatePersonalActivity(updatedActivity)
//
//            withContext(Dispatchers.Main) {
//                Toast.makeText(this@ProfessorCalendarActivity, "Activity postponed to the next day.", Toast.LENGTH_SHORT).show()
//                loadActivitiesForDate(LocalDate.now())
//            }
//        }
//    }
//
//    private fun getActivityColor(activityType: String): Int {
//        return when (activityType) {
//            "Test" -> R.color.colorTest
//            "Midterm Exam" -> R.color.colorMidterm
//            "Essay" -> R.color.colorEssay
//            "Exam" -> R.color.colorExam
//            "Presentation" -> R.color.colorPresentation
//            "Project" -> R.color.colorProject
//            else -> R.color.defaultActivityColor
//        }
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "ActivityDeadlineChannel"
//            val descriptionText = "Channel for activity deadlines"
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel("ActivityDeadlineChannel", name, importance).apply {
//                description = descriptionText
//            }
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//}
//package com.example.gradesaver
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.AlarmManager
//import android.app.AlertDialog
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Color
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.CheckBox
//import android.widget.ImageButton
//import android.widget.LinearLayout
//import android.widget.ScrollView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import com.example.gradesaver.database.AppDatabase
//import com.example.gradesaver.database.dao.AppDao
//import com.example.gradesaver.database.entities.Activity
//import com.example.gradesaver.database.entities.CheckedActivity
//import com.example.gradesaver.database.entities.PersonalActivity
//import com.example.gradesaver.database.entities.User
//import com.example.gradesaver.notifications.NotificationReceiver
//import com.jakewharton.threetenabp.AndroidThreeTen
//import com.prolificinteractive.materialcalendarview.CalendarDay
//import com.prolificinteractive.materialcalendarview.MaterialCalendarView
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.threeten.bp.LocalDate
//import org.threeten.bp.ZoneId
//import org.threeten.bp.format.DateTimeFormatter
//import java.text.ParseException
//import java.util.Calendar
//import java.util.Date
//
//class ProfessorCalendarActivity : AppCompatActivity() {
//    private var user: User? = null
//    private lateinit var dao: AppDao
//    private val TAG = "ProfessorCalendarActivity"
//    private var lastClickTime: Long = 0
//    private lateinit var calendarView: MaterialCalendarView
//
//    companion object {
//        private const val REQUEST_NOTIFICATION_PERMISSION = 1
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        AndroidThreeTen.init(this)
//        setContentView(R.layout.activity_professor_calendar)
//
//        user = intent.getSerializableExtra("USER_DETAILS") as? User
//        if (user == null) {
//            finish()  // Close activity if no user data is found
//            return
//        }
//
//        dao = AppDatabase.getInstance(applicationContext).appDao()
//
//        calendarView = findViewById(R.id.calendarView)
//        val addPersonalActivityButton = findViewById<Button>(R.id.addPersonalActivityButton)
//
//        val today = LocalDate.now()
//        val calendarDay = CalendarDay.from(today)
//        calendarView.setCurrentDate(calendarDay)
//        calendarView.selectedDate = calendarDay
//
//        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
//        findViewById<TextView>(R.id.reminderTitle).text = "The activities for: ${formatter.format(today)}"
//
//        calendarView.setOnDateChangedListener { _, date, _ ->
//            val selectedDate = date.date
//            findViewById<TextView>(R.id.reminderTitle).text = "The activities for: ${formatter.format(selectedDate)}"
//            loadActivitiesForDate(selectedDate)
//        }
//
//        loadActivitiesForDate(today)
//
//        addPersonalActivityButton.setOnClickListener {
//            val intent = Intent(this, AddPersonalActivityActivity::class.java)
//            intent.putExtra("USER_ID", user?.userId)
//            startActivity(intent)
//        }
//
//        createNotificationChannel()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
//            }
//        }
//    }
//
//    private fun loadActivitiesForDate(date: LocalDate) {
//        Log.d(TAG, "Loading activities for date: $date")
//        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
//        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
//
//        user?.let { user ->
//            lifecycleScope.launch {
//                val activities = dao.getActivitiesForProfessorByDay(user.userId, Date(startOfDay), Date(endOfDay))
//                val personalActivities = dao.getPersonalActivitiesByDay(user.userId, Date(startOfDay), Date(endOfDay))
//                val checkedActivities = dao.getCheckedActivitiesByUser(user.userId)
//                withContext(Dispatchers.Main) {
//                    val scrollView = findViewById<ScrollView>(R.id.scrollView)
//                    val reminderTitle = findViewById<TextView>(R.id.reminderTitle)
//                    if (activities.isEmpty() && personalActivities.isEmpty()) {
//                        Log.d(TAG, "No activities found for this day.")
//                        Toast.makeText(this@ProfessorCalendarActivity, "No activities for this day.", Toast.LENGTH_SHORT).show()
//                        scrollView.visibility = View.GONE
//                        reminderTitle.visibility = View.GONE
//                    } else {
//                        Log.d(TAG, "Activities loaded: ${activities.size}, Personal activities loaded: ${personalActivities.size}")
//                        Toast.makeText(this@ProfessorCalendarActivity, "Activities loaded for the day.", Toast.LENGTH_SHORT).show()
//                        scrollView.visibility = View.VISIBLE
//                        reminderTitle.visibility = View.VISIBLE
//                        updateUIWithActivities(activities, personalActivities, checkedActivities)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun updateUIWithActivities(activities: List<Activity>, personalActivities: List<PersonalActivity>, checkedActivities: List<CheckedActivity>) {
//        Log.d(TAG, "Updating UI with activities")
//        val hourlyLayout = findViewById<LinearLayout>(R.id.hourlyLayout)
//        hourlyLayout.removeAllViews()
//
//        val mergedActivities = mutableListOf<Any>().apply {
//            addAll(activities)
//            addAll(personalActivities)
//        }.sortedBy { activity ->
//            when (activity) {
//                is Activity -> activity.dueDate.time
//                is PersonalActivity -> activity.dueDate.time
//                else -> null
//            }
//        }
//
//        mergedActivities.forEach { activity ->
//            try {
//                val dueDate = when (activity) {
//                    is Activity -> activity.dueDate
//                    is PersonalActivity -> activity.dueDate
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//                val calendar = Calendar.getInstance()
//                calendar.time = dueDate
//
//                val hour = calendar.get(Calendar.HOUR_OF_DAY)
//                val minute = calendar.get(Calendar.MINUTE)
//
//                val activityName = when (activity) {
//                    is Activity -> activity.activityName
//                    is PersonalActivity -> activity.activityName
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//                val activityType = when (activity) {
//                    is Activity -> activity.activityType
//                    is PersonalActivity -> activity.activityType
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//
//                Log.d(TAG, "Activity: $activityName, Hour: $hour")
//
//                val activityItemLayout = LinearLayout(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    ).also {
//                        it.setMargins(8, 8, 8, 8)
//                    }
//                    orientation = LinearLayout.HORIZONTAL
//                    setPadding(20, 20, 20, 20)
//                    setBackgroundColor(ContextCompat.getColor(context, getActivityColor(activityType)))
//                    setOnClickListener { handleDoubleClick(activity) }
//                }
//
//                val checkBox = CheckBox(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    val isChecked = when (activity) {
//                        is Activity -> checkedActivities.any { it.activityId == activity.activityId && it.isChecked }
//                        is PersonalActivity -> checkedActivities.any { it.personalActivityId == activity.personalActivityId && it.isChecked }
//                        else -> false
//                    }
//                    this.isChecked = isChecked
//
//                    setOnCheckedChangeListener { _, isChecked ->
//                        lifecycleScope.launch {
//                            val checkedActivity = when (activity) {
//                                is Activity -> CheckedActivity(userId = user?.userId ?: 0, activityId = activity.activityId, isChecked = isChecked)
//                                is PersonalActivity -> CheckedActivity(userId = user?.userId ?: 0, personalActivityId = activity.personalActivityId, isChecked = isChecked)
//                                else -> null
//                            }
//                            if (checkedActivity != null) {
//                                if (isChecked) {
//                                    dao.insertCheckedActivity(checkedActivity)
//                                } else {
//                                    when (activity) {
//                                        is Activity -> dao.deleteCheckedActivityByActivity(checkedActivity.userId, activity.activityId)
//                                        is PersonalActivity -> dao.deleteCheckedActivityByPersonalActivity(checkedActivity.userId, activity.personalActivityId)
//                                    }
//                                }
//                                loadActivitiesForDate(calendarView.selectedDate?.date ?: LocalDate.now())
//                            }
//                        }
//                    }
//                }
//
//                val textView = TextView(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        0,
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        1f
//                    ).also {
//                        it.setMargins(16, 0, 16, 0)
//                    }
//                    text = String.format("%02d:%02d - %s", hour, minute, activityName)
//                    textSize = 16f
//                    setTextColor(ContextCompat.getColor(context, R.color.white))
//                }
//
//                val deleteButton = ImageButton(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    setImageResource(R.drawable.baseline_trash)
//                    setBackgroundColor(Color.TRANSPARENT)
//                    setOnClickListener {
//                        if (activity is PersonalActivity) {
//                            showDeleteConfirmationDialog(activity)
//                        }
//                    }
//                }
//
//                val postponeButton = ImageButton(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    setImageResource(R.drawable.baseline_postpone)
//                    setBackgroundColor(Color.TRANSPARENT)
//                    setOnClickListener {
//                        if (activity is PersonalActivity) {
//                            postponePersonalActivity(activity)
//                        }
//                    }
//                }
//
//                activityItemLayout.addView(checkBox)
//                activityItemLayout.addView(textView)
//                if (activity is PersonalActivity) {
//                    activityItemLayout.addView(deleteButton)
//                    activityItemLayout.addView(postponeButton)
//                }
//
//                hourlyLayout.addView(activityItemLayout)
//
//                // Schedule notification
//                scheduleNotification(activityName, activityType, dueDate, activity is PersonalActivity)
//
//            } catch (e: ParseException) {
//                Log.e(TAG, "Error parsing date: ${activity}", e)
//                Toast.makeText(this, "Error parsing date: ${activity}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    private fun handleDoubleClick(activity: Any) {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastClickTime < 300) {
//            showCourseInfoPopup(activity)
//        }
//        lastClickTime = currentTime
//    }
//
//    private fun showCourseInfoPopup(activity: Any) {
//        val courseName = when (activity) {
//            is Activity -> lifecycleScope.launch {
//                val course = dao.getCourseById(activity.courseId)
//                withContext(Dispatchers.Main) {
//                    showInfoDialog(course?.courseName ?: "Unknown Course")
//                }
//            }
//            else -> return
//        }
//    }
//
//    private fun showInfoDialog(courseName: String) {
//        AlertDialog.Builder(this).apply {
//            setTitle("Course Information")
//            setMessage("Course Name: $courseName")
//            setPositiveButton("OK", null)
//        }.show()
//    }
//
//    private fun showDeleteConfirmationDialog(activity: PersonalActivity) {
//        AlertDialog.Builder(this).apply {
//            setTitle("Delete Personal Activity")
//            setMessage("Are you sure you want to delete this personal activity?")
//            setPositiveButton("Yes") { _, _ ->
//                lifecycleScope.launch {
//                    dao.deletePersonalActivity(activity)
//                    loadActivitiesForDate(LocalDate.now())
//                }
//            }
//            setNegativeButton("No", null)
//        }.show()
//    }
//
//    private fun postponePersonalActivity(activity: PersonalActivity) {
//        lifecycleScope.launch {
//            val newDueDate = Calendar.getInstance().apply {
//                time = activity.dueDate
//                add(Calendar.DAY_OF_YEAR, 1)
//            }.time
//
//            val updatedActivity = activity.copy(dueDate = newDueDate)
//            dao.updatePersonalActivity(updatedActivity)
//
//            withContext(Dispatchers.Main) {
//                Toast.makeText(this@ProfessorCalendarActivity, "Activity postponed to the next day.", Toast.LENGTH_SHORT).show()
//                loadActivitiesForDate(LocalDate.now())
//            }
//        }
//    }
//
//    private fun getActivityColor(activityType: String): Int {
//        return when (activityType) {
//            "Test" -> R.color.colorTest
//            "Midterm Exam" -> R.color.colorMidterm
//            "Essay" -> R.color.colorEssay
//            "Exam" -> R.color.colorExam
//            "Presentation" -> R.color.colorPresentation
//            "Project" -> R.color.colorProject
//            else -> R.color.defaultActivityColor
//        }
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "ActivityDeadlineChannel"
//            val descriptionText = "Channel for activity deadlines"
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel("ActivityDeadlineChannel", name, importance).apply {
//                description = descriptionText
//            }
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    @SuppressLint("ScheduleExactAlarm")
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
//

//package com.example.gradesaver
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.AlarmManager
//import android.app.AlertDialog
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Color
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.CheckBox
//import android.widget.ImageButton
//import android.widget.LinearLayout
//import android.widget.ScrollView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import com.example.gradesaver.database.AppDatabase
//import com.example.gradesaver.database.dao.AppDao
//import com.example.gradesaver.database.entities.Activity
//import com.example.gradesaver.database.entities.CheckedActivity
//import com.example.gradesaver.database.entities.PersonalActivity
//import com.example.gradesaver.database.entities.User
//import com.example.gradesaver.notifications.NotificationReceiver
//import com.jakewharton.threetenabp.AndroidThreeTen
//import com.prolificinteractive.materialcalendarview.CalendarDay
//import com.prolificinteractive.materialcalendarview.MaterialCalendarView
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.threeten.bp.LocalDate
//import org.threeten.bp.ZoneId
//import org.threeten.bp.format.DateTimeFormatter
//import java.text.ParseException
//import java.util.Calendar
//import java.util.Date
//
//class ProfessorCalendarActivity : AppCompatActivity() {
//    private var user: User? = null
//    private lateinit var dao: AppDao
//    private val TAG = "ProfessorCalendarActivity"
//    private var lastClickTime: Long = 0
//    private lateinit var calendarView: MaterialCalendarView
//
//    companion object {
//        private const val REQUEST_NOTIFICATION_PERMISSION = 1
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        AndroidThreeTen.init(this)
//        setContentView(R.layout.activity_professor_calendar)
//
//        user = intent.getSerializableExtra("USER_DETAILS") as? User
//        if (user == null) {
//            finish()  // Close activity if no user data is found
//            return
//        }
//
//        dao = AppDatabase.getInstance(applicationContext).appDao()
//
//        calendarView = findViewById(R.id.calendarView)
//        val addPersonalActivityButton = findViewById<Button>(R.id.addPersonalActivityButton)
//
//        val today = LocalDate.now()
//        val calendarDay = CalendarDay.from(today)
//        calendarView.setCurrentDate(calendarDay)
//        calendarView.selectedDate = calendarDay
//
//        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
//        findViewById<TextView>(R.id.reminderTitle).text = "The activities for: ${formatter.format(today)}"
//
//        calendarView.setOnDateChangedListener { _, date, _ ->
//            val selectedDate = date.date
//            findViewById<TextView>(R.id.reminderTitle).text = "The activities for: ${formatter.format(selectedDate)}"
//            loadActivitiesForDate(selectedDate)
//        }
//
//        loadActivitiesForDate(today)
//
//        addPersonalActivityButton.setOnClickListener {
//            val intent = Intent(this, AddPersonalActivityActivity::class.java)
//            intent.putExtra("USER_ID", user?.userId)
//            startActivity(intent)
//        }
//
//        createNotificationChannel()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
//            }
//        }
//    }
//
//    private fun loadActivitiesForDate(date: LocalDate) {
//        Log.d(TAG, "Loading activities for date: $date")
//        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
//        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
//
//        user?.let { user ->
//            lifecycleScope.launch {
//                val activities = dao.getActivitiesForProfessorByDay(user.userId, Date(startOfDay), Date(endOfDay))
//                val personalActivities = dao.getPersonalActivitiesByDay(user.userId, Date(startOfDay), Date(endOfDay))
//                val checkedActivities = dao.getCheckedActivitiesByUser(user.userId)
//                withContext(Dispatchers.Main) {
//                    val scrollView = findViewById<ScrollView>(R.id.scrollView)
//                    val reminderTitle = findViewById<TextView>(R.id.reminderTitle)
//                    if (activities.isEmpty() && personalActivities.isEmpty()) {
//                        Log.d(TAG, "No activities found for this day.")
//                        Toast.makeText(this@ProfessorCalendarActivity, "No activities for this day.", Toast.LENGTH_SHORT).show()
//                        scrollView.visibility = View.GONE
//                        reminderTitle.visibility = View.GONE
//                    } else {
//                        Log.d(TAG, "Activities loaded: ${activities.size}, Personal activities loaded: ${personalActivities.size}")
//                        Toast.makeText(this@ProfessorCalendarActivity, "Activities loaded for the day.", Toast.LENGTH_SHORT).show()
//                        scrollView.visibility = View.VISIBLE
//                        reminderTitle.visibility = View.VISIBLE
//                        updateUIWithActivities(activities, personalActivities, checkedActivities)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun updateUIWithActivities(activities: List<Activity>, personalActivities: List<PersonalActivity>, checkedActivities: List<CheckedActivity>) {
//        Log.d(TAG, "Updating UI with activities")
//        val hourlyLayout = findViewById<LinearLayout>(R.id.hourlyLayout)
//        hourlyLayout.removeAllViews()
//
//        val mergedActivities = mutableListOf<Any>().apply {
//            addAll(activities)
//            addAll(personalActivities)
//        }.sortedBy { activity ->
//            when (activity) {
//                is Activity -> activity.dueDate.time
//                is PersonalActivity -> activity.dueDate.time
//                else -> null
//            }
//        }
//
//        mergedActivities.forEach { activity ->
//            try {
//                val dueDate = when (activity) {
//                    is Activity -> activity.dueDate
//                    is PersonalActivity -> activity.dueDate
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//                val calendar = Calendar.getInstance()
//                calendar.time = dueDate
//
//                val hour = calendar.get(Calendar.HOUR_OF_DAY)
//                val minute = calendar.get(Calendar.MINUTE)
//
//                val activityName = when (activity) {
//                    is Activity -> activity.activityName
//                    is PersonalActivity -> activity.activityName
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//                val activityType = when (activity) {
//                    is Activity -> activity.activityType
//                    is PersonalActivity -> activity.activityType
//                    else -> throw ParseException("Unknown activity type", 0)
//                }
//
//                Log.d(TAG, "Activity: $activityName, Hour: $hour")
//
//                val activityItemLayout = LinearLayout(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    ).also {
//                        it.setMargins(8, 8, 8, 8)
//                    }
//                    orientation = LinearLayout.HORIZONTAL
//                    setPadding(20, 20, 20, 20)
//                    setBackgroundColor(ContextCompat.getColor(context, getActivityColor(activityType)))
//                    setOnClickListener { handleDoubleClick(activity) }
//                }
//
//                val checkBox = CheckBox(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    val isChecked = when (activity) {
//                        is Activity -> checkedActivities.any { it.activityId == activity.activityId && it.isChecked }
//                        is PersonalActivity -> checkedActivities.any { it.personalActivityId == activity.personalActivityId && it.isChecked }
//                        else -> false
//                    }
//                    this.isChecked = isChecked
//
//                    setOnCheckedChangeListener { _, isChecked ->
//                        lifecycleScope.launch {
//                            val checkedActivity = when (activity) {
//                                is Activity -> CheckedActivity(userId = user?.userId ?: 0, activityId = activity.activityId, isChecked = isChecked)
//                                is PersonalActivity -> CheckedActivity(userId = user?.userId ?: 0, personalActivityId = activity.personalActivityId, isChecked = isChecked)
//                                else -> null
//                            }
//                            if (checkedActivity != null) {
//                                if (isChecked) {
//                                    dao.insertCheckedActivity(checkedActivity)
//                                } else {
//                                    when (activity) {
//                                        is Activity -> dao.deleteCheckedActivityByActivity(checkedActivity.userId, activity.activityId)
//                                        is PersonalActivity -> dao.deleteCheckedActivityByPersonalActivity(checkedActivity.userId, activity.personalActivityId)
//                                    }
//                                }
//                                loadActivitiesForDate(calendarView.selectedDate?.date ?: LocalDate.now())
//                            }
//                        }
//                    }
//                }
//
//                val textView = TextView(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        0,
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        1f
//                    ).also {
//                        it.setMargins(16, 0, 16, 0)
//                    }
//                    text = String.format("%02d:%02d - %s", hour, minute, activityName)
//                    textSize = 16f
//                    setTextColor(ContextCompat.getColor(context, R.color.white))
//                }
//
//                val deleteButton = ImageButton(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    setImageResource(R.drawable.baseline_trash)
//                    setBackgroundColor(Color.TRANSPARENT)
//                    setOnClickListener {
//                        if (activity is PersonalActivity) {
//                            showDeleteConfirmationDialog(activity)
//                        }
//                    }
//                }
//
//                val postponeButton = ImageButton(this).apply {
//                    layoutParams = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    setImageResource(R.drawable.baseline_postpone)
//                    setBackgroundColor(Color.TRANSPARENT)
//                    setOnClickListener {
//                        if (activity is PersonalActivity) {
//                            postponePersonalActivity(activity)
//                        }
//                    }
//                }
//
//                activityItemLayout.addView(checkBox)
//                activityItemLayout.addView(textView)
//                if (activity is PersonalActivity) {
//                    activityItemLayout.addView(deleteButton)
//                    activityItemLayout.addView(postponeButton)
//                }
//
//                hourlyLayout.addView(activityItemLayout)
//
//                // Schedule notification for both types of activities
//                scheduleNotification(activityName, activityType, dueDate, activity is PersonalActivity)
//
//            } catch (e: ParseException) {
//                Log.e(TAG, "Error parsing date: ${activity}", e)
//                Toast.makeText(this, "Error parsing date: ${activity}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    private fun handleDoubleClick(activity: Any) {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastClickTime < 300) {
//            showCourseInfoPopup(activity)
//        }
//        lastClickTime = currentTime
//    }
//
//    private fun showCourseInfoPopup(activity: Any) {
//        val courseName = when (activity) {
//            is Activity -> lifecycleScope.launch {
//                val course = dao.getCourseById(activity.courseId)
//                withContext(Dispatchers.Main) {
//                    showInfoDialog(course?.courseName ?: "Unknown Course")
//                }
//            }
//            else -> return
//        }
//    }
//
//    private fun showInfoDialog(courseName: String) {
//        AlertDialog.Builder(this).apply {
//            setTitle("Course Information")
//            setMessage("Course Name: $courseName")
//            setPositiveButton("OK", null)
//        }.show()
//    }
//
//    private fun showDeleteConfirmationDialog(activity: PersonalActivity) {
//        AlertDialog.Builder(this).apply {
//            setTitle("Delete Personal Activity")
//            setMessage("Are you sure you want to delete this personal activity?")
//            setPositiveButton("Yes") { _, _ ->
//                lifecycleScope.launch {
//                    dao.deletePersonalActivity(activity)
//                    loadActivitiesForDate(LocalDate.now())
//                }
//            }
//            setNegativeButton("No", null)
//        }.show()
//    }
//
//    private fun postponePersonalActivity(activity: PersonalActivity) {
//        lifecycleScope.launch {
//            val newDueDate = Calendar.getInstance().apply {
//                time = activity.dueDate
//                add(Calendar.DAY_OF_YEAR, 1)
//            }.time
//
//            val updatedActivity = activity.copy(dueDate = newDueDate)
//            dao.updatePersonalActivity(updatedActivity)
//
//            withContext(Dispatchers.Main) {
//                Toast.makeText(this@ProfessorCalendarActivity, "Activity postponed to the next day.", Toast.LENGTH_SHORT).show()
//                loadActivitiesForDate(LocalDate.now())
//            }
//        }
//    }
//
//    private fun getActivityColor(activityType: String): Int {
//        return when (activityType) {
//            "Test" -> R.color.colorTest
//            "Midterm Exam" -> R.color.colorMidterm
//            "Essay" -> R.color.colorEssay
//            "Exam" -> R.color.colorExam
//            "Presentation" -> R.color.colorPresentation
//            "Project" -> R.color.colorProject
//            else -> R.color.defaultActivityColor
//        }
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "ActivityDeadlineChannel"
//            val descriptionText = "Channel for activity deadlines"
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel("ActivityDeadlineChannel", name, importance).apply {
//                description = descriptionText
//            }
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    @SuppressLint("ScheduleExactAlarm")
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

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.CheckedActivity
import com.example.gradesaver.database.entities.PersonalActivity
import com.example.gradesaver.database.entities.User
import com.jakewharton.threetenabp.AndroidThreeTen
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.ParseException
import java.util.Calendar
import java.util.Date

class ProfessorCalendarActivity : AppCompatActivity() {
    private var user: User? = null
    private lateinit var dao: AppDao
    private val TAG = "ProfessorCalendarActivity"
    private var lastClickTime: Long = 0
    private lateinit var calendarView: MaterialCalendarView

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_professor_calendar)

        user = intent.getSerializableExtra("USER_DETAILS") as? User
        if (user == null) {
            finish()
            return
        }

        dao = AppDatabase.getInstance(applicationContext).appDao()

        calendarView = findViewById(R.id.calendarView)
        val addPersonalActivityButton = findViewById<Button>(R.id.addPersonalActivityButton)

        val today = LocalDate.now()
        val calendarDay = CalendarDay.from(today)
        calendarView.setCurrentDate(calendarDay)
        calendarView.selectedDate = calendarDay

        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
        findViewById<TextView>(R.id.reminderTitle).text = "The activities for: ${formatter.format(today)}"

        calendarView.setOnDateChangedListener { _, date, _ ->
            val selectedDate = date.date
            findViewById<TextView>(R.id.reminderTitle).text = "The activities for: ${formatter.format(selectedDate)}"
            loadActivitiesForDate(selectedDate)
        }

        loadActivitiesForDate(today)

        addPersonalActivityButton.setOnClickListener {
            val intent = Intent(this, AddPersonalActivityActivity::class.java)
            intent.putExtra("USER_ID", user?.userId)
            startActivity(intent)
        }
    }

    private fun loadActivitiesForDate(date: LocalDate) {
        Log.d(TAG, "Loading activities for date: $date")
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        user?.let { user ->
            lifecycleScope.launch {
                val activities = dao.getActivitiesForProfessorByDay(user.userId, Date(startOfDay), Date(endOfDay))
                val personalActivities = dao.getPersonalActivitiesByDay(user.userId, Date(startOfDay), Date(endOfDay))
                val checkedActivities = dao.getCheckedActivitiesByUser(user.userId)
                withContext(Dispatchers.Main) {
                    val scrollView = findViewById<ScrollView>(R.id.scrollView)
                    val reminderTitle = findViewById<TextView>(R.id.reminderTitle)
                    if (activities.isEmpty() && personalActivities.isEmpty()) {
                        Log.d(TAG, "No activities found for this day.")
                        Toast.makeText(this@ProfessorCalendarActivity, "No activities for this day.", Toast.LENGTH_SHORT).show()
                        scrollView.visibility = View.GONE
                        reminderTitle.visibility = View.GONE
                    } else {
                        Log.d(TAG, "Activities loaded: ${activities.size}, Personal activities loaded: ${personalActivities.size}")
                        Toast.makeText(this@ProfessorCalendarActivity, "Activities loaded for the day.", Toast.LENGTH_SHORT).show()
                        scrollView.visibility = View.VISIBLE
                        reminderTitle.visibility = View.VISIBLE
                        updateUIWithActivities(activities, personalActivities, checkedActivities)
                    }
                }
            }
        }
    }

    private fun updateUIWithActivities(activities: List<Activity>, personalActivities: List<PersonalActivity>, checkedActivities: List<CheckedActivity>) {
        Log.d(TAG, "Updating UI with activities")
        val hourlyLayout = findViewById<LinearLayout>(R.id.hourlyLayout)
        hourlyLayout.removeAllViews()

        val mergedActivities = mutableListOf<Any>().apply {
            addAll(activities)
            addAll(personalActivities)
        }.sortedBy { activity ->
            when (activity) {
                is Activity -> activity.dueDate.time
                is PersonalActivity -> activity.dueDate.time
                else -> null
            }
        }

        mergedActivities.forEach { activity ->
            try {
                val dueDate = when (activity) {
                    is Activity -> activity.dueDate
                    is PersonalActivity -> activity.dueDate
                    else -> throw ParseException("Unknown activity type", 0)
                }
                val calendar = Calendar.getInstance()
                calendar.time = dueDate

                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val activityName = when (activity) {
                    is Activity -> activity.activityName
                    is PersonalActivity -> activity.activityName
                    else -> throw ParseException("Unknown activity type", 0)
                }
                val activityType = when (activity) {
                    is Activity -> activity.activityType
                    is PersonalActivity -> activity.activityType
                    else -> throw ParseException("Unknown activity type", 0)
                }

                Log.d(TAG, "Activity: $activityName, Hour: $hour")

                val activityItemLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also {
                        it.setMargins(8, 8, 8, 8)
                    }
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(20, 20, 20, 20)
                    setBackgroundColor(ContextCompat.getColor(context, getActivityColor(activityType)))
                    setOnClickListener { handleDoubleClick(activity) }
                }

                val checkBox = CheckBox(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    val isChecked = when (activity) {
                        is Activity -> checkedActivities.any { it.activityId == activity.activityId && it.isChecked }
                        is PersonalActivity -> checkedActivities.any { it.personalActivityId == activity.personalActivityId && it.isChecked }
                        else -> false
                    }
                    this.isChecked = isChecked

                    setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            val checkedActivity = when (activity) {
                                is Activity -> CheckedActivity(userId = user?.userId ?: 0, activityId = activity.activityId, isChecked = isChecked)
                                is PersonalActivity -> CheckedActivity(userId = user?.userId ?: 0, personalActivityId = activity.personalActivityId, isChecked = isChecked)
                                else -> null
                            }
                            if (checkedActivity != null) {
                                if (isChecked) {
                                    dao.insertCheckedActivity(checkedActivity)
                                } else {
                                    when (activity) {
                                        is Activity -> dao.deleteCheckedActivityByActivity(checkedActivity.userId, activity.activityId)
                                        is PersonalActivity -> dao.deleteCheckedActivityByPersonalActivity(checkedActivity.userId, activity.personalActivityId)
                                    }
                                }
                                loadActivitiesForDate(calendarView.selectedDate?.date ?: LocalDate.now())
                            }
                        }
                    }
                }

                val textView = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).also {
                        it.setMargins(16, 0, 16, 0)
                    }
                    text = String.format("%02d:%02d - %s", hour, minute, activityName)
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                }

                val deleteButton = ImageButton(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setImageResource(R.drawable.baseline_trash)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        if (activity is PersonalActivity) {
                            showDeleteConfirmationDialog(activity)
                        }
                    }
                }

                val postponeButton = ImageButton(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setImageResource(R.drawable.baseline_postpone)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        if (activity is PersonalActivity) {
                            postponePersonalActivity(activity)
                        }
                    }
                }

                activityItemLayout.addView(checkBox)
                activityItemLayout.addView(textView)
                if (activity is PersonalActivity) {
                    activityItemLayout.addView(deleteButton)
                    activityItemLayout.addView(postponeButton)
                }

                hourlyLayout.addView(activityItemLayout)

            } catch (e: ParseException) {
                Log.e(TAG, "Error parsing date: ${activity}", e)
                Toast.makeText(this, "Error parsing date: ${activity}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleDoubleClick(activity: Any) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 300) {
            showCourseInfoPopup(activity)
        }
        lastClickTime = currentTime
    }

    private fun showCourseInfoPopup(activity: Any) {
        val courseName = when (activity) {
            is Activity -> lifecycleScope.launch {
                val course = dao.getCourseById(activity.courseId)
                withContext(Dispatchers.Main) {
                    showInfoDialog(course?.courseName ?: "Unknown Course")
                }
            }
            else -> return
        }
    }

    private fun showInfoDialog(courseName: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Course Information")
            setMessage("Course Name: $courseName")
            setPositiveButton("OK", null)
        }.show()
    }

    private fun showDeleteConfirmationDialog(activity: PersonalActivity) {
        AlertDialog.Builder(this).apply {
            setTitle("Delete Personal Activity")
            setMessage("Are you sure you want to delete this personal activity?")
            setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    dao.deletePersonalActivity(activity)
                    loadActivitiesForDate(LocalDate.now())
                }
            }
            setNegativeButton("No", null)
        }.show()
    }

    private fun postponePersonalActivity(activity: PersonalActivity) {
        lifecycleScope.launch {
            val newDueDate = Calendar.getInstance().apply {
                time = activity.dueDate
                add(Calendar.DAY_OF_YEAR, 1)
            }.time

            val updatedActivity = activity.copy(dueDate = newDueDate)
            dao.updatePersonalActivity(updatedActivity)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfessorCalendarActivity, "Activity postponed to the next day.", Toast.LENGTH_SHORT).show()
                loadActivitiesForDate(LocalDate.now())
            }
        }
    }

    private fun getActivityColor(activityType: String): Int {
        return when (activityType) {
            "Test" -> R.color.colorTest
            "Midterm Exam" -> R.color.colorMidterm
            "Essay" -> R.color.colorEssay
            "Exam" -> R.color.colorExam
            "Presentation" -> R.color.colorPresentation
            "Project" -> R.color.colorProject
            else -> R.color.defaultActivityColor
        }
    }
}

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
import com.example.gradesaver.dataClasses.ReminderWithActivity
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.CheckedActivity
import com.example.gradesaver.database.entities.PersonalActivity
import com.example.gradesaver.database.entities.Reminder
import com.example.gradesaver.database.entities.User
import com.example.gradesaver.decorators.ActivityDecorator
import com.jakewharton.threetenabp.AndroidThreeTen
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.ParseException
import java.util.Calendar
import java.util.Date

class StudentCalendarActivity : AppCompatActivity() {
    private var user: User? = null
    private lateinit var dao: AppDao
    private lateinit var calendarView: MaterialCalendarView
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_student_calendar)

        user = intent.getSerializableExtra("USER_DETAILS") as? User
        if (user == null) {
            finish() // Close activity if no user data is found
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
        loadAllActivities()

        addPersonalActivityButton.setOnClickListener {
            val selectedDate = calendarView.selectedDate?.date ?: today
            val intent = Intent(this, AddPersonalActivityActivity::class.java)
            intent.putExtra("USER_ID", user?.userId)
            intent.putExtra("SELECTED_DATE", selectedDate.toString())
            startActivity(intent)
        }
    }

    private fun loadAllActivities() {
        user?.let { user ->
            lifecycleScope.launch {
                val allActivities = dao.getAllActivitiesByUser(user.userId)
                val allPersonalActivities = dao.getAllPersonalActivitiesByUser(user.userId)
                val allReminders = dao.getAllRemindersByUser(user.userId)
                val universityDates = mutableSetOf<CalendarDay>()
                val personalDates = mutableSetOf<CalendarDay>()
                val bothDates = mutableSetOf<CalendarDay>()

                val activityDates = allActivities.map {
                    CalendarDay.from(
                        ZonedDateTime.ofInstant(
                            org.threeten.bp.Instant.ofEpochMilli(it.dueDate.time),
                            ZoneId.systemDefault()
                        ).toLocalDate()
                    )
                }.toSet()

                val personalActivityDates = allPersonalActivities.map {
                    CalendarDay.from(
                        ZonedDateTime.ofInstant(
                            org.threeten.bp.Instant.ofEpochMilli(it.dueDate.time),
                            ZoneId.systemDefault()
                        ).toLocalDate()
                    )
                }.toSet()

                val reminderDates = allReminders.map {
                    CalendarDay.from(
                        ZonedDateTime.ofInstant(
                            org.threeten.bp.Instant.ofEpochMilli(it.reminderDate.time),
                            ZoneId.systemDefault()
                        ).toLocalDate()
                    )
                }.toSet()

                activityDates.forEach { date ->
                    if (personalActivityDates.contains(date) || reminderDates.contains(date)) {
                        bothDates.add(date)
                    } else {
                        universityDates.add(date)
                    }
                }

                personalActivityDates.forEach { date ->
                    if (!activityDates.contains(date) && !reminderDates.contains(date)) {
                        personalDates.add(date)
                    }
                }

                reminderDates.forEach { date ->
                    if (!activityDates.contains(date) && !personalActivityDates.contains(date)) {
                        universityDates.add(date)
                    } else {
                        bothDates.add(date)
                    }
                }

                withContext(Dispatchers.Main) {
                    updateCalendarDecorators(universityDates, personalDates, bothDates)
                }
            }
        }
    }

    private fun loadActivitiesForDate(date: LocalDate) {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        user?.let { user ->
            lifecycleScope.launch {
                val activities = dao.getTodaysActivitiesByUser(user.userId, Date(startOfDay), Date(endOfDay))
                val personalActivities = dao.getTodaysPersonalActivitiesByUser(user.userId, Date(startOfDay), Date(endOfDay))
                val remindersWithActivities = dao.getRemindersWithActivityForUserByDay(user.userId, Date(startOfDay), Date(endOfDay))
                val checkedActivities = dao.getCheckedActivitiesByUser(user.userId)
                withContext(Dispatchers.Main) {
                    val scrollView = findViewById<ScrollView>(R.id.scrollView)
                    val reminderTitle = findViewById<TextView>(R.id.reminderTitle)
                    if (activities.isEmpty() && personalActivities.isEmpty() && remindersWithActivities.isEmpty()) {
                        scrollView.visibility = View.GONE
                        reminderTitle.visibility = View.GONE
                    } else {
                        scrollView.visibility = View.VISIBLE
                        reminderTitle.visibility = View.VISIBLE
                        updateUIWithActivities(activities, personalActivities, remindersWithActivities, checkedActivities)
                    }
                }
            }
        }
    }

    private fun updateUIWithActivities(
        activities: List<Activity>,
        personalActivities: List<PersonalActivity>,
        reminders: List<ReminderWithActivity>,
        checkedActivities: List<CheckedActivity>
    ) {
        val TAG = "UPDATE"
        Log.d(TAG, "Updating UI with activities")
        val hourlyLayout = findViewById<LinearLayout>(R.id.hourlyLayout)
        hourlyLayout.removeAllViews()

        val mergedActivities = mutableListOf<Any>().apply {
            addAll(activities)
            addAll(personalActivities)
            addAll(reminders)
        }.sortedBy { activity ->
            when (activity) {
                is Activity -> activity.dueDate.time
                is PersonalActivity -> activity.dueDate.time
                is ReminderWithActivity -> activity.reminder.reminderDate.time
                else -> null
            }
        }

        mergedActivities.forEach { activity ->
            try {
                val dueDate = when (activity) {
                    is Activity -> activity.dueDate
                    is PersonalActivity -> activity.dueDate
                    is ReminderWithActivity -> activity.reminder.reminderDate
                    else -> throw ParseException("Unknown activity type", 0)
                }
                val calendar = Calendar.getInstance()
                calendar.time = dueDate

                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val activityName = when (activity) {
                    is Activity -> activity.activityName
                    is PersonalActivity -> activity.activityName
                    is ReminderWithActivity -> activity.reminder.reminderMessage
                    else -> throw ParseException("Unknown activity type", 0)
                }
                val activityType = when (activity) {
                    is Activity -> activity.activityType
                    is PersonalActivity -> activity.activityType
                    is ReminderWithActivity -> activity.activity?.activityType
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
                        is ReminderWithActivity -> checkedActivities.any { it.reminderId == activity.reminder.reminderId && it.isChecked }
                        else -> false
                    }
                    this.isChecked = isChecked

                    setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            val checkedActivity = when (activity) {
                                is Activity -> CheckedActivity(userId = user?.userId ?: 0, activityId = activity.activityId, isChecked = isChecked)
                                is PersonalActivity -> CheckedActivity(userId = user?.userId ?: 0, personalActivityId = activity.personalActivityId, isChecked = isChecked)
                                is ReminderWithActivity -> CheckedActivity(userId = user?.userId ?: 0, reminderId = activity.reminder.reminderId, isChecked = isChecked)
                                else -> null
                            }
                            if (checkedActivity != null) {
                                if (isChecked) {
                                    Log.d(TAG, "Inserting CheckedActivity: $checkedActivity")
                                    dao.insertCheckedActivity(checkedActivity)
                                } else {
                                    Log.d(TAG, "Deleting CheckedActivity: $checkedActivity")
                                    when (activity) {
                                        is Activity -> dao.deleteCheckedActivityByActivity(checkedActivity.userId, activity.activityId)
                                        is PersonalActivity -> dao.deleteCheckedActivityByPersonalActivity(checkedActivity.userId, activity.personalActivityId)
                                        is ReminderWithActivity -> dao.deleteCheckedActivityByReminder(checkedActivity.userId, activity.reminder.reminderId)
                                    }
                                }
                                loadActivitiesForDate(calendarView.selectedDate?.date ?: LocalDate.now())
                            } else {
                                Log.e(TAG, "CheckedActivity is null")
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
                        when (activity) {
                            is PersonalActivity -> showDeleteConfirmationDialog(activity)
                            is ReminderWithActivity -> showDeleteReminderConfirmationDialog(activity.reminder)
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
                        when (activity) {
                            is PersonalActivity -> postponePersonalActivity(activity)
                            is ReminderWithActivity -> postponeReminder(activity.reminder)
                        }
                    }
                }

                activityItemLayout.addView(checkBox)
                activityItemLayout.addView(textView)
                if (activity is PersonalActivity || activity is ReminderWithActivity) {
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
        when (activity) {
            is Activity -> lifecycleScope.launch {
                val course = dao.getCourseById(activity.courseId)
                withContext(Dispatchers.Main) {
                    showInfoDialog(course?.courseName ?: "Unknown Course", activity.activityName)
                }
            }
            is ReminderWithActivity -> lifecycleScope.launch {
                val course = activity.activity?.let { dao.getCourseById(it.courseId) }
                withContext(Dispatchers.Main) {
                    activity.activity?.let { showInfoDialog(course?.courseName ?: "Unknown Course", it.activityName) }
                }
            }
            else -> return
        }
    }

    private fun showInfoDialog(courseName: String, activityName: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Course Information")
            setMessage("Course Name: $courseName\nActivity Name: $activityName")
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

    private fun showDeleteReminderConfirmationDialog(reminder: Reminder) {
        AlertDialog.Builder(this).apply {
            setTitle("Delete Reminder")
            setMessage("Are you sure you want to delete this reminder?")
            setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    dao.deleteReminder(reminder)
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
                Toast.makeText(this@StudentCalendarActivity, "Activity postponed to the next day.", Toast.LENGTH_SHORT).show()
                loadActivitiesForDate(LocalDate.now())
            }
        }
    }

    private fun postponeReminder(reminder: Reminder) {
        lifecycleScope.launch {
            val newReminderDate = Calendar.getInstance().apply {
                time = reminder.reminderDate
                add(Calendar.DAY_OF_YEAR, 1)
            }.time

            val updatedReminder = reminder.copy(reminderDate = newReminderDate)
            dao.updateReminder(updatedReminder)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@StudentCalendarActivity, "Reminder postponed to the next day.", Toast.LENGTH_SHORT).show()
                loadActivitiesForDate(LocalDate.now())
            }
        }
    }

    private fun getActivityColor(activityType: String?): Int {
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

    private fun updateCalendarDecorators(universityDates: Set<CalendarDay>, personalDates: Set<CalendarDay>, bothDates: Set<CalendarDay>) {
        // Clear previous decorators
        calendarView.removeDecorators()

        // Add decorators
        if (universityDates.isNotEmpty()) {
            val universityColor = ContextCompat.getColor(this, R.color.teal)
            Log.d("stud calendar", "Adding university decorator with color: $universityColor")
            calendarView.addDecorator(ActivityDecorator(universityColor, universityDates))
        }
        if (personalDates.isNotEmpty()) {
            val personalColor = ContextCompat.getColor(this, R.color.defaultActivityColor)
            Log.d("stud calendar", "Adding personal decorator with color: $personalColor")
            calendarView.addDecorator(ActivityDecorator(personalColor, personalDates))
        }
        if (bothDates.isNotEmpty()) {
            val bothColor = ContextCompat.getColor(this, R.color.colorMidterm)
            Log.d("stud calendar", "Adding both decorator with color: $bothColor")
            calendarView.addDecorator(ActivityDecorator(bothColor, bothDates))
        }

        // Refresh the calendar view
        calendarView.invalidateDecorators()
    }
}

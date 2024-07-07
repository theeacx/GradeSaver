package com.example.gradesaver

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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
import com.example.gradesaver.database.entities.ExportedActivity
import com.example.gradesaver.database.entities.PersonalActivity
import com.example.gradesaver.database.entities.User
import com.example.gradesaver.decorators.ActivityDecorator
import com.example.gradesaver.decorators.BoldDecorator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
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

class ProfessorCalendarActivity : AppCompatActivity() {
    private var user: User? = null
    private lateinit var dao: AppDao
    private val TAG = "ProfessorCalendarActivity"
    private var lastClickTime: Long = 0
    private lateinit var calendarView: MaterialCalendarView

    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1
        private const val RC_SIGN_IN = 9001
    }

    @SuppressLint("WrongViewCast")
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
        val showLegendButton = findViewById<Button>(R.id.showLegendButton)
        val shareIcon = findViewById<ImageView>(R.id.shareIcon)

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

        loadAllActivities()

        addPersonalActivityButton.setOnClickListener {
            val intent = Intent(this, AddPersonalActivityActivity::class.java)
            intent.putExtra("USER_ID", user?.userId)
            startActivity(intent)
        }

        showLegendButton.setOnClickListener {
            showLegendDialog()
        }

        shareIcon.setOnClickListener {
            showExportDialog()
        }

        configureGoogleSignIn()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        Log.d("SignInConfig", "Google Sign-In configured.")
    }

    private fun showExportDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_export_calendar, null)
        builder.setView(dialogView)

        val emailTextView = dialogView.findViewById<EditText>(R.id.emailTextView)
        val useDifferentEmailCheckBox = dialogView.findViewById<CheckBox>(R.id.useDifferentEmailCheckBox)
        val exportButton = dialogView.findViewById<Button>(R.id.exportButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        val currentUserEmail = GoogleSignIn.getLastSignedInAccount(this)?.email ?: "user@example.com"
        emailTextView.setText(currentUserEmail)
        emailTextView.isEnabled = false

        useDifferentEmailCheckBox.setOnCheckedChangeListener { _, isChecked ->
            emailTextView.isEnabled = isChecked
        }

        val alertDialog = builder.create()

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        exportButton.setOnClickListener {
            val email = emailTextView.text.toString()
            exportToGoogleCalendar(email)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun exportToGoogleCalendar(email: String) {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            lifecycleScope.launch {
                val allActivities = dao.getActivitiesForProfessorByDay(user!!.userId, Date(0), Date(Long.MAX_VALUE))
                val allPersonalActivities = dao.getPersonalActivitiesByUser(user!!.userId)
                val exportedActivities = dao.getAllExportedActivities()

                val exportedActivityIds = exportedActivities.filter { it.activityType == "university" }.map { it.activityId }.toSet()
                val exportedPersonalActivityIds = exportedActivities.filter { it.activityType == "personal" }.map { it.activityId }.toSet()

                withContext(Dispatchers.IO) {
                    try {
                        val credential = GoogleAccountCredential.usingOAuth2(
                            this@ProfessorCalendarActivity, listOf(CalendarScopes.CALENDAR)
                        ).setSelectedAccount(account.account)

                        val service = com.google.api.services.calendar.Calendar.Builder(
                            NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            credential
                        ).setApplicationName("GradeSaver").build()

                        // Export all university activities
                        for (activity in allActivities) {
                            if (activity.activityId !in exportedActivityIds) {
                                val startDateTime = toDateTime(activity.dueDate)
                                val endDateTime = toDateTime(activity.dueDate) // Adjust this if your activities have specific end times

                                val event = Event()
                                    .setSummary(activity.activityName)
                                    .setDescription("University Activity")
                                    .setStart(EventDateTime().setDateTime(startDateTime))
                                    .setEnd(EventDateTime().setDateTime(endDateTime))

                                service.events().insert("primary", event).execute()
                                dao.insertExportedActivity(ExportedActivity(0, activity.activityId, "university"))
                            }
                        }

                        // Export all personal activities
                        for (personalActivity in allPersonalActivities) {
                            if (personalActivity.personalActivityId !in exportedPersonalActivityIds) {
                                val startDateTime = toDateTime(personalActivity.dueDate)
                                val endDateTime = toDateTime(personalActivity.dueDate) // Adjust this if your activities have specific end times

                                val event = Event()
                                    .setSummary(personalActivity.activityName)
                                    .setDescription("Personal Activity")
                                    .setStart(EventDateTime().setDateTime(startDateTime))
                                    .setEnd(EventDateTime().setDateTime(endDateTime))

                                service.events().insert("primary", event).execute()
                                dao.insertExportedActivity(ExportedActivity(0, personalActivity.personalActivityId, "personal"))
                            }
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfessorCalendarActivity, "Exported to Google Calendar", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfessorCalendarActivity, "Error exporting to Google Calendar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            googleSignInClient.signInIntent.also { signInIntent ->
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            Log.d("SignInResult", "Received result from Google Sign-In.")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    Log.d("SignInSuccess", "Successfully signed in: ${account.email}")
                    exportToGoogleCalendar(account.email ?: "user@example.com")
                }
            } catch (e: ApiException) {
                e.printStackTrace()
                Log.e("SignInError", "Sign in failed: ${e.statusCode}")
                Toast.makeText(this, "Sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLegendDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Activity Legend")

        val legendLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val legendItems = listOf(
            Pair("Teal", "University Activities"),
            Pair("Default", "Personal Activities"),
            Pair("Midterm", "Both University and Personal Activities")
        )

        legendItems.forEach { (colorName, description) ->
            val color = when (colorName) {
                "Teal" -> ContextCompat.getColor(this@ProfessorCalendarActivity, R.color.teal)
                "Default" -> ContextCompat.getColor(this@ProfessorCalendarActivity, R.color.defaultActivityColor)
                "Midterm" -> ContextCompat.getColor(this@ProfessorCalendarActivity, R.color.both)
                else -> Color.TRANSPARENT
            }

            val itemLayout = LinearLayout(this@ProfessorCalendarActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val colorView = View(this@ProfessorCalendarActivity).apply {
                setBackgroundColor(color)
                layoutParams = LinearLayout.LayoutParams(50, 50).apply {
                    setMargins(0, 0, 16, 0)
                }
            }

            val descriptionView = TextView(this@ProfessorCalendarActivity).apply {
                text = description
                textSize = 16f
                setTextColor(Color.BLACK)
            }

            itemLayout.addView(colorView)
            itemLayout.addView(descriptionView)
            legendLayout.addView(itemLayout)
        }

        builder.setView(legendLayout)
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    private fun loadAllActivities() {
        user?.let { user ->
            lifecycleScope.launch {
                val allActivities = dao.getActivitiesForProfessorByDay(user.userId, Date(0), Date(Long.MAX_VALUE))
                val allPersonalActivities = dao.getPersonalActivitiesByUser(user.userId)
                val universityDates = mutableSetOf<CalendarDay>()
                val personalDates = mutableSetOf<CalendarDay>()
                val bothDates = mutableSetOf<CalendarDay>()

                val activityDates = allActivities.map {
                    ZonedDateTime.ofInstant(
                        org.threeten.bp.Instant.ofEpochMilli(it.dueDate.time),
                        ZoneId.systemDefault()
                    ).toLocalDate()
                }.toSet()

                val personalActivityDates = allPersonalActivities.map {
                    ZonedDateTime.ofInstant(
                        org.threeten.bp.Instant.ofEpochMilli(it.dueDate.time),
                        ZoneId.systemDefault()
                    ).toLocalDate()
                }.toSet()

                activityDates.forEach { date ->
                    val calendarDay = CalendarDay.from(date)
                    if (personalActivityDates.contains(date)) {
                        bothDates.add(calendarDay)
                    } else {
                        universityDates.add(calendarDay)
                    }
                }

                personalActivityDates.forEach { date ->
                    val calendarDay = CalendarDay.from(date)
                    if (!activityDates.contains(date)) {
                        personalDates.add(calendarDay)
                    }
                }

                withContext(Dispatchers.Main) {
                    updateCalendarDecorators(universityDates, personalDates, bothDates)
                }
            }
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
        when (activity) {
            is Activity -> lifecycleScope.launch {
                val course = dao.getCourseById(activity.courseId)
                withContext(Dispatchers.Main) {
                    showInfoDialog(course?.courseName ?: "Unknown Course", activity.activityName)
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

    private fun updateCalendarDecorators(universityDates: Set<CalendarDay>, personalDates: Set<CalendarDay>, bothDates: Set<CalendarDay>) {
        // Clear previous decorators
        calendarView.removeDecorators()

        val textSize = resources.getDimension(R.dimen.calendar_day_text_size)
        val isBold = true

        // Add bold decorator for all days
        calendarView.addDecorator(BoldDecorator(textSize, isBold))

        // Add decorators for specific dates
        if (universityDates.isNotEmpty()) {
            val universityColor = ContextCompat.getColor(this, R.color.teal)
            Log.d(TAG, "Adding university decorator with color: $universityColor")
            calendarView.addDecorator(ActivityDecorator(universityColor, universityDates))
        }
        if (personalDates.isNotEmpty()) {
            val personalColor = ContextCompat.getColor(this, R.color.defaultActivityColor)
            Log.d(TAG, "Adding personal decorator with color: $personalColor")
            calendarView.addDecorator(ActivityDecorator(personalColor, personalDates))
        }
        if (bothDates.isNotEmpty()) {
            val bothColor = ContextCompat.getColor(this, R.color.both)
            Log.d(TAG, "Adding both decorator with color: $bothColor")
            calendarView.addDecorator(ActivityDecorator(bothColor, bothDates))
        }

        // Refresh the calendar view
        calendarView.invalidateDecorators()
    }

    private fun toDateTime(date: Date): DateTime {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return DateTime(calendar.timeInMillis)
    }
}

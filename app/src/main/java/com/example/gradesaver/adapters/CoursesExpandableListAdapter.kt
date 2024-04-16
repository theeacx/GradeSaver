package com.example.gradesaver.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.gradesaver.AddActivityActivity
import com.example.gradesaver.R
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale


@Suppress("NAME_SHADOWING")
class CoursesExpandableListAdapter(private val context: Context,
                                   private val courseList: MutableList<Course>,
                                   private val courseDetails: HashMap<String, List<com.example.gradesaver.database.entities.Activity>>, // Modified to List<Activity>
                                   private val scope: CoroutineScope,
                                   private val professorId: Int) : BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): Any {
        return this.courseList[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    @SuppressLint("SuspiciousIndentation")
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.group_item, parent, false)
        }

        val course = getGroup(groupPosition) as Course // Cast to Course instead of String
        val courseTextView = convertView?.findViewById<TextView>(R.id.course)
        courseTextView?.text = course.courseName // Use courseName property

        // Inside your getGroupView() in CoursesExpandableListAdapter
        val trashIcon = convertView?.findViewById<ImageView>(R.id.ivDelete)
        trashIcon?.setOnClickListener {
            val courseToDelete = courseList[groupPosition]
            AlertDialog.Builder(context).apply {
                setTitle("Delete Course")
                setMessage("Are you sure you want to permanently delete ${courseToDelete.courseName}?")
                setPositiveButton("Yes") { dialog, which ->
                    scope.launch {
                        AppDatabase.getInstance(context).appDao().deleteCourse(courseToDelete)
                        // Update the list and notify the adapter of the dataset change
                        courseList.removeAt(groupPosition)
                        notifyDataSetChanged()
                        // Any additional UI updates or logic after deletion can be performed here
                    }
                }
                setNegativeButton("No", null)
            }.create().show()
        }

        val infoIcon = convertView?.findViewById<ImageView>(R.id.ivInfo)
        infoIcon?.setOnClickListener {
            val course = courseList[groupPosition]
            // Fetch the professor's details from the database
            scope.launch {
                val professor = AppDatabase.getInstance(context).appDao().getUserById(course.professorId)
                val message = "Professor Email: ${professor.email}\nCourse Code: ${course.enrollmentCode}"
                // Check if context is an instance of Activity before casting
                (context as? android.app.Activity)?.runOnUiThread {
                    AlertDialog.Builder(context).apply {
                        setTitle(course.courseName)
                        setMessage(message)
                        setPositiveButton("OK", null)
                        setNeutralButton("Edit course details") { dialog, which ->
                            showEditCourseDialog(course)
                        }
                    }.show()
                }
            }
        }



        val addActivityIcon = convertView?.findViewById<ImageView>(R.id.ivAdd)
        addActivityIcon?.setOnClickListener {
            // Create an intent to start AddActivityActivity
            val intent = Intent(context, AddActivityActivity::class.java)
            // Pass the course ID as an extra
            intent.putExtra("COURSE_ID", courseList[groupPosition].courseId)
            context.startActivity(intent)
        }



        return convertView!!
    }

    private fun showEditCourseDialog(course: Course) {
        // Declare EditTexts outside the LinearLayout to ensure they're accessible in the dialog's button listener
        val courseNameEditText = EditText(context).apply { setText(course.courseName) }
        val courseCodeEditText = EditText(context).apply { setText(course.enrollmentCode) }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(courseNameEditText)
            addView(courseCodeEditText)
        }

        AlertDialog.Builder(context).apply {
            setTitle("Edit Course")
            setView(layout)
            setPositiveButton("OK") { dialog, which ->
                val newName = courseNameEditText.text.toString()
                val newCode = courseCodeEditText.text.toString()
                if (newName.isNotEmpty() && newCode.isNotEmpty()) {
                    course.courseName = newName
                    course.enrollmentCode = newCode
                    scope.launch {
                        AppDatabase.getInstance(context).appDao().updateCourse(course)
                        refreshData()
                    }
                }
            }
            setNegativeButton("Cancel", null)
            show()
        }
    }

    private fun refreshData() {
        scope.launch {
            val updatedCourses = AppDatabase.getInstance(context).appDao().getCoursesByProfessor(professorId)
            val updatedCourseDetails = HashMap<String, List<Activity>>()

            // Fetching activities for each updated course
            for (course in updatedCourses) {
                val activities = AppDatabase.getInstance(context).appDao().getActivitiesByCourse(course.courseId)
                updatedCourseDetails[course.courseName] = activities
            }

            // Update the list and details map
            courseList.clear()
            courseList.addAll(updatedCourses)
            courseDetails.clear()
            courseDetails.putAll(updatedCourseDetails)

            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }


    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val courseName: String = courseList[groupPosition].courseName
        return courseDetails[courseName]?.get(childPosition) ?: throw IllegalStateException("Activity not found")
    }


    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.child_item, parent, false)
        }

        val activity = getChild(groupPosition, childPosition) as Activity
        if (convertView != null) {
            convertView.findViewById<TextView>(R.id.activityName)?.text = activity.activityName
        }
        if (convertView != null) {
            convertView.findViewById<TextView>(R.id.activityDueDate)?.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(activity.dueDate)
        }

        return convertView!!
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return this.courseList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        // Assuming that courseList contains your group data (Courses) and
        // courseDetails is a HashMap mapping course names to lists of Activities.
        val course = courseList[groupPosition]
        // Return the number of activities for this course.
        return courseDetails[course.courseName]?.size ?: 0
    }
}
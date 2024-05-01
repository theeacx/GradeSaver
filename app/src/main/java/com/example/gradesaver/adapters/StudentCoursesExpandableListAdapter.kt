package com.example.gradesaver.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.example.gradesaver.AddRemindersActivity
import com.example.gradesaver.ManageRemindersActivity
import com.example.gradesaver.R
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentCoursesExpandableListAdapter(
    private val context: Context,
    private val user: User,
    private var allCourses: MutableList<Course>, // Holds all courses
    private var details: MutableMap<Course, List<Activity>>,
    private val onDeleteCourse: (Course) -> Unit,
    private val coroutineScope: CoroutineScope
) : BaseExpandableListAdapter(), Filterable {

    private var filteredCourses = allCourses // Initially, all courses are shown

    override fun getGroup(groupPosition: Int) = filteredCourses[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int) = details[filteredCourses[groupPosition]]?.get(childPosition) ?: throw IllegalStateException("Activity not found")

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()

    override fun getGroupCount() = filteredCourses.size

    override fun getChildrenCount(groupPosition: Int) = details[filteredCourses[groupPosition]]?.size ?: 0

    override fun hasStableIds() = true

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.student_group_item, parent, false)
        val course = getGroup(groupPosition)
        view.findViewById<TextView>(R.id.course).text = course.courseName
        view.findViewById<ImageView>(R.id.ivDelete).setOnClickListener {
            onDeleteCourse(course)  // Trigger the deletion logic with confirmation
        }
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.student_child_item, parent, false)
        val activity = getChild(groupPosition, childPosition)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = Date()
        view.findViewById<TextView>(R.id.activityName).text = activity.activityName
        view.findViewById<TextView>(R.id.activityDueDate).text = dateFormat.format(activity.dueDate)

        val imageView = view.findViewById<ImageView>(R.id.addReminders)

        val info = view.findViewById<ImageView>(R.id.ivInfo);
        info.setOnClickListener {
            val intent = Intent(context, ManageRemindersActivity::class.java).apply {
                putExtra("USER_DETAILS", user)  // Ensure user is serializable or pass user ID
                putExtra("ACTIVITY", activity)
            }
            context.startActivity(intent)
        }

        coroutineScope.launch(Dispatchers.IO) {
            val hasReminders = AppDatabase.getInstance(context).appDao().getRemindersByActivity(activity.activityId).isNotEmpty()
            withContext(Dispatchers.Main) {
                if (activity.dueDate.after(currentDate) && !hasReminders) {
                    imageView.visibility = View.VISIBLE
                    imageView.setOnClickListener {
                        val intent = Intent(context, AddRemindersActivity::class.java).apply {
                            putExtra("USER_DETAILS", user)  // Ensure user is serializable or pass user ID
                            putExtra("ACTIVITY", activity)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    imageView.visibility = View.INVISIBLE
                }
            }
        }
        return view
    }




    fun updateData(newCourses: MutableList<Course>, newDetails: MutableMap<Course, List<Activity>>) {
        allCourses.clear()
        allCourses.addAll(newCourses)
        details.clear()
        details.putAll(newDetails)
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: MutableList<Course> = mutableListOf()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(allCourses)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()
                    for (item in allCourses) {
                        if (item.courseName.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }
                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCourses = results?.values as? MutableList<Course> ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}

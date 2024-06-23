package com.example.gradesaver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.ActivityWithCourse
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityListAdapter(private val context: Context, private val dataSource: List<ActivityWithCourse>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.activity_list_item, parent, false)

        val activityNameTextView = rowView.findViewById<TextView>(R.id.activityName)
        val activityDeadlineTextView = rowView.findViewById<TextView>(R.id.activityDeadline)
        val courseNameTextView = rowView.findViewById<TextView>(R.id.courseName)

        val activityWithCourse = getItem(position) as ActivityWithCourse

        activityNameTextView.text = "Name: ${activityWithCourse.activity.activityName}"
        activityDeadlineTextView.text = "Deadline: ${dateFormat.format(activityWithCourse.activity.dueDate)}"
        courseNameTextView.text = "Course name: ${activityWithCourse.courseName}"

        return rowView
    }
}

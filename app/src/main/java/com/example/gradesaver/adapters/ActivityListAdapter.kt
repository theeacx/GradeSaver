package com.example.gradesaver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.gradesaver.R
import com.example.gradesaver.ActivityWithProfessorEmail
import java.text.SimpleDateFormat
import java.util.*

class ActivityListAdapter(
    private val context: Context,
    private val activities: List<ActivityWithProfessorEmail>
) : BaseAdapter() {

    override fun getCount(): Int {
        return activities.size
    }

    override fun getItem(position: Int): Any {
        return activities[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_list_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val activityWithProfessorEmail = activities[position]
        viewHolder.activityName.text = "Activity: ${activityWithProfessorEmail.activity.activityName}"
        viewHolder.courseName.text = "Course: ${activityWithProfessorEmail.course.courseName}"

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        viewHolder.activityDeadline.text = "Deadline: ${formatter.format(activityWithProfessorEmail.activity.dueDate)}"
        viewHolder.professorEmail.text = "Professor Email: ${activityWithProfessorEmail.professorEmail}"

        return view
    }

    private class ViewHolder(view: View) {
        val activityName: TextView = view.findViewById(R.id.activityName)
        val activityDeadline: TextView = view.findViewById(R.id.activityDeadline)
        val courseName: TextView = view.findViewById(R.id.courseName)
        val professorEmail: TextView = view.findViewById(R.id.professorEmail)
    }
}

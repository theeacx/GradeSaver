package com.example.gradesaver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.gradesaver.R
import com.example.gradesaver.database.entities.Reminder
import java.text.SimpleDateFormat
import java.util.Locale

class ReminderAdapter(context: Context, reminders: List<Reminder>) :
    ArrayAdapter<Reminder>(context, 0, reminders) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.reminder_layout, parent, false)
        }

        val reminder = getItem(position) ?: return view!!

        val dateView = view?.findViewById<TextView>(R.id.activityDate)
        if (dateView != null) {
            dateView.text = dateFormat.format(reminder.reminderDate)
        }

        val deleteIcon = view?.findViewById<ImageView>(R.id.deleteActivity)
        if (deleteIcon != null) {
            deleteIcon.setOnClickListener {
                // Handle deletion logic here
            }
        }

        return view!!
    }
}

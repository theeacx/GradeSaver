package com.example.gradesaver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.gradesaver.R
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ReminderAdapter(private val context: Context, private var reminders: MutableList<Reminder>) :
    ArrayAdapter<Reminder>(context, 0, reminders) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.reminder_layout, parent, false)

        val reminder = getItem(position)!!
        val dateView = view.findViewById<TextView>(R.id.activityDate)
        dateView.text = dateFormat.format(reminder.reminderDate)

        val deleteIcon = view.findViewById<ImageView>(R.id.deleteActivity)
        deleteIcon.setOnClickListener {
            showDeleteConfirmationDialog(reminder, position)
        }

        return view
    }

    private fun showDeleteConfirmationDialog(reminder: Reminder, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteReminder(reminder, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteReminder(reminder: Reminder, position: Int) {
        val appDatabase = AppDatabase.getInstance(context.applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            appDatabase.appDao().deleteReminder(reminder)
            reminders.removeAt(position)
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
                Toast.makeText(context, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

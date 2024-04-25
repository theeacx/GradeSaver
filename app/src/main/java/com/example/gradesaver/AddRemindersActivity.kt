package com.example.gradesaver

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gradesaver.database.entities.Activity

class AddRemindersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminders)

        // Retrieve the Activity object passed via intent
        val activity = intent.getSerializableExtra("ACTIVITY") as? Activity
        if (activity != null) {
            val activityName = activity.activityName

            // Set the TextView to include the name of the activity
            val titleText: TextView = findViewById(R.id.titleText)
            titleText.text = "Reminders for $activityName"

            // Set the button text dynamically based on the activity name
            val addReminderButton: Button = findViewById(R.id.addReminderButton)
            addReminderButton.text = "Add Reminders for $activityName"

            // Initialize other UI elements
            initSpinner()
            handleSpinnerSelection()
        }
    }

    private fun initSpinner() {
        val spinner: Spinner = findViewById(R.id.reminderTypeSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.reminder_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun handleSpinnerSelection() {
        val spinner: Spinner = findViewById(R.id.reminderTypeSpinner)
        val numberField: EditText = findViewById(R.id.numberField)
        val messageField: EditText = findViewById(R.id.messageField)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Show the custom fields only if "Custom" is selected
                val isVisible = parent.getItemAtPosition(position).toString() == "Custom"
                numberField.visibility = if (isVisible) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                numberField.visibility = View.GONE
            }
        }
    }
}

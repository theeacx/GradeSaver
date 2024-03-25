package com.example.gradesaver

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val roleSpinner: Spinner = findViewById(R.id.roleSpinner)

        ArrayAdapter.createFromResource(
            this,
            R.array.role_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            roleSpinner.adapter = adapter
        }

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position > 0) {
                    // User has selected an actual role, handle the selection
                    val selectedRole = parent.getItemAtPosition(position).toString()
                    // TODO: Use the selectedRole for something
                } else {
                    // Hint is selected, do nothing or handle as needed
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Interface callback, not used in this context
            }
        }

    }
}
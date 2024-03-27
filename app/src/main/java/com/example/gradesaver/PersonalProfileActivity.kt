package com.example.gradesaver

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gradesaver.database.entities.User

class PersonalProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_profile)

        val user = intent.getSerializableExtra("USER_DETAILS") as? User
        user?.let {
            // Change from EditText to TextView
            val emailTextView: TextView = findViewById(R.id.emailTextView)
            val typeTextView: TextView = findViewById(R.id.typeTextView)

            // Set text to display user details
            emailTextView.text = user.email
            typeTextView.text = user.role
        }
    }
}

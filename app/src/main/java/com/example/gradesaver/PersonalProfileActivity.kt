package com.example.gradesaver


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

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

        // Inside your PersonalProfileActivity
        val fabDeleteProfile: FloatingActionButton = findViewById(R.id.fab_delete_profile)
        fabDeleteProfile.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Delete Account")
                setMessage("Do you really want to permanently delete your account?")
                setPositiveButton("Yes") { dialog, which ->
                    user?.let { nonNullUser ->
                        lifecycleScope.launch {
                            AppDatabase.getInstance(this@PersonalProfileActivity).appDao().deleteUser(nonNullUser)
                            Toast.makeText(this@PersonalProfileActivity, "Account deleted", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@PersonalProfileActivity, SignUpActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish() // This call is to destroy the current activity

                        }
                    }
                }
                setNegativeButton("No", null)
            }.create().show()
        }

        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Perform any other logout operations you might need here, like clearing shared preferences or tokens

            // Now navigate back to the login screen or any other appropriate screen
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // This call is to destroy the current activity
        }

        val changePasswordButton: Button = findViewById(R.id.changePasswordButton)

    }
}

package com.example.gradesaver


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.User
import com.example.gradesaver.security.Hash.Companion.toSHA256
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
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // This call is to destroy the current activity
        }

        val changePasswordButton: Button = findViewById(R.id.changePasswordButton)
        changePasswordButton.setOnClickListener {
            val changePasswordDialogView = LayoutInflater.from(this).inflate(R.layout.change_password_dialog_box, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(changePasswordDialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Change", null)
                .create()

            dialog.setOnShowListener {
                val currentPasswordEditText = changePasswordDialogView.findViewById<EditText>(R.id.currentPassword)
                val newPasswordEditText = changePasswordDialogView.findViewById<EditText>(R.id.newPassword)
                val confirmNewPasswordEditText = changePasswordDialogView.findViewById<EditText>(R.id.confirmNewPassword)

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    // Hash the current password input by the user to compare with the stored hash
                    val currentPasswordInputHash = currentPasswordEditText.text.toString().toSHA256()
                    val newPassword = newPasswordEditText.text.toString()
                    val confirmNewPassword = confirmNewPasswordEditText.text.toString()

                    if (newPassword == confirmNewPassword) {
                        // Check if the hashed input of the current password matches the stored hashed password
                        if (currentPasswordInputHash == user?.password) {
                            lifecycleScope.launch {
                                // Hash the new password before storing it
                                val hashedNewPassword = newPassword.toSHA256()
                                user.password = hashedNewPassword // Assuming you have a way to update the user object

                                // Update user in the database
                                AppDatabase.getInstance(this@PersonalProfileActivity).appDao().updateUser(user)

                                Toast.makeText(this@PersonalProfileActivity, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        } else {
                            Toast.makeText(this@PersonalProfileActivity, "Current password is incorrect or new passwords do not match.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@PersonalProfileActivity, "New passwords do not match.", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            dialog.show()
            }
            }

        }

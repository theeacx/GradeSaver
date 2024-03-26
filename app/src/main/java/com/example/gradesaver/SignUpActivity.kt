package com.example.gradesaver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.User
import com.example.gradesaver.security.Hash.Companion.toSHA256
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPassword)
        val signUpButton: Button = findViewById(R.id.signUpButton)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            when {
                email.isEmpty() -> {
                    Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show()
                    emailEditText.requestFocus()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show()
                    passwordEditText.requestFocus()
                }
                confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Please confirm your password!", Toast.LENGTH_SHORT).show()
                    confirmPasswordEditText.requestFocus()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }
                !isValidEmail(email) -> {
                    Toast.makeText(this, "Please use an institutional email!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val role = determineUserRole(email)
                    if (role != "Invalid") {
                        val hashedPassword = password.toSHA256()
                        val newUser = User(0, email, hashedPassword, role) // Ideally, password should be hashed
                        lifecycleScope.launch {
                            AppDatabase.getInstance(this@SignUpActivity).appDao().insertUser(newUser)
                            Toast.makeText(this@SignUpActivity, "Sign up successful! Please login.", Toast.LENGTH_SHORT).show()
                            // Intent to go back to the login screen
                            val loginIntent = Intent(this@SignUpActivity, MainActivity::class.java)
                            // These flags clear the back stack so the user can't navigate back to the signup screen with the back button
                            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(loginIntent)
                            finish() // Call finish to destroy the signup activity
                        }
                    } else {
                        Toast.makeText(this@SignUpActivity, "Invalid role determined based on email.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.endsWith("@stud.ase.ro") || email.endsWith("@csie.ase.ro") || email.endsWith("@ie.ase.ro")
    }

    private fun determineUserRole(email: String): String {
        return when {
            email.endsWith("@stud.ase.ro") -> "Student"
            email.endsWith("@csie.ase.ro") || email.endsWith("@ie.ase.ro") -> "Professor"
            else -> "Invalid"
        }
    }
}

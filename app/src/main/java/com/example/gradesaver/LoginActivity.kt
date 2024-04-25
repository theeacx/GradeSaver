package com.example.gradesaver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.security.Hash.Companion.toSHA256
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    lateinit var username : EditText
    lateinit var password: EditText
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.loginButton)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        val dao = AppDatabase.getInstance(this).appDao()
        val signUpText: TextView = findViewById(R.id.signUpText)

        signUpText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val inputUsername = username.text.toString().trim()
            val inputPassword = password.text.toString().toSHA256()

            when {
                inputUsername.isEmpty() -> Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show()
                inputPassword.isEmpty() -> Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show()
                else -> loginUser(inputUsername, inputPassword, dao)
            }
        }
    }

    private fun loginUser(inputUsername: String, inputPassword: String, dao: AppDao) {
        lifecycleScope.launch {
            val user = dao.getUserByEmail(inputUsername)
            if (user != null && user.password == inputPassword) {
                val nextActivity = when (user.role) {
                    "Professor" -> ProfessorMainScreenActivity::class.java
                    "Student" -> StudentMainScreenActivity::class.java
                    else -> null
                }

                nextActivity?.let {
                    val intent = Intent(this@LoginActivity, it)
                    intent.putExtra("USER_DETAILS", user)
                    startActivity(intent)
                } ?: run {
                    Toast.makeText(this@LoginActivity, "Invalid user role.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@LoginActivity, "Login Failed! Please sign up!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

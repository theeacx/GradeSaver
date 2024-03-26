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

class MainActivity : AppCompatActivity() {
    lateinit var username : EditText
    lateinit var password: EditText
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            if (user != null && user.password == inputPassword) { // Ideally, you should hash the password and compare hashes.
                Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                // Navigate to the next screen or session home
            } else {
                Toast.makeText(this@MainActivity, "Login Failed! Please sign up!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

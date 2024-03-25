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
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var username : EditText
    lateinit var password: EditText
    lateinit var loginButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginButton=findViewById(R.id.loginButton)
        username=findViewById(R.id.username)
        password=findViewById(R.id.password)
        val dao = AppDatabase.getInstance(this).appDao()
        val signUpText: TextView = findViewById(R.id.signUpText)
        signUpText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val inputUsername = username.text.toString()
            val inputPassword = password.text.toString()

            lifecycleScope.launch {
                // Assuming you have a method to hash or securely store and compare passwords
                val user = dao.getUserByEmail(inputUsername)

                if (user != null && user.password == inputPassword) { // You should hash the password and compare hashes
                    // Login success
                    Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    // Login failed
                    Toast.makeText(this@MainActivity, "Login Failed! Please sign up!", Toast.LENGTH_SHORT).show()
                }
        }


    }
}}
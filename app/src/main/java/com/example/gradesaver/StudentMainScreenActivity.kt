package com.example.gradesaver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.gradesaver.database.entities.User
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StudentMainScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_main_screen)

        val user = intent.getSerializableExtra("USER_DETAILS") as? User

        val fabOne: FloatingActionButton = findViewById(R.id.fabOne)
        fabOne.setOnClickListener {
            val intent = Intent(this, PersonalProfileActivity::class.java).apply {
                putExtra("USER_DETAILS", user)
            }
            startActivity(intent)
        }

        val fabTwo: FloatingActionButton = findViewById(R.id.fabTwo)
        fabTwo.setOnClickListener {
            val intent = Intent(this, StudentsCourseActivity::class.java).apply {
                putExtra("USER_DETAILS", user)
            }
            startActivity(intent)
        }

        val fabThree: FloatingActionButton = findViewById(R.id.fabThree)
        fabThree.setOnClickListener {
            val intent = Intent(this, StudentCalendarActivity::class.java).apply {
                putExtra("USER_DETAILS", user)
            }
            startActivity(intent)
        }

        val fabFour: FloatingActionButton = findViewById(R.id.fabFour)
        fabFour.setOnClickListener {
            val intent = Intent(this, StudentDashbordActivity::class.java).apply {
                putExtra("USER_DETAILS", user)
            }
            startActivity(intent)
        }

        val buttonChat : Button = findViewById(R.id.buttonCSIEgpt)
        buttonChat.setOnClickListener {
            val intent = Intent(this, CsieGptActivity::class.java).apply {
                putExtra("USER_DETAILS", user)
            }
            startActivity(intent)
        }
    }
}
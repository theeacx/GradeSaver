package com.example.gradesaver.database.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*
// Users Table
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val email: String,
    val password: String,
    val role: String
)

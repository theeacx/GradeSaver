package com.example.gradesaver.database.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*
@Entity(
    tableName = "courses",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = arrayOf("userId"),
        childColumns = arrayOf("professorId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Course(
    @PrimaryKey(autoGenerate = true) val courseId: Int = 0,
    val courseName: String,
    val professorId: Int,
    val enrollmentCode: String
)

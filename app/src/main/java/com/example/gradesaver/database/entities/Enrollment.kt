package com.example.gradesaver.database.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*
@Entity(
    tableName = "enrollments",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = arrayOf("courseId"),
            childColumns = arrayOf("courseId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("userId"),
            childColumns = arrayOf("studentId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Enrollment(
    @PrimaryKey(autoGenerate = true) val enrollmentId: Int = 0,
    val courseId: Int,
    val studentId: Int
)

package com.example.gradesaver

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.adapters.ActivityListAdapter
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

class StudentActivityListActivity : AppCompatActivity() {
    private lateinit var dao: AppDao
    private lateinit var listView: ListView
    private lateinit var activitiesWithProfessorEmail: List<ActivityWithProfessorEmail>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        dao = AppDatabase.getInstance(this).appDao()
        listView = findViewById(R.id.activityListView)

        loadActivities()

        val exportPdfButton: Button = findViewById(R.id.exportPdfButton)
        exportPdfButton.setOnClickListener {
            exportToPdf()
        }
    }

    private fun loadActivities() {
        lifecycleScope.launch {
            val currentDate = Date()
            val activitiesWithCourses = dao.getUpcomingActivities(currentDate)
            activitiesWithProfessorEmail = activitiesWithCourses.map { activityWithCourse ->
                val professor = dao.getUserById(activityWithCourse.course.professorId)
                ActivityWithProfessorEmail(activityWithCourse.activity, activityWithCourse.course, professor?.email ?: "No email")
            }
            val adapter = ActivityListAdapter(this@StudentActivityListActivity, activitiesWithProfessorEmail)
            listView.adapter = adapter
        }
    }

    private fun exportToPdf() {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText("Activities To Do List", 72f, 72f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        var yPosition = 120f

        for (activityWithProfessorEmail in activitiesWithProfessorEmail) {
            canvas.drawText("Activity: ${activityWithProfessorEmail.activity.activityName}", 72f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Course: ${activityWithProfessorEmail.course.courseName}", 72f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Professor Email: ${activityWithProfessorEmail.professorEmail}", 72f, yPosition, paint)
            yPosition += 40f
        }

        document.finishPage(page)

        val directoryPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/"
        val filePath = directoryPath + "ActivitiesToDoList.pdf"
        val file = File(filePath)

        try {
            document.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF exported to $filePath", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error exporting PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        document.close()

        sharePdf(file)
    }

    private fun sharePdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share PDF using"))
    }
}

data class ActivityWithProfessorEmail(
    val activity: Activity,
    val course: Course,
    val professorEmail: String
)

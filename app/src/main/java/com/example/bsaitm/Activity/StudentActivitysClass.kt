package com.example.bsaitm.Activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.applandeo.materialcalendarview.EventDay
import com.example.bsaitm.DataClass.StudentData
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityStudentActivitysClassBinding
import com.google.firebase.auth.FirebaseAuth

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import com.google.firebase.firestore.FirebaseFirestore


class StudentActivitysClass : AppCompatActivity() {
    private val binding by lazy {
        ActivityStudentActivitysClassBinding.inflate(layoutInflater)
    }

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = FirebaseFirestore.getInstance()
        getUserInfo()

    }



    private fun getUserInfo() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return


        val ref = db.collection("students").document(userId)
        val percent=intent.getStringExtra("percent")?.toFloatOrNull()?.toInt() ?: 0

        setupPieChart(percent)


        try {
            ref.get().addOnSuccessListener { document ->
                if (document.exists()) {

                    val data = document.toObject(StudentData::class.java)
                    if (data != null) {

                        val subject=intent.getStringExtra("subject")
                        Log.d("DEBUG","$subject${data.name}")
                        markAttendanceOnCalendar(data.rollNo,data.branch,data.semester,data.batch,data.course,subject)


                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user info: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            Log.d("###", "error")
        }
    }

    private fun setupPieChart(percent: Int) {
        val pie: Pie = AnyChart.pie()

        val absentPercentage = 100 - percent

        val data: MutableList<DataEntry> = ArrayList()
        data.add(ValueDataEntry("Present", percent))
        data.add(ValueDataEntry("Absent", absentPercentage))

        pie.data(data)

        pie.title("Attendance Percentage")
        pie.labels().position("outside")

        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)

        binding.pieChartView.setChart(pie)
    }



    private fun markAttendanceOnCalendar(
        rollNo: String?,
        branch: String?,
        semester: String?,
        batch: String?,
        course: String?,
        subject: String?
    ) {
        if (rollNo.isNullOrEmpty()) {
            Log.e("DEBUG", "Roll number is null or empty!")
            return
        }

        // ✅ Null or empty check
        if (course.isNullOrEmpty() || branch.isNullOrEmpty() || semester.isNullOrEmpty() ||
            batch.isNullOrEmpty() || subject.isNullOrEmpty()) {
            Log.e("DEBUG", "One or more required fields are empty!")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val ref = db.collection(course).document(branch).collection(semester)
            .document(batch).collection("studentsAttendance")
            .document(rollNo).collection("subjects").document(subject).collection("attendance")

        try {
            ref.get().addOnSuccessListener { snapshot ->
                val presentDays = mutableListOf<EventDay>()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                try {
                    for (document in snapshot) {
                        val dateString = document.id // Firestore document ID is the date
                        val status = document.getString("status")

                        if (status == "Present") {
                            val calendar = Calendar.getInstance()
                            calendar.time = sdf.parse(dateString)!!

                            // ✅ Green mark for present days
                            presentDays.add(EventDay(calendar, R.drawable.checked))
                        }
                    }

                    // ✅ Set the marked dates on CalendarView
                    binding.calendarView.setEvents(presentDays)

                } catch (e: Exception) {
                    Log.e("ParseError", "Error parsing date: ${e.message}")
                }
            }.addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching attendance: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("GeneralError", "Unexpected error: ${e.message}")
        }
    }
}
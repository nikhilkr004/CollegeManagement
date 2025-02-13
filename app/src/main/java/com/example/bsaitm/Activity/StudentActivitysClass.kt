package com.example.bsaitm.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.bumptech.glide.Glide
import com.example.bsaitm.DataClass.StudentData
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityStudentActivitysClassBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException


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





    private fun fetchAttendanceForChart(
        lineChart: LineChart,
        rollNo: String?,
        branch: String?,
        semester: String?,
        batch: String?,
        course: String?
    ) {

        val ref = db.collection(course!!).document(branch!!).collection(
            semester!!
        ).document(batch!!)
            .collection("studentsAttendance").document(rollNo!!).collection("attendance")


        ref.get().addOnSuccessListener { snapshot ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val attendanceData = mutableMapOf<Int, MutableList<Pair<Int, Float>>>()

            for (document in snapshot.documents) {
                val dateString = document.id // Firestore document ID is the date
                val status = document.getString("status")

                val calendar = Calendar.getInstance()
                calendar.time = sdf.parse(dateString)!!
                val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                // Y-axis value: 1 for Present, -1 for Absent
                val attendanceValue = if (status == "Present") 1f else -1f

                // Store data per week
                if (!attendanceData.containsKey(weekOfYear)) {
                    attendanceData[weekOfYear] = mutableListOf()
                }
                attendanceData[weekOfYear]?.add(Pair(dayOfWeek, attendanceValue))
            }

            // Process data for Line Chart
            val entries = ArrayList<Entry>()
            var xIndex = 0

            for ((week, days) in attendanceData) {
                days.sortBy { it.first } // Sort by day of the week
                for ((day, value) in days) {
                    entries.add(Entry(xIndex.toFloat(), value))
                    xIndex++
                }
                // Add a gap between weeks
                xIndex += 2
            }

            // Update Line Chart
            setupLineChart(lineChart, entries)

        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error fetching attendance: ${e.message}")
        }

    }

    private fun setupLineChart(lineChart: LineChart, entries: ArrayList<Entry>) {
        val dataSet = LineDataSet(entries, "Attendance Activity")
        dataSet.color = Color.BLUE
        dataSet.setCircleColor(Color.RED)
        dataSet.circleRadius = 5f
        dataSet.valueTextSize = 10f
        dataSet.lineWidth = 3f

        val lineData = LineData(dataSet)

        lineChart.data = lineData
        lineChart.invalidate() // Refresh chart

        // Customize X-Axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textSize = 12f

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
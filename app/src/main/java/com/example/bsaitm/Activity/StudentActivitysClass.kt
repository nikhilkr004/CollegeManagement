package com.example.bsaitm.Activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityStudentActivitysClassBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import com.google.firebase.firestore.FirebaseFirestore


class StudentActivitysClass : AppCompatActivity() {
    private val binding by lazy {
        ActivityStudentActivitysClassBinding.inflate(layoutInflater)
    }

    companion object {
        var rollNO: String = ""
        var batch: String = ""
        var branch: String = ""
        var semester: String = ""
        var course: String = ""
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
        db=FirebaseFirestore.getInstance()

        val branch=intent.getStringExtra("branch")
        val rollNo=intent.getStringExtra("rollNo")
        val semester=intent.getStringExtra("sem")
        val course=intent.getStringExtra("course")
        val batch=intent.getStringExtra("batch")

        markAttendanceOnCalendar(binding.calendarView, rollNo,branch,semester,batch,course)

        fetchAttendanceForChart(binding.lineChart,rollNo,branch,semester,batch,course)
    }

    private fun fetchAttendanceForChart(lineChart: LineChart, rollNo: String?, branch: String?, semester: String?, batch: String?, course: String?) {

        val ref =db.collection(course!!).document(branch!!).collection(semester!!
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

    private fun setupLineChart(lineChart: LineChart, entries: ArrayList<Entry>){
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
        calendarView: CalendarView,
        rollNo: String?,
        branch: String?,
        semester: String?,
        batch: String?,
        course: String?
    ) {
        if (rollNo.isNullOrEmpty()) {
            Log.e("FirestoreError", "Roll number is null or empty!")
            return
        }



        // ✅ Check for null values before using them in Firestore
        if (course!!.isEmpty() || branch!!.isEmpty() || semester!!.isEmpty() || batch!!.isEmpty()) {
            Log.e("FirestoreError", "One or more required fields are empty!")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val ref = db.collection(course).document(branch).collection(semester
        ).document(batch)
            .collection("studentsAttendance").document(rollNo).collection("attendance")


        ref.get().addOnSuccessListener { snapshot ->
            val presentDays = mutableListOf<EventDay>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (document in snapshot.documents) {
                val dateString = document.id // Firestore document ID is the date
                val status = document.getString("status")

                if (status == "Present") {
                    val calendar = Calendar.getInstance()
                    calendar.time = sdf.parse(dateString)!!

                    // ✅ Green mark for present days
                    presentDays.add(EventDay(calendar,R.drawable.checked))
                }
            }

            // ✅ Set the marked dates on CalendarView
            calendarView.setEvents(presentDays)

        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error fetching attendance: ${e.message}")
        }
    }


}
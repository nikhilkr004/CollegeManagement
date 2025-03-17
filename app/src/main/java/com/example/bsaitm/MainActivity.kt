package com.example.bsaitm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.bsaitm.Activity.LeaveActivity
import com.example.bsaitm.Activity.NoticeActivity
import com.example.bsaitm.Activity.ProfileActivity
import com.example.bsaitm.Activity.StudentActivitysClass
import com.example.bsaitm.Activity.ViewSyllabusActivity
import com.example.bsaitm.Adapter.ImageAdapter
import com.example.bsaitm.Adapter.NoticeAdapter
import com.example.bsaitm.Adapter.SubjectsAttendanceAdapter
import com.example.bsaitm.Constant.diplomacivilsem1b1
import com.example.bsaitm.Constant.diplomacivilsem2b1
import com.example.bsaitm.Constant.diplomacivilsem3b1
import com.example.bsaitm.Constant.diplomacivilsem4b1
import com.example.bsaitm.Constant.diplomacivilsem5b1
import com.example.bsaitm.Constant.diplomacivilsem6b1
import com.example.bsaitm.Constant.diplomacomputersem1b1
import com.example.bsaitm.Constant.diplomacomputersem2b1
import com.example.bsaitm.Constant.diplomacomputersem3b1
import com.example.bsaitm.Constant.diplomacomputersem4b1
import com.example.bsaitm.Constant.diplomacomputersem5b1
import com.example.bsaitm.Constant.diplomacomputersem6b1
import com.example.bsaitm.Constant.diplomaelectricalsem1b1
import com.example.bsaitm.Constant.diplomaelectricalsem2b1
import com.example.bsaitm.Constant.diplomaelectricalsem3b1
import com.example.bsaitm.Constant.diplomaelectricalsem4b1
import com.example.bsaitm.Constant.diplomaelectricalsem5b1
import com.example.bsaitm.Constant.diplomaelectricalsem6b1
import com.example.bsaitm.Constant.diplomaelectronicsem1b1
import com.example.bsaitm.Constant.diplomaelectronicsem2b1
import com.example.bsaitm.Constant.diplomaelectronicsem3b1
import com.example.bsaitm.Constant.diplomaelectronicsem4b1
import com.example.bsaitm.Constant.diplomaelectronicsem5b1
import com.example.bsaitm.Constant.diplomaelectronicsem6b1
import com.example.bsaitm.Constant.diplomamechanicalsem1b1
import com.example.bsaitm.Constant.diplomamechanicalsem2b1
import com.example.bsaitm.Constant.diplomamechanicalsem3b1
import com.example.bsaitm.Constant.diplomamechanicalsem4b1
import com.example.bsaitm.Constant.diplomamechanicalsem5b1
import com.example.bsaitm.Constant.diplomamechanicalsem6b1

import com.example.bsaitm.DataClass.NoticeData
import com.example.bsaitm.DataClass.StudentData
import com.example.bsaitm.DataClass.SubjectAttendance
import com.example.bsaitm.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var db: FirebaseFirestore
    private lateinit var databaseReference: DatabaseReference
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var studentData:ArrayList<StudentData>
    private lateinit var subjectAttendanceList: ArrayList<SubjectAttendance>
    private lateinit var adapter: ImageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        studentData= ArrayList()
        subjectAttendanceList = ArrayList()
        db = FirebaseFirestore.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        //image slider
        initalization()
        setTransformer()

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })





        getUserInfo()

        binding.profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.viewNotice.setOnClickListener {
            startActivity(Intent(this,NoticeActivity::class.java))
        }

        binding.leaveRequest.setOnClickListener {
            startActivity(Intent(this,LeaveActivity::class.java))
        }

        binding.viewSyllabus.setOnClickListener {
            startActivity(Intent(this,ViewSyllabusActivity::class.java))
        }
    }


    private fun getUserInfo() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val ref = db.collection("students").document(userId)


        try {
            ref.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    studentData.clear()
                    val data = document.toObject(StudentData::class.java)
                    if (data != null) {
                        studentData.add(data)
                        binding.name.text = data.name

                        Glide.with(this).load(data.profileImage).placeholder(R.drawable.user_)
                            .into(binding.profile)

                        fetchAttendance(data)


                        val intent = Intent(this, StudentActivitysClass::class.java)
                        intent.putExtra("branch", data.branch)
                        intent.putExtra("sem", data.semester)
                        intent.putExtra("course", data.course)
                        intent.putExtra("batch", data.batch)
                        intent.putExtra("rollNo", data.rollNo)
//                        startActivity(intent)
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





    private fun fetchAttendance(data: StudentData) {
        val subjectRecycleview = binding.attendanceRecycler
        subjectRecycleview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val db = FirebaseFirestore.getInstance()
        val studentRef = db.collection(data.course!!)
            .document(data.branch!!)
            .collection(data.semester!!)
            .document(data.batch!!)
            .collection("studentsAttendance")
            .document(data.rollNo!!)
            .collection("subjects") // ✅ Fetch all subjects dynamically

        Log.d("DEBUG", "Fetching subjects for Student: ${data.rollNo}")

        // ✅ Fix 1: Subject List Selection
        val selectedKey = data.course + data.branch + data.semester + data.batch
        val subjectList = when (selectedKey) {
            "diplomacomputersem1b1" -> diplomacomputersem1b1
            "diplomacomputersem2b1" -> diplomacomputersem2b1
            "diplomacomputersem3b1" -> diplomacomputersem3b1
            "diplomacomputersem4b1" -> diplomacomputersem4b1
            "diplomacomputersem5b1" -> diplomacomputersem5b1
            "diplomacomputersem6b1" -> diplomacomputersem6b1

            ////civil
            "diplomacivilsem1b1" -> diplomacivilsem1b1
            "diplomacivilsem2b1" -> diplomacivilsem2b1
            "diplomacivilsem3b1" -> diplomacivilsem3b1
            "diplomacivilsem4b1" -> diplomacivilsem4b1
            "diplomacivilsem5b1" -> diplomacivilsem5b1
            "diplomacivilsem6b1" -> diplomacivilsem6b1


            /// electrical
            "diplomaelectricalsem1b1" -> diplomaelectricalsem1b1
            "diplomaelectricalsem2b1" -> diplomaelectricalsem2b1
            "diplomaelectricalsem3b1" -> diplomaelectricalsem3b1
            "diplomaelectricalsem4b1" -> diplomaelectricalsem4b1
            "diplomaelectricalsem5b1" -> diplomaelectricalsem5b1
            "diplomaelectricalsem6b1" -> diplomaelectricalsem6b1

            //eletronics
            "diplomaelectronicsem1b1" -> diplomaelectronicsem1b1
            "diplomaelectronicsem2b1" -> diplomaelectronicsem2b1
            "diplomaelectronicsem3b1" -> diplomaelectronicsem3b1
            "diplomaelectronicsem4b1" -> diplomaelectronicsem4b1
            "diplomaelectronicsem5b1" -> diplomaelectronicsem5b1
            "diplomaelectronicsem6b1" -> diplomaelectronicsem6b1

            "diplomamechanicalsem1b1" -> diplomamechanicalsem1b1
            "diplomamechanicalsem2b1" -> diplomamechanicalsem2b1
            "diplomamechanicalsem3b1" -> diplomamechanicalsem3b1
            "diplomamechanicalsem4b1" -> diplomamechanicalsem4b1
            "diplomamechanicalsem5b1" -> diplomamechanicalsem5b1
            "diplomamechanicalsem6b1" -> diplomamechanicalsem6b1


            else -> emptyArray()
        }

        // ✅ Fix 2: Clear List Before Loop
        subjectAttendanceList.clear()

        val totalSubjects = subjectList.size
        var completedSubjects = 0

        // ✅ Fix 3: Initialize Adapter Before Loop
        val adapters = SubjectsAttendanceAdapter(subjectAttendanceList,studentRef)
        subjectRecycleview.adapter = adapters

        subjectList.forEach { subject ->
            val subjectName = subject.trim().replace(" ", "").lowercase()
            Log.d("DEBUG", "Fetching Attendance for Subject: $subjectName")

            val attendanceRef = studentRef.document(subjectName).collection("attendance")

            attendanceRef.get().addOnSuccessListener { attendanceSnapshot ->
                var presentDays = 0
                var totalDays = 0

                for (attendanceDoc in attendanceSnapshot.documents) {
                    val status = attendanceDoc.getString("status")
                    if (status != null) {
                        totalDays++
                        if (status == "Present") {
                            presentDays++
                        }
                    }
                }

                val attendancePercentage = if (totalDays > 0) {
                    (presentDays.toDouble() / totalDays.toFloat()) * 100
                } else {
                    0.0
                }

                val formattedPercentage = String.format("%.1f", attendancePercentage)

                // ✅ Fix 4: Update List Without Clearing
                subjectAttendanceList.add(SubjectAttendance(subjectName, formattedPercentage))

                Log.d("DEBUG", "Subject: $subjectName, Attendance: $formattedPercentage %")

                // ✅ Fix 5: Update RecyclerView After All Subjects Are Fetched
                completedSubjects++
                if (completedSubjects == totalSubjects) {
                    binding.mainLayout.visibility=View.VISIBLE
                    binding.mainShimmerLayout.visibility=View.GONE

                    adapters.notifyDataSetChanged()
                }

            }.addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching attendance for $subjectName: ${e.message}")
            }
        }
    }






    private fun initalization() {
        viewPager2 = binding.viewPager2
        handler = Handler(Looper.myLooper()!!)
        imageList = ArrayList()

        imageList.add(R.drawable.tyyuj)
        imageList.add(R.drawable.assasa)
        imageList.add(R.drawable.dsfgdsfg)





        adapter = ImageAdapter(imageList, viewPager2)

        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    private fun setTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }
//
//        val r = 1 - Math.abs(position)
//        page.scaleY = 0.85f + r * 0.14f
        viewPager2.setPageTransformer(transformer)
    }

    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()

        handler.postDelayed(runnable, 2000)
    }


    private val runnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    }




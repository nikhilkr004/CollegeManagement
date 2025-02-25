package com.example.bsaitm.Activity

import LeaveRequestAdapter
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import attachSwipeToDelete
import com.example.bsaitm.Adapter.LeaveRequest
import com.example.bsaitm.Constant
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityLeaveBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class LeaveActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLeaveBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var leaveRequests = mutableListOf<LeaveRequest>() // ✅ Fixed List Initialization
    private lateinit var adapter: LeaveRequestAdapter
    private var selectedTeacherId = ""
    private var selectedTeacherName = ""
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        recyclerView = binding.requestRecyclerview
        val studentName = intent.getStringExtra("name")

        binding.composeLeave.setOnClickListener {
            showLeaveDropdown(studentName)
        }

        setupRecyclerView()
        fetchLeaveData()
    }

    private fun fetchLeaveData() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        db.collection("leave_requests")
            .whereEqualTo("studentId", userId)
            .addSnapshotListener { snapshots, _ ->

                leaveRequests.clear()

                if (snapshots != null) {
                    val leaveList = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(LeaveRequest::class.java)?.copy(leaveId = doc.id)
                    }

                    val sortedLeaveList = leaveList.sortedByDescending { it.time }
//                    if (leaveList.isEmpty()){
//                        binding.requestRecyclerview.visibility=View.GONE
//                        binding.i
//                    }
                    leaveRequests.addAll(sortedLeaveList) // ✅ Fixed List Addition
                   adapter.notifyDataSetChanged()// ✅ Ensure UI Updates
                }
            }
    }

    private fun showLeaveDropdown(studentName: String?) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.write_leave_item, null)
        val teacher: AutoCompleteTextView = view.findViewById(R.id.tvTeacher)
        val startDate: EditText = view.findViewById(R.id.etStartDate)
        val endDate: EditText = view.findViewById(R.id.etEndDate)
        val lvTitle: EditText = view.findViewById(R.id.etheading)
        val lvReason: EditText = view.findViewById(R.id.etReason)
        val apply: TextView = view.findViewById(R.id.apply)
        val cancel: TextView = view.findViewById(R.id.cencel)

        startDate.setOnClickListener { selectDate(startDate) }
        endDate.setOnClickListener { selectDate(endDate) }

        db.collection("Teacher").get()
            .addOnSuccessListener { documents ->
                val teacherList = ArrayList<String>()
                val teacherMap = HashMap<String, String>()

                for (doc in documents) {
                    val teacherName = doc.getString("name") ?: "Unknown"
                    val teacherId = doc.id
                    teacherList.add(teacherName)
                    teacherMap[teacherName] = teacherId
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teacherList)
                teacher.setAdapter(adapter)
                teacher.setOnItemClickListener { _, _, position, _ ->
                    selectedTeacherName = teacherList[position]
                    selectedTeacherId = teacherMap[selectedTeacherName] ?: ""
                }
            }

        apply.setOnClickListener {
            submitLeaveRequest(lvTitle, lvReason, dialog, startDate, endDate, studentName)
            Constant.showDialog(this, "sending request")
        }
        cancel.setOnClickListener { dialog.dismiss() }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun submitLeaveRequest(
        lvTitle: EditText,
        lvReason: EditText,
        dialog: BottomSheetDialog,
        startDate: EditText,
        endDate: EditText,
        studentName: String?
    ) {

        try {


        val fromDate = startDate.text.toString()
        val toDate = endDate.text.toString()
        val reason = lvReason.text.toString()
        val title = lvTitle.text.toString()
        val studentId = auth.currentUser?.uid ?: return

        if (selectedTeacherId.isEmpty() && fromDate.isEmpty() && toDate.isEmpty() && reason.isEmpty() && title.isEmpty()) {
            Constant.hideDialog()
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            val uniqueId = generateFirestoreId()
            val leaveRequest = LeaveRequest(
                studentId = studentId,
                studentName = studentName.toString(),
                teacherId = selectedTeacherId,
                teacherName = selectedTeacherName,
                fromDate = fromDate,
                toDate = toDate,
                title = title,
                time = Timestamp.now(),
                reason = reason,
                leaveId = uniqueId,
                status = "Pending"
            )

            db.collection("leave_requests").document(uniqueId).set(leaveRequest)
                .addOnSuccessListener {
                    Toast.makeText(this, "Leave Request Submitted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    fetchLeaveData() // ✅ Refresh data
                    Constant.hideDialog()
                }
                .addOnFailureListener {
                    Constant.hideDialog()
                    Toast.makeText(this, "Failed to Submit", Toast.LENGTH_SHORT).show()
                }
        }
        }catch (e:Exception ){
            Log.d("DEBUG","${e.localizedMessage}")
        }
    }

    private fun setupRecyclerView() {
        adapter = LeaveRequestAdapter(leaveRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        attachSwipeToDelete(recyclerView,adapter)
    }

    private fun selectDate(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                editText.setText("$day/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }



    fun generateFirestoreId(): String = FirebaseFirestore.getInstance().collection("leave_requests").document().id
}

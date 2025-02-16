package com.example.bsaitm.Activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.Adapter.LeaveItemActionListener
import com.example.bsaitm.Adapter.LeaveRequest
import com.example.bsaitm.Adapter.LeaveRequestAdapter
import com.example.bsaitm.Constant
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityLeaveBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

class LeaveActivity : AppCompatActivity(),LeaveItemActionListener{

    private val binding by lazy {
        ActivityLeaveBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var leaveRequests = mutableListOf<LeaveRequest>()
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
                if (snapshots != null) {
                    val leaveList = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(LeaveRequest::class.java)?.copy(leaveId = doc.id)
                    }

                    // 🔥 Sort List By Timestamp (Latest First)
                    val sortedLeaveList = leaveList.sortedByDescending { it.time }

                    adapter.updateList(sortedLeaveList) // ✅ RecyclerView Update
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

        // Fetch teacher data
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
        val fromDate = startDate.text.toString()
        val toDate = endDate.text.toString()
        val reason = lvReason.text.toString()
        val title = lvTitle.text.toString()
        val studentId = auth.currentUser?.uid ?: return

        if (selectedTeacherId.isEmpty() || fromDate.isEmpty() || toDate.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

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
                Constant.hideDialog()
            }
            .addOnFailureListener {
                Constant.hideDialog()
                Toast.makeText(this, "Failed to Submit", Toast.LENGTH_SHORT).show()
            }
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

    fun generateFirestoreId(): String {
        return FirebaseFirestore.getInstance().collection("dummy").document().id
    }

    private fun setupRecyclerView() {
        try {
            adapter = LeaveRequestAdapter(leaveRequests.toMutableList(), this)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter

//            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
//                override fun onMove(
//                    recyclerView: RecyclerView,
//                    viewHolder: RecyclerView.ViewHolder,
//                    target: RecyclerView.ViewHolder
//                ): Boolean = false
//
//                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    val position = viewHolder.adapterPosition
//                    if (position != RecyclerView.NO_POSITION) {
//                        // You can choose to call onDelete here or let the long-press dialog handle deletion.
//                        onDelete(leaveRequests[position], position)
//                    } else {
//                        adapter.notifyDataSetChanged()
//                    }
//                }
//            })
//            itemTouchHelper.attachToRecyclerView(recyclerView)
        } catch (e: Exception) {
            Log.d("DEBUG", e.localizedMessage)
        }
    }

    override fun onEdit(leave: LeaveRequest, position: Int) {
        // Open a BottomSheetDialog (or any edit dialog) to allow editing.
        Toast.makeText(this, "Edit: ${leave.title}", Toast.LENGTH_SHORT).show()
        // Implement your edit logic here (for example, prefill a dialog with leave data).
    }

    override fun onDelete(leave: LeaveRequest, position: Int) {
        // Call adapter.removeItem() to delete the item.
        adapter.removeItem(position)
        Toast.makeText(this, "Deleted: ${leave.title}", Toast.LENGTH_SHORT).show()
    }
}

package com.example.bsaitm.Adapter

import com.google.protobuf.Timestamp

data class LeaveRequest(
    val studentId: String = "",
    val studentName: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val fromDate: String = "",
    val toDate: String = "",
    val reason: String = "",
    val title:String="",
    val time:com.google.firebase.Timestamp?=null,
    val leaveId:String="",
    val status: String = "Pending"

)


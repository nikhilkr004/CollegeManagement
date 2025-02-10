package com.example.bsaitm

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.example.bsaitm.DataClass.StudentData
import com.example.bsaitm.databinding.ProgressDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


object Constant {

val auth=FirebaseAuth.getInstance()
    private var dialog: AlertDialog?=null
    fun showDialog(context: Context, message:String){
        val process= ProgressDialogBinding.inflate(LayoutInflater.from(context))
        process.text.text=message.toString()

        dialog=AlertDialog.Builder(context).setView(process.root).setCancelable(true).create()
        dialog!!.show()
    }

    fun hideDialog(){
        dialog!!.dismiss()
    }
    fun currentUserId(): String {
        return auth.currentUser!!.uid
    }

    fun fetchUserData(userId: String,callback:(StudentData?) -> Unit) {
         val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("students")

        fun getUserInfo(userId: String, callback: (StudentData?) -> Unit) {
            val userRef = databaseReference.child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(StudentData::class.java)
                    callback(user)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    callback(null)
                }
            })
        }
    }


val category= arrayOf(
"java",
    "c-language",
    "cplus",
    "html",
    "python"
)

    val course= arrayOf(
        "diploma",
        "btech",
    )

    val semester= arrayOf(
        "sem1",
        "sem2",
        "sem3",
        "sem4",
        "sem5",
        "sem6",
        "sem7",
        "sem8"
    )

    val batch= arrayOf(
        "b1",
        "b2",
        "b3"
    )

    val branch= arrayOf(
        "computer",
        "electrical",
        "electronic",
        "civil",
        "mechanical"
    )
}
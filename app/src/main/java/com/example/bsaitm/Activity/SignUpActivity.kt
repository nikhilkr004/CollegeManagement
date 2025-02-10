package com.example.bsaitm.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bsaitm.Constant
import com.example.bsaitm.DataClass.StudentData
import com.example.bsaitm.MainActivity
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivitySignUpBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.util.Util

class SignUpActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
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
        firestore=FirebaseFirestore.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        setUpAutoTextField()

        binding.signInTxt.setOnClickListener {
        startActivity(Intent(this,SignInActivity::class.java))
        }
        binding.signUp.setOnClickListener {
            signUp()
            Constant.showDialog(this, "wait signing up....")
        }
    }

    private fun signUp() {
        val name = binding.name.text.toString()
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val number = binding.number.text.toString()
        val course = binding.courses.text.toString()
        val semester = binding.semester.text.toString()
        val branch = binding.branch.text.toString()
        val batch = binding.batch.text.toString()
        val rollNo = binding.ronnno.text.toString()

        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            val studentData = StudentData(
                name = name,
                email = email,
                password = password,
                number = number,
                course = course,
                semester = semester,
                branch = branch,
                batch = batch,
                profileImage = "",
                userId = auth.currentUser!!.uid,
                rollNo = rollNo
            )

            if (name.equals("") or email.equals("") or password.equals("") or number.equals("") or branch.equals(
                    ""
                ) or batch.equals("") or
                rollNo.equals("")
            ) {
                Toast.makeText(this, "Fill all the field", Toast.LENGTH_SHORT).show()
            } else {
                firestore.collection("students").document(auth.currentUser!!.uid).set(studentData)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            Constant.hideDialog()
                            finish()
                        } else {
                            Constant.hideDialog()
                            Toast.makeText(
                                this,
                                it.exception!!.localizedMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "something went wrong ", Toast.LENGTH_SHORT).show()
                        Constant.hideDialog()
                    }
            }


        }
    }

    private fun setUpAutoTextField() {
        val course = ArrayAdapter(this, R.layout.show_list, Constant.course)
        val semester = ArrayAdapter(this, R.layout.show_list, Constant.semester)
        val branch = ArrayAdapter(this, R.layout.show_list, Constant.branch)
        val batch = ArrayAdapter(this, R.layout.show_list, Constant.batch)

        binding.courses.setAdapter(course)
        binding.semester.setAdapter(semester)
        binding.branch.setAdapter(branch)
        binding.batch.setAdapter(batch)
    }
}
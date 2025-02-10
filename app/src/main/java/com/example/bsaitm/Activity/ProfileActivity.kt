package com.example.bsaitm.Activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.bsaitm.Constant
import com.example.bsaitm.DataClass.StudentData
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID

class ProfileActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private var profileImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
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
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        fatchProfileInfo()

        binding.logoutLayout.setOnClickListener {
            logoutUser(this)
        }

        binding.updateProfileLayout.setOnClickListener {
            updateUserProfile(profileImageUri)
        }

    }

    private fun updateUserProfile(profileImageUri: Uri?) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.profile_edit_dialog, null)
        val saveBtn = view.findViewById<TextView>(R.id.editprofile)
        val cancel = view.findViewById<TextView>(R.id.cencel)
        val profileImage = view.findViewById<CircleImageView>(R.id.profile_image)
        val name: EditText = view.findViewById(R.id.name)
        val number: EditText = view.findViewById(R.id.number)
        val branch: EditText = view.findViewById(R.id.branch)
        val rollNo: EditText = view.findViewById(R.id.ronnno)
        val courses: EditText = view.findViewById(R.id.courses)
        val semester: EditText = view.findViewById(R.id.semester)
        val batch: EditText = view.findViewById(R.id.batch)

        val uid = auth.currentUser!!.uid
        val ref = db.collection("students").document(uid)


        try {
            ref.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.toObject(StudentData::class.java)
                    if (data != null) {
                        name.setText(data.name.toString())
                        number.setText(data.number.toString())
                        rollNo.setText(data.rollNo.toString())
                        courses.setText(data.course.toString())
                        semester.setText(data.semester.toString())
                        batch.setText(data.batch.toString())
                        branch.setText(data.branch.toString())

                        if (profileImageUri == null) Glide.with(this)
                            .load(data.profileImage)
                            .placeholder(R.drawable.user_).into(profileImage)
                        else Glide.with(this).load(profileImageUri).into(profileImage)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "try trough an error", Toast.LENGTH_SHORT).show()
        }





        cancel.setOnClickListener {
            dialog.dismiss()
        }

        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)

        }

        saveBtn.setOnClickListener {
            Constant.showDialog(this,"Wait saving your info....")

            val name = name.text.toString()
            val mobileNo = number.text.toString()
            val courses = courses.text.toString()
            val semester = semester.text.toString()
            val batch = batch.text.toString()
            val branch = branch.text.toString()
            val rollNo = rollNo.text.toString()

            if (name.isNotEmpty() || mobileNo.isNotEmpty() || courses.isNotEmpty() || semester.isNotEmpty() || batch.isNotEmpty() ||
                branch.isNotEmpty() || rollNo.isNotEmpty()
            ) {
                uploadImageToFirebase(
                    name,
                    mobileNo,
                    courses,
                    semester,
                    batch,
                    branch,
                    rollNo,
                    dialog
                )
            }
            else{
                Toast.makeText(this, "fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }




        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun uploadImageToFirebase(
        name: String,
        mobileNo: String,
        courses: String,
        semester: String,
        batch: String,
        branch: String,
        rollNo: String,
        dialog: BottomSheetDialog
    ) {


        if (profileImageUri != null) {
            val imageFileName = UUID.randomUUID().toString() + ".jpg"
            val storage = FirebaseStorage.getInstance().reference
            val imagesRef = storage.child("profile_image/$imageFileName")

            try {
                imagesRef.putFile(profileImageUri!!).addOnSuccessListener {
                    try {
                        imagesRef.downloadUrl.addOnSuccessListener { uri ->
                            saveDataToFirebase(
                                name,
                                mobileNo,
                                courses,
                                semester,
                                batch,
                                branch,
                                rollNo,
                                dialog,
                                uri
                            )
                        }
                    } catch (e: Exception) {
                        Log.d("@@@", "error")
                    }

                }
            } catch (e: Exception) {
                Log.d("@@@@@", "error")
            }

        }
    }

    private fun saveDataToFirebase(
        name: String,
        mobileNo: String,
        courses: String,
        semester: String,
        batch: String,
        branch: String,
        rollNo: String,
        dialog: BottomSheetDialog,
        uri: Uri?
    ) {

        val student = StudentData(
            name = name,
            number = mobileNo,
            course = courses,
            semester = semester,
            batch = batch,
            branch = branch,
            rollNo = rollNo,
            profileImage = uri.toString()
        )

        val studentMap = mutableMapOf<String, Any>().apply {
            student.name?.let { put("name", it) }
            student.number?.let { put("number", it) }
            student.course?.let { put("course", it) }
            student.semester?.let { put("semester", it) }
            student.batch?.let { put("batch", it) }
            student.branch?.let { put("branch", it) }
            student.rollNo?.let { put("rollNo", it) }
            student.profileImage?.let { put("profileImage", it) }
        }


        val uid = auth.currentUser!!.uid
        val ref = db.collection("students").document(uid)

        try {
            ref.update(studentMap).addOnSuccessListener {
                Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show()
                Constant.hideDialog()
                dialog.dismiss()
            }
                .addOnFailureListener {
                    Toast.makeText(this, "error to save data ", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.d("@@@", "error")
        }

    }

    private fun fatchProfileInfo() {
        val uid = auth.currentUser!!.uid
        val ref = db.collection("students").document(uid)


        try {
            ref.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.toObject(StudentData::class.java)
                    if (data != null) {

                        Glide.with(this).load(data.profileImage).placeholder(R.drawable.user_).into(binding.profileImage)

                        binding.name.text = data.name.toString()
                        binding.email.text = data.email.toString()
                        binding.userInfo.text =
                            "Course ${data.course} | Branch ${data.branch} | Semester ${data.semester} | Roll no ${data.rollNo}"
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error to Fetch data", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.d("@@@@@", "Error to fetch data")
        }


    }


    fun logoutUser(context: Context) {
        FirebaseAuth.getInstance().signOut()

        // Redirect to LoginActivity (Replace with your actual login activity)
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }



    /// upload image on storage
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.data
            updateUserProfile(profileImageUri)
        }
    }
}
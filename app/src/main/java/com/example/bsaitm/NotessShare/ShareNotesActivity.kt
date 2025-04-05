package com.example.bsaitm.NotessShare

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityShareNotesBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ShareNotesActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityShareNotesBinding.inflate(layoutInflater)
    }

    private lateinit var titleEditText: EditText
    private lateinit var subjectEditText: EditText
    private lateinit var semesterEditText: EditText
    private lateinit var selectFileBtn: TextView
    private lateinit var uploadBtn: TextView
    private lateinit var selectedFileName: TextView

    private var selectedFileUri: Uri? = null

    private val PICK_PDF_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        titleEditText = findViewById(R.id.titleEditText)
        subjectEditText = findViewById(R.id.subjectEditText)
        semesterEditText = findViewById(R.id.semesterEditText)
        selectFileBtn = findViewById(R.id.selectFileBtn)
        uploadBtn = findViewById(R.id.uploadBtn)
        selectedFileName = findViewById(R.id.selectedFileName)

        selectFileBtn.setOnClickListener {
            pickPdfFile()
        }

        binding.imageButton2.setOnClickListener {
            finish()
        }


        uploadBtn.setOnClickListener {
            if (selectedFileUri != null) {
                uploadNote()
            } else {
                Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.data
            selectedFileName.text = selectedFileUri?.lastPathSegment
        }
    }

    private fun uploadNote() {
        val title = titleEditText.text.toString().trim()
        val subject = subjectEditText.text.toString().trim()
        val semester = semesterEditText.text.toString().trim()

        if (title.isEmpty() || subject.isEmpty() || semester.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "notes/${UUID.randomUUID()}.pdf"
        val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

        val uploadTask = storageRef.putFile(selectedFileUri!!)
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading Note...")
        progressDialog.show()

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                saveNoteMetadata(title, subject, semester, downloadUrl.toString())
                progressDialog.dismiss()
                Toast.makeText(this, "Note uploaded successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            progressDialog.setMessage("Uploaded ${progress.toInt()}%")
        }

    }

    private fun saveNoteMetadata(
        title: String,
        subject: String,
        semester: String,
        fileUrl: String
    ) {
        val noteData = hashMapOf(
            "title" to title,
            "subject" to subject,
            "semester" to semester,
            "fileUrl" to fileUrl,
            "uploadedBy" to FirebaseAuth.getInstance().uid,
            "uploadDate" to Timestamp.now(),
            "likes" to 0
        )

        FirebaseFirestore.getInstance().collection("notes")
            .add(noteData)
    }
}

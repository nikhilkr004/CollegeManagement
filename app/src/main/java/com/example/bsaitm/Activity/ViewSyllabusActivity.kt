package com.example.bsaitm.Activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bsaitm.Adapter.SyllabusAdapter
import com.example.bsaitm.DataClass.PdfModel
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityViewSyllabusBinding
import com.google.firebase.firestore.FirebaseFirestore

class ViewSyllabusActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityViewSyllabusBinding.inflate(layoutInflater)
    }
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: SyllabusAdapter
    private var pdfData = mutableListOf<PdfModel>()


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

        /// setup recyclerview
        val recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SyllabusAdapter(pdfData,this)
        recyclerView.adapter = adapter

        /// get pdf
        getPdfData()
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                filterList(p0.toString())
            }
        })

    }

    private fun filterList(query: String) {
        val filteredList = pdfData.filter {
            it.text.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredList)

    }

    private fun getPdfData() {
        db.collection("uploads")
            .orderBy("timestamp") // Sort by latest uploads
            .get()
            .addOnSuccessListener { result ->
                pdfData.clear()
                for (document in result) {
                    val pdf = document.toObject(PdfModel::class.java)
                    pdfData.add(pdf)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore Error", e.message.toString())
                Toast.makeText(this, "Failed to load PDFs", Toast.LENGTH_SHORT).show()
            }
    }
}
package com.example.bsaitm.NotessShare

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityViewPdfsBinding
import com.google.firebase.firestore.FirebaseFirestore

class ViewPdfsActivity : AppCompatActivity() {
private val binding by lazy {
    ActivityViewPdfsBinding.inflate(layoutInflater)
}
    private val notesList = mutableListOf<PdfDataClass>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private val filteredList = mutableListOf<PdfDataClass>()
    private lateinit var adapter: NotesADAPTER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = binding.recyclerview
        searchView = findViewById(R.id.editTextText)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotesADAPTER(filteredList) {


        }
        recyclerView.adapter = adapter

        fetchNotes()
        setupSearch()
    }

    private fun fetchNotes() {
        FirebaseFirestore.getInstance().collection("notes")
            .get()
            .addOnSuccessListener { documents ->
                notesList.clear()
                for (doc in documents) {
                    val note = doc.toObject(PdfDataClass::class.java)
                    notesList.add(note)
                }
                filteredList.clear()
                filteredList.addAll(notesList)
                adapter.notifyDataSetChanged()
            }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText?.lowercase() ?: ""
                filteredList.clear()
                filteredList.addAll(
                    notesList.filter {
                        it.title?.lowercase()!!.contains(searchText) ||
                                it.title.lowercase().contains(searchText)
                    }
                )
                adapter.notifyDataSetChanged()
                return true
            }
        })
    }
}
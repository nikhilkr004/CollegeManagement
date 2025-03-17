package com.example.bsaitm.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bsaitm.Adapter.NoticeAdapter
import com.example.bsaitm.DataClass.NoticeData
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ActivityNoticeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NoticeActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityNoticeBinding.inflate(layoutInflater)
    }
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var noticeAdapter: NoticeAdapter
    private lateinit var noticeData: ArrayList<NoticeData>
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
        auth = FirebaseAuth.getInstance()


        noticeData = ArrayList()

        /// back button

        binding.backBtn.setOnClickListener { finish() }

        val recyclerView = binding.noticeRecycler
        recyclerView.layoutManager =
            LinearLayoutManager(this)
        noticeAdapter = NoticeAdapter(noticeData)
        recyclerView.adapter = noticeAdapter

        fetchNoticeInfo()
    }
    private fun fetchNoticeInfo() {
        val ref = db.collection("notice")
            .orderBy("date", Query.Direction.DESCENDING) // 🔥 Recent data first

        try {
            ref.get().addOnSuccessListener { documents ->
                noticeData.clear()
                for (document in documents) {
                    val data = document.toObject(NoticeData::class.java)
                    data?.let { noticeData.add(it)

                        checkEmptyStste()
                    }
                }

                noticeAdapter.notifyDataSetChanged() // ✅ RecyclerView Update
            }.addOnFailureListener {
                Toast.makeText(this, "Error fetching notices", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Exception: ${e.message}")
        }
    }

    private fun checkEmptyStste() {
        if (noticeData.isEmpty()){
            binding.noticeRecycler.visibility=View.GONE
            binding.lottieAnimationView.visibility=View.VISIBLE
        }else{
            binding.lottieAnimationView.visibility=View.GONE
            binding.noticeRecycler.visibility=View.VISIBLE
        }

    }

}

package com.example.bsaitm.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bsaitm.DataClass.NoticeData
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ShowNoticeUiDesignBinding
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NoticeAdapter(val data:List<NoticeData>):RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {


    class ViewHolder(val binding:ShowNoticeUiDesignBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NoticeData) {
            val db=FirebaseFirestore.getInstance()
            val context=binding.root.context


            try {

                val ref = db.collection("Teacher").document(data.userUid)
                ref.get().addOnSuccessListener {
                    document->

                    if (document.exists()) {

                        val userdata = document.toObject(Teacher::class.java)

                        try {
                            binding.useName.text = userdata!!.name.toString()
                            Glide.with(context).load(userdata.image).placeholder(R.drawable.user_)
                                .into(binding.profileImage)
                        }catch (e:Exception){

                        }



                    }
                }.addOnFailureListener { Toast.makeText(context, "error to fetch data ", Toast.LENGTH_SHORT).show() }
            }catch (e:Exception){
                Log.d("@@","error")
            }


            binding.time.text= TimeAgo.using(data.date!!.toLong())
            binding.title.text=data.title.toString()
            binding.dise.text=data.disc.toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val inflater=LayoutInflater.from(parent.context)
        val binding=ShowNoticeUiDesignBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val data=data[position]
        holder.bind(data)
    }
}
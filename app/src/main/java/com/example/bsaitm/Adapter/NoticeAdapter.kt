package com.example.bsaitm.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bsaitm.DataClass.NoticeData
import com.example.bsaitm.NotessShare.PdfDataClass
import com.example.bsaitm.R
import com.example.bsaitm.databinding.ViewDeleteNoticeItemBinding

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class NoticeAdapter(val data:List<NoticeData>):RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {


    class ViewHolder(val binding:ViewDeleteNoticeItemBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PdfDataClass) {
            val db=FirebaseFirestore.getInstance()
            val context=binding.root.context


            try {

                val ref = db.collection("Teacher").document(data.userUid)
                ref.get().addOnSuccessListener {
                        document->

                    if (document.exists()) {

                        val userdata = document.toObject(Teacher::class.java)

                        try {
                            binding.userName.text = userdata!!.name.toString()
                            Glide.with(context).load(userdata.image).placeholder(R.drawable.user_)
                                .into(binding.icon)
                        }catch (e:Exception){

                        }



                    }
                }.addOnFailureListener { Toast.makeText(context, "error to fetch data ", Toast.LENGTH_SHORT).show() }
            }catch (e:Exception){
                Log.d("@@","error")
               }


            binding.date.text=convertTimestampToDate(data.date)
            binding.title.text=data.title.toString()
            binding.noticeDetails.text=data.disc

        }
        private fun convertTimestampToDate(time: Timestamp?): CharSequence? {
            return time?.toDate()?.let {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) // 🔥 Format Define
                sdf.format(it)
            } ?: "N/A" // Default Value if Timestamp is Null
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val inflater=LayoutInflater.from(parent.context)
        val binding=ViewDeleteNoticeItemBinding.inflate(inflater)
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
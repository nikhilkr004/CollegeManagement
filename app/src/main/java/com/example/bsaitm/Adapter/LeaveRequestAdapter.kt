package com.example.bsaitm.Adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.databinding.LeaveStatusItemBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

import java.text.SimpleDateFormat
import java.util.Locale

class LeaveRequestAdapter(var data:MutableList<LeaveRequest>):RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder>() {



    class ViewHolder(val binding:LeaveStatusItemBinding):RecyclerView.ViewHolder(binding.root) {




        fun bind(data: LeaveRequest) {
            binding.title.text=data.title
            binding.status.text=data.status
            binding.time.text=convertTimestampToDate(data.time)

            binding.textView11.text="${data.fromDate} - ${data.toDate} "
            binding.teacherName.text=data.teacherName.toString()

            // 🔥 Status Color Change
            binding.status.setTextColor(
                when (data.status) {
                    "Pending" -> Color.YELLOW
                    "Accepted" -> Color.GREEN
                    else -> Color.RED
                }
            )
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
        val binding=LeaveStatusItemBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data=data[position]
        holder.bind(data)
    }


    fun updateList(newList: List<LeaveRequest>) {
        data=newList.toMutableList()
        notifyDataSetChanged()  // 🔥 RecyclerView ko refresh karega
    }


    fun removeItem(position: Int) {
        Log.d("LeaveRequestAdapter", "removeItem called for position: $position")

        if (position < 0 || position >= data.size) {
            Log.e("LeaveRequestAdapter", "Invalid position: $position")
            return
        }

        val removedItem = data[position]
        Log.d("LeaveRequestAdapter", "Removing item: ${removedItem.title}, leaveId: ${removedItem.leaveId}")

        if (removedItem.leaveId.isNullOrEmpty()) {
            Log.e("LeaveRequestAdapter", "leaveId is null or empty for position: $position")
            notifyItemChanged(position)
            return
        }

        FirebaseFirestore.getInstance().collection("leave_requests")
            .document(removedItem.leaveId)
            .delete()
            .addOnSuccessListener {
                Log.d("LeaveRequestAdapter", "Firestore deletion successful")

                    data.removeAt(position)
                    notifyItemRemoved(position)

            }
            .addOnFailureListener { e ->
                Log.e("LeaveRequestAdapter", "Firestore deletion failed: ${e.message}")
                notifyItemChanged(position)
            }
    }


}
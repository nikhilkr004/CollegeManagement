package com.example.bsaitm.Adapter

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.databinding.LeaveStatusItemBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

// Callback interface for handling edit and delete actions.
interface LeaveItemActionListener {
    fun onEdit(leave: LeaveRequest, position: Int)
    fun onDelete(leave: LeaveRequest, position: Int)
}

class LeaveRequestAdapter(
    private var data: MutableList<LeaveRequest>,
    private val actionListener: LeaveItemActionListener
) : RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder>() {

    // Updates the adapter's list and refreshes the RecyclerView.
    fun updateList(newList: List<LeaveRequest>) {
        data = newList.toMutableList()
        notifyDataSetChanged()
    }

    // ViewHolder class using View Binding.
    class ViewHolder(private val binding: LeaveStatusItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(leave: LeaveRequest, actionListener: LeaveItemActionListener) {
            binding.title.text = leave.title
            binding.status.text = leave.status
            binding.time.text = convertTimestampToDate(leave.time)
            binding.textView11.text = "${leave.fromDate} - ${leave.toDate}"
            binding.teacherName.text = leave.teacherName

            // Set the status text color based on its value.
            binding.status.setTextColor(
                when (leave.status) {
                    "Pending" -> Color.YELLOW
                    "Accepted" -> Color.GREEN
                    else -> Color.RED
                }
            )

            // Long-press on the item to show options.
            binding.root.setOnLongClickListener {
                val options = arrayOf("Edit", "Delete")
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Select Option")
                    .setItems(options) { dialog, which ->
                        when (which) {
                            0 -> actionListener.onEdit(leave, adapterPosition)
                            1 -> actionListener.onDelete(leave, adapterPosition)
                        }
                    }
                    .show()
                true
            }
        }

        // Helper method to convert Timestamp to a formatted String.
        private fun convertTimestampToDate(time: Timestamp?): String {
            return time?.toDate()?.let {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                sdf.format(it)
            } ?: "N/A"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LeaveStatusItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], actionListener)
    }

    // Removes an item from Firestore and updates the RecyclerView.
    fun removeItem(position: Int) {
        if (position < 0 || position >= data.size) return

        val removedItem = data[position]
        FirebaseFirestore.getInstance().collection("leave_requests")
            .document(removedItem.leaveId)
            .delete()
            .addOnSuccessListener {
                data.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                notifyItemChanged(position)
            }
    }
}

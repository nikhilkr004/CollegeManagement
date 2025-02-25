import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.Adapter.LeaveRequest
import com.example.bsaitm.databinding.LeaveStatusItemBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class LeaveRequestAdapter(
    private var data: MutableList<LeaveRequest>,
) : RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder>() {

    // ViewHolder Class
    class ViewHolder(private val binding: LeaveStatusItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(leave: LeaveRequest) {
            binding.title.text = leave.title
            binding.status.text = leave.status
            binding.time.text = convertTimestampToDate(leave.time)
            binding.textView11.text = "${leave.fromDate} - ${leave.toDate}"
            binding.teacherName.text = leave.teacherName

            binding.status.setTextColor(
                when (leave.status) {
                    "Pending" -> Color.YELLOW
                    "Accepted" -> Color.GREEN
                    else -> Color.RED
                }
            )
        }

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
        holder.bind(data[position])
    }

    // **Helper method to remove item**
    fun removeItem(position: Int) {
        if (position in data.indices) {
            val leaveId = data[position].leaveId

            FirebaseFirestore.getInstance().collection("leave_requests")
                .document(leaveId)
                .delete()
                .addOnSuccessListener {
                    if (position in data.indices) { // Check again before removing
                        data.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
                .addOnFailureListener {
                    Log.e("LeaveRequestAdapter", "Failed to delete item from Firestore")
                }
        }
    }
}

    // **Function to attach swipe-to-delete**
    fun attachSwipeToDelete(recyclerView: RecyclerView, adapter: LeaveRequestAdapter) {
        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition

                    // Show confirmation dialog
                    AlertDialog.Builder(recyclerView.context)
                        .setTitle("Delete Request")
                        .setMessage("Are you sure you want to delete this leave request?")
                        .setPositiveButton("Delete") { _, _ ->
                            adapter.removeItem(position)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            adapter.notifyItemChanged(position) // Restore item if canceled
                            dialog.dismiss()
                        }
                        .show()
                }

                override fun onChildDraw(
                    c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                    dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
                ) {
                    val itemView = viewHolder.itemView
                    val background = ColorDrawable(Color.RED)
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    background.draw(c)

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


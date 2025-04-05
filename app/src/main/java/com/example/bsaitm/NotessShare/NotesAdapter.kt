import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.Adapter.NoticeAdapter
import com.example.bsaitm.NotessShare.PdfDataClass
import com.example.bsaitm.R
import com.example.bsaitm.databinding.NotesItemBinding

class NotesAdapter(
    private val notes: List<PdfDataClass>
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteViewHolder {
        val inflater= LayoutInflater.from(parent.context)
        val binding= NotesItemBinding.inflate(inflater)
        return NoticeAdapter.ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    inner class NoteViewHolder(val binding: NotesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


    }


}

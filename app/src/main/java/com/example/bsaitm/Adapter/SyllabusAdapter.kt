package com.example.bsaitm.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.DataClass.PdfModel
import com.example.bsaitm.databinding.PdfSyllabusBinding

class SyllabusAdapter(
    var data: List<PdfModel>,
    private val context: Context
) : RecyclerView.Adapter<SyllabusAdapter.ViewHolder>() {

    fun updateList(newList: List<PdfModel>) {
        data = newList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: PdfSyllabusBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PdfModel, onClick: (PdfModel) -> Unit) {
            binding.pdfname.text = data.text
            binding.root.setOnClickListener {
                onClick(data) // Handle click event
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PdfSyllabusBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pdfData = data[position]
        holder.bind(pdfData) { selectedPdf ->
            openPdf(context, selectedPdf.pdfUrl)
        }
    }

    private fun openPdf(context: Context, pdfUrl: String) {
//        val intent = Intent(context, PdfViewerActivity::class.java)
//        intent.putExtra("PDF_URL", pdfUrl)
//        context.startActivity(intent)
    }
}

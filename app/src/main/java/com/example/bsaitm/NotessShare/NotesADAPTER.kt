package com.example.bsaitm.NotessShare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.databinding.NotesItemBinding

class NotesADAPTER(val data: List<PdfDataClass>, function: () -> Unit): RecyclerView.Adapter<NotesADAPTER.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
       val inflater= LayoutInflater.from(parent.context)
        val binding= NotesItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val data=data[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
       return data.size
    }

    class ViewHolder (val binding: NotesItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: PdfDataClass) {
            binding.pdfname.text=data.title.toString()
        }

    }
}



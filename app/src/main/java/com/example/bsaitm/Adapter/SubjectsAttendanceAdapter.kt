package com.example.bsaitm.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bsaitm.Activity.StudentActivitysClass
import com.example.bsaitm.DataClass.SubjectAttendance
import com.example.bsaitm.MainActivity
import com.example.bsaitm.databinding.AttendancePersentageUiBinding
import com.google.firebase.firestore.CollectionReference

class SubjectsAttendanceAdapter(val data: List<SubjectAttendance>, studentRef: CollectionReference):RecyclerView.Adapter<SubjectsAttendanceAdapter.ViewHolder>() {
    class ViewHolder (val binding:AttendancePersentageUiBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data: SubjectAttendance) {

            val context=binding.root.context
            binding.textView5.text = data.subjectName

            // ✅ Fix: Safe conversion for progress value
            val progress = data.attendancePercentage.toFloatOrNull()?.toInt() ?: 0
            binding.scoreProgressIndicator.progress = progress

            binding.scoreProgressText.text = "${progress} %"

            binding.root.setOnClickListener{

                val intent=Intent(context,StudentActivitysClass::class.java)
                intent.putExtra("subject",data.subjectName)
                context.startActivity(intent)
            }

        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val inflater=LayoutInflater.from(parent.context)
        val binding=AttendancePersentageUiBinding.inflate(inflater,parent,false)
        return  ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val data=data[position]
        holder.bind(data)
    }
}
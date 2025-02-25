package com.example.bsaitm.Adapter

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.example.bsaitm.Activity.StudentActivitysClass
import com.example.bsaitm.DataClass.SubjectAttendance
import com.example.bsaitm.MainActivity
import com.example.bsaitm.R
import com.example.bsaitm.databinding.AttendancePersentageUiBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.CollectionReference

class SubjectsAttendanceAdapter(val data: List<SubjectAttendance>, studentRef: CollectionReference):RecyclerView.Adapter<SubjectsAttendanceAdapter.ViewHolder>() {
    class ViewHolder (val binding:AttendancePersentageUiBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data: SubjectAttendance) {

            val context=binding.root.context
            binding.textView5.text = data.subjectName


            // Safe conversion: Convert String to Float, then to Int
         binding.scoreProgressIndicator.progress= data.attendancePercentage.toFloatOrNull()?.toInt() ?: 0



            binding.scoreProgressText.text = "${data.attendancePercentage} %"

            binding.root.setOnClickListener{

                val intent=Intent(context,StudentActivitysClass::class.java)
                intent.putExtra("subject",data.subjectName)
                intent.putExtra("percent",data.attendancePercentage)
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
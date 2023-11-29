package com.ngxqt.classmanagementmvvm.ui.adapter

import android.graphics.Color
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.data.model.StudentItem
import com.ngxqt.classmanagementmvvm.databinding.StudentItemAttendanceBinding
import com.ngxqt.classmanagementmvvm.databinding.StudentItemBinding

class StudentAttendanceAdapter(val studentItems: ArrayList<StudentItem>) :
    RecyclerView.Adapter<StudentAttendanceAdapter.StudentViewHolder>() {

    var onItemClick: ((Int) -> Unit)? = null
    var onContactClick: ((Int) -> Unit)? = null

    lateinit var binding: StudentItemAttendanceBinding
    inner class StudentViewHolder(private val binding: StudentItemAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        var roll: TextView = binding.roll
        var studentName: TextView = binding.name
        var cardView: CardView = binding.cardview

        init {
            binding.root.setOnClickListener {
                onItemClick?.invoke(layoutPosition)
            }
            binding.root.setOnCreateContextMenuListener(this)
            binding.buttonMess.setOnClickListener {
                onContactClick?.invoke(layoutPosition)
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.add(adapterPosition,0,0,"Edit")
            menu?.add(adapterPosition,1,0,"Delete")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        binding = StudentItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.roll.setText((studentItems.get(position).roll).toString())
        holder.studentName.setText(studentItems.get(position).name)
        holder.cardView.setCardBackgroundColor(getColor(position))
    }

    private fun getColor(position: Int): Int {
        return Color.parseColor("#"+Integer.toHexString(
            ContextCompat.getColor(binding.root.context,
            R.color.normal
        )))
    }

    override fun getItemCount(): Int {
        return studentItems.size
    }
}
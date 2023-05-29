package com.ngxqt.classmanagementmvvm.ui.adapter

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ngxqt.classmanagementmvvm.data.model.ClassItem
import com.ngxqt.classmanagementmvvm.databinding.ClassItemBinding

/** Code bị comment là phương án 2*/
class ClassAdapter(val classItems: ArrayList<ClassItem>) :
    RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {
    //PagingDataAdapter<ClassItem,ClassAdapter.ClassViewHolder>(CLASS_COMPARATOR) {

    /*private val onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(classItem: ClassItem)
    }*/

    var onItemClick: ((Int) -> Unit)? = null
    var onMapClick: ((Int) -> Unit)? = null

    inner class ClassViewHolder(private val binding: ClassItemBinding) :
        RecyclerView.ViewHolder(binding.root), OnCreateContextMenuListener {

        var className: TextView = binding.classTv
        var subjectName: TextView = binding.subjectTv

        init {
            binding.root.setOnClickListener {
                onItemClick?.invoke(layoutPosition)
            }
            binding.root.setOnCreateContextMenuListener(this)
            binding.buttonMap.setOnClickListener {
                onMapClick?.invoke(layoutPosition)
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(bindingAdapterPosition, 0, 0, "Edit")
            menu?.add(bindingAdapterPosition, 1, 0, "Delete")
            //menu?.add(bindingAdapterPosition, 2, 0, "Map")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val binding =
            ClassItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.className.setText(classItems.get(position).className)
        holder.subjectName.setText(classItems.get(position).subjectName)
    }

    override fun getItemCount(): Int {
        return classItems.size
    }
}
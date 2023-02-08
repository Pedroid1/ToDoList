package com.example.todolistkotlin.presentation.adapter.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.todolistkotlin.databinding.CalendarCellBinding
import com.example.todolistkotlin.presentation.model.CalendarModel
import com.example.todolistkotlin.presentation.viewholder.CalendarViewHolder

class CalendarAdapter(private val onDayClickListener: OnDayClickListener) :
    ListAdapter<CalendarModel, CalendarViewHolder>(DIFFUTILS) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val _binding =
            CalendarCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val layoutParams = _binding.root.layoutParams
        layoutParams.height = (parent.measuredWidth * 0.13).toInt()
        return CalendarViewHolder(_binding, onDayClickListener)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bindDay(getItem(position))
    }

    companion object {
        val DIFFUTILS = object : DiffUtil.ItemCallback<CalendarModel>() {
            override fun areItemsTheSame(oldItem: CalendarModel, newItem: CalendarModel): Boolean {
                return oldItem.timeInMillis == newItem.timeInMillis &&
                        oldItem.haveEvents == newItem.haveEvents &&
                        oldItem.isSelected == newItem.isSelected
            }

            override fun areContentsTheSame(
                oldItem: CalendarModel,
                newItem: CalendarModel
            ): Boolean {
                return oldItem.timeInMillis == newItem.timeInMillis &&
                        oldItem.haveEvents == newItem.haveEvents &&
                        oldItem.isSelected == newItem.isSelected
            }
        }
    }

    interface OnDayClickListener {
        fun onDayClickListener(timeInMillis: Long)
    }
}
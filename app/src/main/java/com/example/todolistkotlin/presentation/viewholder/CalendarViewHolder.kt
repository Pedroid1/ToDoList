package com.example.todolistkotlin.presentation.viewholder

import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.CalendarCellBinding
import com.example.todolistkotlin.presentation.model.CalendarModel
import com.example.todolistkotlin.presentation.adapter.recycler.CalendarAdapter
import com.example.todolistkotlin.util.DateUtils
import com.example.todolistkotlin.util.toInvisible
import com.example.todolistkotlin.util.toVisible
import com.google.android.material.color.MaterialColors

class CalendarViewHolder(
    private val _binding: CalendarCellBinding,
    private val onDayClickListener: CalendarAdapter.OnDayClickListener
) : RecyclerView.ViewHolder(_binding.root), OnClickListener {
    private var currentTimeInMillis: Long? = null

    fun bindDay(day: CalendarModel) {
        _binding.root.setOnClickListener(this)
        currentTimeInMillis = day.timeInMillis

        if (day.timeInMillis != null) {
            val context = _binding.root.context
            _binding.txtDay.text = DateUtils.longIntoDay(day.timeInMillis!!)
            _binding.txtDay.visibility = View.VISIBLE
            if (day.haveEvents) _binding.eventDay.toVisible() else _binding.eventDay.toInvisible()

            if (day.isSelected) {
                _binding.apply {
                    txtDay.background =
                        ContextCompat.getDrawable(context, R.drawable.selected_date_radius)
                    txtDay.setTextColor(
                        MaterialColors.getColor(
                            context,
                            android.R.attr.colorBackground,
                            ContextCompat.getColor(context, R.color.white)
                        )
                    )
                }
            }
        }
    }

    override fun onClick(v: View?) {
        if (currentTimeInMillis != null) {
            onDayClickListener.onDayClickListener(currentTimeInMillis!!)
        }
    }
}
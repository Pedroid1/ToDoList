package com.example.todolistkotlin.presentation.viewholder

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.EmptyListRecyclerItemBinding
import com.example.todolistkotlin.presentation.model.HomeRecyclerViewItem
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.example.todolistkotlin.databinding.LargeTaskItemBinding
import com.example.todolistkotlin.databinding.TaskDateItemBinding
import com.example.todolistkotlin.presentation.adapter.recycler.HomeRecyclerAdapter
import com.example.todolistkotlin.util.DateUtils

sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class TaskDateItemViewHolder(
        private val _binding: TaskDateItemBinding
    ) : HomeRecyclerViewHolder(_binding) {
        fun bindDate(dateItem: HomeRecyclerViewItem.TaskDateItem) {
            _binding.txtDate.text = dateItem.uiText.asString(_binding.root.context)
        }
    }

    class TaskItem(
        private val _binding: LargeTaskItemBinding,
        private val clickListener: HomeRecyclerAdapter.TaskAdapterListener
    ) : HomeRecyclerViewHolder(_binding) {
        fun bindTask(taskItem: HomeRecyclerViewItem.TaskItem) {
            val context = _binding.root.context
            _binding.apply {
                val currentTask = taskItem.item.task
                item = taskItem.item
                listener = clickListener

                ViewCompat.setScreenReaderFocusable(taskCard, true)
                txtHour.text = DateUtils.longIntoTime(currentTask.dateInMills)
                when(currentTask.priority) {
                    EnumTaskPriority.LOW -> {
                        flag.setColorFilter(ContextCompat.getColor(context, R.color.low_priority_color))
                        flag.contentDescription = context.getString(R.string.low_priority)
                    }
                    EnumTaskPriority.MEDIUM -> {
                        flag.setColorFilter(ContextCompat.getColor(context, R.color.medium_priority_color))
                        flag.contentDescription = context.getString(R.string.medium_priority)
                    }
                    else -> {
                        flag.setColorFilter(ContextCompat.getColor(context, R.color.high_priority_color))
                        flag.contentDescription = context.getString(R.string.high_priority)
                    }
                }
                txtCategory.setBackgroundColor(Color.parseColor(taskItem.item.category.categoryColor))
            }
        }
    }

    class Empty(private val _binding: EmptyListRecyclerItemBinding) : HomeRecyclerViewHolder(_binding) {
        fun bindMessage(emptyMessage: HomeRecyclerViewItem.Empty) {
            val context = _binding.root.context
            _binding.txt1.text = emptyMessage.uiText.asString(context)
            _binding.txt2.text = context.getString(R.string.empty_task_list_action)
        }
    }
}
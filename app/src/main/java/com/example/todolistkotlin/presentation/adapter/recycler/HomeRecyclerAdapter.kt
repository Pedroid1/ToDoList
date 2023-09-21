package com.example.todolistkotlin.presentation.adapter.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.todolistkotlin.databinding.EmptyListRecyclerItemBinding
import com.example.todolistkotlin.presentation.model.HomeRecyclerViewItem
import com.example.todolistkotlin.enuns.EnumHomeRecyclerViewType
import com.example.todolistkotlin.databinding.LargeTaskItemBinding
import com.example.todolistkotlin.databinding.TaskDateItemBinding
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.presentation.viewholder.HomeRecyclerViewHolder

class HomeRecyclerAdapter(private val listener: HomeAdapterEvent) :
    ListAdapter<HomeRecyclerViewItem, HomeRecyclerViewHolder>(DIFFUTILS) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        return when (EnumHomeRecyclerViewType.getEnumByOrdinal(viewType)) {
            EnumHomeRecyclerViewType.DATE -> {
                return HomeRecyclerViewHolder.TaskDateItemViewHolder(
                    TaskDateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            EnumHomeRecyclerViewType.TASK -> {
                return HomeRecyclerViewHolder.TaskItem(
                    LargeTaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    listener
                )
            }
            else -> return HomeRecyclerViewHolder.Empty(
                EmptyListRecyclerItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) = when (holder) {
        is HomeRecyclerViewHolder.TaskDateItemViewHolder -> holder.bindDate(getItem(position) as HomeRecyclerViewItem.TaskDateItem)
        is HomeRecyclerViewHolder.TaskItem -> holder.bindTask(getItem(position) as HomeRecyclerViewItem.TaskItem)
        is HomeRecyclerViewHolder.Empty -> holder.bindMessage(getItem(position) as HomeRecyclerViewItem.Empty)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    companion object {
        val DIFFUTILS = object : DiffUtil.ItemCallback<HomeRecyclerViewItem>() {
            override fun areItemsTheSame(
                oldItem: HomeRecyclerViewItem,
                newItem: HomeRecyclerViewItem
            ): Boolean {
                return when {
                    oldItem is HomeRecyclerViewItem.TaskItem && newItem is HomeRecyclerViewItem.TaskItem -> {
                        return when {
                            oldItem.task.id != newItem.task.id -> false
                            oldItem.task.title != newItem.task.title -> false
                            oldItem.task.completed != newItem.task.completed -> false
                            else -> true
                        }
                    }
                    oldItem is HomeRecyclerViewItem.TaskDateItem && newItem is HomeRecyclerViewItem.TaskDateItem -> {
                        return when {
                            oldItem.uiText != newItem.uiText -> false
                            else -> true
                        }
                    }
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: HomeRecyclerViewItem,
                newItem: HomeRecyclerViewItem
            ): Boolean {
                return when {
                    oldItem is HomeRecyclerViewItem.TaskItem && newItem is HomeRecyclerViewItem.TaskItem -> {
                        return when {
                            oldItem.task.id != newItem.task.id -> false
                            oldItem.task.title != newItem.task.title -> false
                            oldItem.task.completed != newItem.task.completed -> false
                            else -> true
                        }
                    }
                    oldItem is HomeRecyclerViewItem.TaskDateItem && newItem is HomeRecyclerViewItem.TaskDateItem -> {
                        return when {
                            oldItem.uiText != newItem.uiText -> false
                            else -> true
                        }
                    }
                    else -> false
                }
            }
        }
    }

    interface HomeAdapterEvent {
        fun onTaskClicked(task: Task)
    }

}
package com.example.todolistkotlin.presentation.adapter.recycler

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.databinding.CategoryItemBinding
import com.example.todolistkotlin.databinding.EmptyListRecyclerItemBinding
import com.example.todolistkotlin.enuns.EnumCategoryRecyclerViewType
import com.example.todolistkotlin.presentation.fragment.CategoryFragmentDirections
import com.example.todolistkotlin.presentation.model.CategoryRecyclerViewItem

class CategoryAdapter(private val taskList: List<Task>) :
    ListAdapter<CategoryRecyclerViewItem, CategoryAdapter.CategoryViewHolder>(DIFFUTILS) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return when (EnumCategoryRecyclerViewType.getEnumByOrdinal(viewType)) {
            EnumCategoryRecyclerViewType.CATEGORY -> {
                CategoryViewHolder.CategoryItem(
                    CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            else -> {
                return CategoryViewHolder.Empty(
                    EmptyListRecyclerItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) = when (holder) {
        is CategoryViewHolder.CategoryItem -> {
            val category = (getItem(position) as CategoryRecyclerViewItem.CategoryItem)
            val numberOfTasks = numberOfTasks(category.category)
            holder.bind(category, numberOfTasks)
        }
        is CategoryViewHolder.Empty -> holder.bindMessage((getItem(position) as CategoryRecyclerViewItem.Empty))
    }

    private fun numberOfTasks(category: Category): Int {
        return taskList.count { it.categoryId == category.id && !it.completed }
    }

    sealed class CategoryViewHolder(val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        class CategoryItem(private val _binding: CategoryItemBinding) :
            CategoryViewHolder(_binding) {
            fun bind(categoryItem: CategoryRecyclerViewItem.CategoryItem, numberOfTasks: Int) {
                val currentCategory = categoryItem.category
                _binding.apply {
                    category = currentCategory
                    totalTaskTxt.text = "$numberOfTasks tarefas"
                    categoryLayout.setBackgroundColor(Color.parseColor(currentCategory.categoryColor))
                    imgCategory.setImageResource(currentCategory.categoryIcon)

                    ViewCompat.setScreenReaderFocusable(rootLayout, true)

                    rootLayout.setOnClickListener {
                        val action =
                            CategoryFragmentDirections.actionCategoryFragmentToCategoryDetailsFragment(
                                currentCategory.id,
                                currentCategory.name
                            )
                        binding.root.findNavController().navigate(action)
                    }
                }
            }
        }

        class Empty(private val _binding: EmptyListRecyclerItemBinding) :
            CategoryViewHolder(_binding) {
            fun bindMessage(emptyMessage: CategoryRecyclerViewItem.Empty) {
                val context = _binding.root.context
                _binding.txt1.text = emptyMessage.uiText.asString(context)
                _binding.txt2.text = context.getString(R.string.empty_task_list_action)
            }
        }
    }

    companion object {
        val DIFFUTILS = object : DiffUtil.ItemCallback<CategoryRecyclerViewItem>() {
            override fun areItemsTheSame(
                oldItem: CategoryRecyclerViewItem,
                newItem: CategoryRecyclerViewItem
            ): Boolean {
                return when {
                    oldItem is CategoryRecyclerViewItem.CategoryItem && newItem is CategoryRecyclerViewItem.CategoryItem -> {
                        oldItem.category.id == newItem.category.id
                    }
                    oldItem is CategoryRecyclerViewItem.Empty && newItem is CategoryRecyclerViewItem.Empty -> {
                        oldItem.uiText == newItem.uiText
                    }
                    else -> {
                        false
                    }
                }
            }

            override fun areContentsTheSame(
                oldItem: CategoryRecyclerViewItem,
                newItem: CategoryRecyclerViewItem
            ): Boolean {
                return when {
                    oldItem is CategoryRecyclerViewItem.CategoryItem && newItem is CategoryRecyclerViewItem.CategoryItem -> {
                        oldItem.category.id == newItem.category.id
                    }
                    oldItem is CategoryRecyclerViewItem.Empty && newItem is CategoryRecyclerViewItem.Empty -> {
                        oldItem.uiText == newItem.uiText
                    }
                    else -> {
                        false
                    }
                }
            }

        }
    }

}
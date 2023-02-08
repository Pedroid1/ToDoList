package com.example.todolistkotlin.presentation.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.FragmentTaskDetailBinding
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.example.todolistkotlin.presentation.viewmodel.MainViewModel
import com.example.todolistkotlin.util.toGone

class TaskDetailFragment : Fragment() {

    private lateinit var _binding: FragmentTaskDetailBinding
    private val viewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }
    private val args: TaskDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(layoutInflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialWork()
        prepareViewListener()
    }

    private fun prepareViewListener() {
        _binding.header.apply {
            setLeftIconListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            setRightIconListener {
                //TODO
            }
        }
    }

    private fun prepareButtonsListener() {
        _binding.apply {
            btnDeleteOpenTask.setOnClickListener {
                deleteTask(args.taskWithCategory.task)
            }
            btnComplete.setOnClickListener {
                completeTask(args.taskWithCategory.task)
            }
            btnDeleteFinishedTask.setOnClickListener {
                deleteTask(args.taskWithCategory.task)
            }
        }
    }

    private fun deleteTask(task: Task) {
        viewModel.onTaskEvent(TaskEvent.Delete(task))
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun completeTask(task: Task) {
        viewModel.onTaskEvent(TaskEvent.Complete(task))
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun initialWork() {
        args.let {
            val task = it.taskWithCategory.task
            _binding.task = task
            _binding.txtDescription.text = task.description.ifEmpty { "-" }

            val category = it.taskWithCategory.category
            _binding.layoutCategory.category = category
            _binding.layoutCategory.categoryLayout.setBackgroundColor(Color.parseColor(category.categoryColor))
            _binding.layoutCategory.imgCategory.setImageResource(category.categoryIcon)
            _binding.layoutCategory.totalTaskTxt.toGone()

            when (task.priority) {
                EnumTaskPriority.LOW -> {
                    _binding.flag.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.low_priority_color
                        )
                    )
                    _binding.flag.contentDescription = getString(R.string.low_priority)
                    _binding.txtPriority.text = getString(R.string.low)
                }
                EnumTaskPriority.MEDIUM -> {
                    _binding.flag.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.medium_priority_color
                        )
                    )
                    _binding.flag.contentDescription = getString(R.string.medium_priority)
                    _binding.txtPriority.text = getString(R.string.medium)
                }
                else -> {
                    _binding.flag.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.high_priority_color
                        )
                    )
                    _binding.flag.contentDescription = getString(R.string.high_priority)
                    _binding.txtPriority.text = getString(R.string.high)
                }
            }
            prepareButtonsListener()
        }
    }
}
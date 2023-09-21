package com.example.todolistkotlin.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.FragmentCalendarBinding
import com.example.todolistkotlin.presentation.model.CalendarModel
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.presentation.adapter.SwipeTouchHelper
import com.example.todolistkotlin.presentation.adapter.recycler.CalendarAdapter
import com.example.todolistkotlin.presentation.adapter.recycler.HomeRecyclerAdapter
import com.example.todolistkotlin.presentation.states.HomeViewState
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.example.todolistkotlin.presentation.viewmodel.HomeViewModel
import com.example.todolistkotlin.util.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class CalendarFragment : Fragment(), CalendarAdapter.OnDayClickListener,
    SwipeTouchHelper.SwipeTouchHelperEvent, HomeRecyclerAdapter.HomeAdapterEvent {

    private lateinit var _binding: FragmentCalendarBinding
    private val homeViewModel: HomeViewModel by lazy { ViewModelProvider(requireActivity())[HomeViewModel::class.java] }

    private val recyclerAdapter = HomeRecyclerAdapter(this)
    private val calendarViewDate: Calendar = Calendar.getInstance()
    private val calendarAdapter: CalendarAdapter = CalendarAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialWork()
        prepareViewListener()
        observer()
    }

    private fun prepareViewListener() {
        _binding.calendarView.apply {
            forwardArrow.setOnClickListener {
                calendarViewDate.add(Calendar.MONTH, 1)
                setupCalendarView(homeViewModel.taskList)
            }
            backArrow.setOnClickListener {
                calendarViewDate.add(Calendar.MONTH, -1)
                setupCalendarView(homeViewModel.taskList)
            }
        }
    }

    private fun initialWork() {
        calendarViewDate.timeInMillis = homeViewModel.selectedTimeCalendarFragment!!
        val itemTouchHelper = ItemTouchHelper(
            SwipeTouchHelper(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
                recyclerAdapter,
                this
            )
        )
        itemTouchHelper.attachToRecyclerView(_binding.calendarTaskRecycler)
    }

    private fun setupCalendarView(taskList: List<Task>) =
        lifecycleScope.launch(Dispatchers.Default) {
            val calendarDays = daysInMonthArray(taskList)
            launch(Dispatchers.Main) {
                val date = DateUtils.monthYearOfDate(calendarViewDate.timeInMillis)
                _binding.calendarView.monthYearTV.text = date
                _binding.calendarView.calendarRecyclerView.adapter = calendarAdapter
                calendarAdapter.submitList(calendarDays)
            }
        }

    private fun daysInMonthArray(taskList: List<Task>): ArrayList<CalendarModel> {
        val daysInMonthArray = ArrayList<CalendarModel>()
        val daysInMonth = calendarViewDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendarViewDate.set(
            Calendar.DAY_OF_MONTH,
            calendarViewDate.getActualMinimum(Calendar.DAY_OF_MONTH)
        )
        val dayOfWeek = calendarViewDate.get(Calendar.DAY_OF_WEEK) - 1
        for (i in 1..42) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add(CalendarModel())
            } else {
                val calendarModel = CalendarModel()
                calendarModel.timeInMillis = calendarViewDate.timeInMillis
                calendarModel.haveEvents = checkIfHaveEvents(taskList)
                calendarModel.isSelected =
                    calendarViewDate.timeInMillis == homeViewModel.selectedTimeCalendarFragment!!
                daysInMonthArray.add(calendarModel)
                calendarViewDate.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        calendarViewDate.add(Calendar.MONTH, -1)
        return daysInMonthArray
    }

    private fun checkIfHaveEvents(taskList: List<Task>): Boolean {
        return taskList.any {
            DateUtils.longIntoDate(it.dateInMills) == DateUtils.longIntoDate(calendarViewDate.timeInMillis) && !it.completed
        }
    }

    private fun undoDeleteTask(task: Any) {
        homeViewModel.onTaskEvent(TaskEvent.RestoreDelete(task as Task))
    }

    private fun deleteTask(task: Any) {
        homeViewModel.deleteTask((task as Task).id)
    }

    private fun completeTask(task: Any) {
        homeViewModel.completeTask(task as Task)
    }

    private fun undoCompletedTask(task: Any) {
        homeViewModel.onTaskEvent(TaskEvent.RestoreComplete(task as Task))
    }

    private fun handleMainViewState(state: HomeViewState) {
        _binding.loadingLayout.root.setupLoadingView(!state.isFetchCompleted)
        if (state.taskList != null) {
            prepareRecyclerView(state.taskList)
            setupCalendarView(state.taskList)
        }
        state.error?.let {
            //TODO - Handle error
        }
    }

    private fun observer() {
        viewLifecycleOwner.apply {
            observe(homeViewModel.homeViewState, ::handleMainViewState)
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.errorEvent.collectLatest {
                    _binding.root.showSnackBar(
                        it.uiText.asString(requireContext()),
                        Snackbar.LENGTH_LONG,
                        R.id.floating_btn
                    )
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.taskEvent.collectLatest { event ->
                    when (event) {
                        is TaskEvent.Complete -> {
                            _binding.root.showSnackBarWithAction(
                                getString(R.string.complete_task),
                                getString(R.string.undo),
                                ::undoCompletedTask,
                                ::completeTask,
                                event.task,
                                Snackbar.LENGTH_LONG,
                                R.id.floating_btn
                            )
                        }
                        is TaskEvent.Delete -> {
                            _binding.root.showSnackBarWithAction(
                                getString(R.string.task_deleted),
                                getString(R.string.undo),
                                ::undoDeleteTask,
                                ::deleteTask,
                                event.task,
                                Snackbar.LENGTH_LONG,
                                R.id.floating_btn
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun prepareRecyclerView(taskList: List<Task>) {
        _binding.calendarTaskRecycler.adapter = recyclerAdapter
        lifecycleScope.launch(Dispatchers.Default) {
            val recyclerList = homeViewModel.getCalendarRecyclerList(
                homeViewModel.selectedTimeCalendarFragment!!,
                taskList
            )
            lifecycleScope.launch(Dispatchers.Main) {
                recyclerAdapter.submitList(recyclerList)
            }
        }
    }

    override fun onDayClickListener(timeInMillis: Long) {
        homeViewModel.selectedTimeCalendarFragment = timeInMillis
        setupCalendarView(homeViewModel.taskList)
        prepareRecyclerView(homeViewModel.taskList)
    }

    override fun onTaskSwiped(taskEvent: TaskEvent) {
        homeViewModel.onTaskEvent(taskEvent)
    }

    override fun onTaskClicked(task: Task) {
        val directions = CalendarFragmentDirections.actionCalendarFragmentToTaskDetailFragment(task)
        findNavController().navigate(directions)
    }
}
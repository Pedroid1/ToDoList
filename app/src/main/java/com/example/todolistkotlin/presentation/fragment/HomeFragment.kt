package com.example.todolistkotlin.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.FragmentHomeBinding
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.adapter.SwipeTouchHelper
import com.example.todolistkotlin.presentation.adapter.recycler.HomeRecyclerAdapter
import com.example.todolistkotlin.presentation.states.HomeViewState
import com.example.todolistkotlin.presentation.states.UserInfoState
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.example.todolistkotlin.presentation.viewmodel.HomeViewModel
import com.example.todolistkotlin.util.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), SwipeTouchHelper.SwipeTouchHelperEvent,
    HomeRecyclerAdapter.HomeAdapterEvent {

    private lateinit var _binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by lazy { ViewModelProvider(requireActivity())[HomeViewModel::class.java] }
    private val adapter = HomeRecyclerAdapter(this)

    private val swipeTouchHelper = SwipeTouchHelper(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
        adapter,
        this
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialWork()
        prepareViewListener()
        observer()
    }

    private fun prepareViewListener() {
        _binding.idchipGroup.setOnCheckedStateChangeListener { group, _ ->
            when (group.checkedChipId) {
                R.id.chip_all -> {
                    viewModel.setTaskFilter(TaskFilter.All())
                    swipeTouchHelper.setTaskFilter(TaskFilter.All())
                }

                R.id.chip_today -> {
                    viewModel.setTaskFilter(TaskFilter.Today())
                    swipeTouchHelper.setTaskFilter(TaskFilter.Today())
                }

                R.id.chip_upcoming -> {
                    viewModel.setTaskFilter(TaskFilter.Upcoming())
                    swipeTouchHelper.setTaskFilter(TaskFilter.Upcoming())
                }

                R.id.chip_completed -> {
                    viewModel.setTaskFilter(TaskFilter.Completed())
                    swipeTouchHelper.setTaskFilter(TaskFilter.Completed())
                }
            }
        }

        _binding.floatingBtn.setOnClickListener {
            navigateToCreateTaskFragment()
        }
    }

    private fun undoDeleteTask(task: Any) {
        viewModel.onTaskEvent(TaskEvent.RestoreDelete(task as Task))
    }

    private fun deleteTask(task: Any) {
        viewModel.deleteTask((task as Task).id)
    }

    private fun completeTask(task: Any) {
        viewModel.completeTask(task as Task)
    }

    private fun undoCompletedTask(task: Any) {
        viewModel.onTaskEvent(TaskEvent.RestoreComplete(task as Task))
    }

    private fun handleHomeViewState(state: HomeViewState) {
        _binding.loadingLayout.root.setupLoadingView(!state.isFetchCompleted)
        if (state.taskList != null) {
            prepareRecyclerView(state.taskList, state.taskFilter)
        }
        state.error?.let {
            //TODO - Exibir layout de error
        }
    }

    private fun handleUserInfoState(state: UserInfoState) {
        val salute = try {
            val firstName = state.username!!.split(" ")[0]
            getString(R.string.salute_with_username, firstName)
        } catch (e: Exception) {
            getString(R.string.salute_without_username)
        }
        _binding.saluteTxt.text = salute
    }

    private fun observer() {
        viewLifecycleOwner.apply {
            observe(viewModel.homeViewState, ::handleHomeViewState)
            observe(viewModel.userInfoState, ::handleUserInfoState)
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorEvent.collectLatest {
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
                viewModel.taskEvent.collectLatest { event ->
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

    private fun prepareRecyclerView(
        tasks: List<Task>,
        taskFilter: TaskFilter
    ) {
        lifecycleScope.launch(Dispatchers.Default) {
            val recyclerList = viewModel.getHomeRecyclerList(tasks, taskFilter)
            lifecycleScope.launch(Dispatchers.Main) { adapter.submitList(recyclerList) }
        }
    }

    private fun initialWork() {
        _binding.taskRecyclerView.adapter = adapter
        val itemTouchHelper = ItemTouchHelper(swipeTouchHelper)
        itemTouchHelper.attachToRecyclerView(_binding.taskRecyclerView)
    }

    override fun onTaskSwiped(taskEvent: TaskEvent) {
        viewModel.onTaskEvent(taskEvent)
    }

    override fun onTaskClicked(task: Task) {
        val directions = HomeFragmentDirections.actionHomeFragmentToTaskDetailFragment(task)
        findNavController().navigate(directions)
    }

    private fun navigateToCreateTaskFragment() {
        val action = HomeFragmentDirections.actionHomeFragmentToCreateTaskFragment()
        findNavController().navigate(action)
    }
}
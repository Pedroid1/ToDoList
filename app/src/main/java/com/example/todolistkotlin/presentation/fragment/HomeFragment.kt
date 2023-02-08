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
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.adapter.SwipeTouchHelper
import com.example.todolistkotlin.presentation.adapter.recycler.HomeRecyclerAdapter
import com.example.todolistkotlin.presentation.model.TaskWithCategory
import com.example.todolistkotlin.presentation.states.MainViewState
import com.example.todolistkotlin.presentation.states.UserInfoState
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.example.todolistkotlin.presentation.viewmodel.MainViewModel
import com.example.todolistkotlin.util.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), SwipeTouchHelper.OnTaskEvent,
    HomeRecyclerAdapter.TaskAdapterListener {

    private lateinit var _binding: FragmentHomeBinding
    private val viewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }
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
        _binding.chipAll.setOnClickListener {
            viewModel.setTaskFilter(TaskFilter.All)
            swipeTouchHelper.setTaskFilter(TaskFilter.All)
        }
        _binding.chipToday.setOnClickListener {
            viewModel.setTaskFilter(TaskFilter.Today)
            swipeTouchHelper.setTaskFilter(TaskFilter.Today)
        }
        _binding.chipUpcoming.setOnClickListener {
            viewModel.setTaskFilter(TaskFilter.Upcoming)
            swipeTouchHelper.setTaskFilter(TaskFilter.Upcoming)
        }
        _binding.chipCompleted.setOnClickListener {
            viewModel.setTaskFilter(TaskFilter.Completed)
            swipeTouchHelper.setTaskFilter(TaskFilter.Completed)
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

    private fun handleHomeViewState(state: MainViewState) {
        _binding.loadingLayout.root.setupLoadingView(!state.isComplete)
        if (state.taskList != null && state.categoryList != null) {
            prepareRecyclerView(state.taskList, state.categoryList, state.taskFilter)
        }
        state.error?.let {
            //TODO - Handle error
        }
    }

    private fun handleUserInfoState(state: UserInfoState) {
        if (state.username != null) {
            try {
                val firstName = state.username.split(" ")[0]
                _binding.saluteTxt.text = getString(R.string.salute_with_username, firstName)
            } catch (e: Exception) {
                _binding.saluteTxt.text = getString(R.string.salute_without_username)
            }
        } else {
            _binding.saluteTxt.text = getString(R.string.salute_without_username)
        }
    }

    private fun observer() {
        viewLifecycleOwner.apply {
            observe(viewModel.homeViewState, ::handleHomeViewState)
            observe(viewModel.userInfoState, ::handleUserInfoState)
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorEvent.collectLatest {
                    _binding.root.showSnackBar(it.uiText.asString(requireContext()), Snackbar.LENGTH_LONG, R.id.floating_btn)
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
        categories: List<Category>,
        taskFilter: TaskFilter
    ) {
        lifecycleScope.launch(Dispatchers.Default) {
            val recyclerList = viewModel.getRecyclerViewMainList(tasks, categories, taskFilter)
            lifecycleScope.launch(Dispatchers.Main) { adapter.submitList(recyclerList) }
        }
    }

    private fun initialWork() {
        _binding.taskRecyclerView.adapter = adapter
        val itemTouchHelper = ItemTouchHelper(swipeTouchHelper)
        itemTouchHelper.attachToRecyclerView(_binding.taskRecyclerView)
    }

    override fun onTaskEvent(taskEvent: TaskEvent) {
        viewModel.onTaskEvent(taskEvent)
    }

    override fun onTaskClicked(taskWithCategory: TaskWithCategory) {
        val directions = HomeFragmentDirections.actionHomeFragmentToTaskDetailFragment(taskWithCategory)
        findNavController().navigate(directions)
    }
}
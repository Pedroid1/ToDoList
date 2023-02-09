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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.FragmentCategoryDetailsBinding
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.adapter.SwipeTouchHelper
import com.example.todolistkotlin.presentation.adapter.recycler.HomeRecyclerAdapter
import com.example.todolistkotlin.presentation.states.HomeViewState
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.example.todolistkotlin.presentation.viewmodel.CategoryViewModel
import com.example.todolistkotlin.presentation.viewmodel.HomeViewModel
import com.example.todolistkotlin.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryDetailsFragment : Fragment(), SwipeTouchHelper.OnTaskEvent,
    HomeRecyclerAdapter.TaskAdapterListener {

    private lateinit var _binding: FragmentCategoryDetailsBinding
    private val homeViewModel: HomeViewModel by lazy { ViewModelProvider(requireActivity())[HomeViewModel::class.java] }
    private val categoryViewModel: CategoryViewModel by lazy { ViewModelProvider(requireActivity())[CategoryViewModel::class.java] }
    private val args: CategoryDetailsFragmentArgs by navArgs()

    private val adapter = HomeRecyclerAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailsBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialWork()
        prepareViewListener()
        observer()
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
                        Snackbar.LENGTH_LONG
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
        _binding.rvTasks.adapter = adapter
        lifecycleScope.launch(Dispatchers.Default) {
            val taskListFilter = taskList.filter { it.category?.id == args.categoryId }
            val recyclerList =
                homeViewModel.getRecyclerViewMainList(taskListFilter, TaskFilter.All())
            lifecycleScope.launch(Dispatchers.Main) {
                adapter.submitList(recyclerList)
            }
        }
    }

    private fun prepareViewListener() {
        _binding.apply {
            header.apply {
                setTitle(args.categoryName)
                setLeftIconListener {
                    backToCategoryFragment()
                }
                setRightIconListener {
                    showAlertDialog()
                }
            }
        }
    }

    private fun showAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_category))
            .setMessage(R.string.delete_category_info)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                categoryViewModel.deleteCategory(args.categoryId)
                backToCategoryFragment()
            }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }

    private fun initialWork() {
        val itemTouchHelper = ItemTouchHelper(
            SwipeTouchHelper(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
                adapter,
                this
            )
        )
        itemTouchHelper.attachToRecyclerView(_binding.rvTasks)
    }

    private fun backToCategoryFragment() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onTaskEvent(taskEvent: TaskEvent) {
        homeViewModel.onTaskEvent(taskEvent)
    }

    override fun onTaskClicked(task: Task) {
        val directions =
            CategoryDetailsFragmentDirections.actionCategoryDetailsFragmentToTaskDetailFragment(task)
        findNavController().navigate(directions)
    }
}
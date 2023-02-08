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
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.databinding.FragmentCategoryDetailsBinding
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.adapter.SwipeTouchHelper
import com.example.todolistkotlin.presentation.adapter.recycler.HomeRecyclerAdapter
import com.example.todolistkotlin.presentation.model.TaskWithCategory
import com.example.todolistkotlin.presentation.states.MainViewState
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.example.todolistkotlin.presentation.viewmodel.MainViewModel
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
    private val mainViewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }
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
        mainViewModel.onTaskEvent(TaskEvent.RestoreDelete(task as Task))
    }

    private fun deleteTask(task: Any) {
        mainViewModel.deleteTask((task as Task).id)
    }

    private fun completeTask(task: Any) {
        mainViewModel.completeTask(task as Task)
    }

    private fun undoCompletedTask(task: Any) {
        mainViewModel.onTaskEvent(TaskEvent.RestoreComplete(task as Task))
    }

    private fun handleMainViewState(state: MainViewState) {
        _binding.loadingLayout.root.setupLoadingView(!state.isComplete)
        if (state.taskList != null && state.categoryList != null) {
            prepareRecyclerView(state.taskList, state.categoryList)
        }
        state.error?.let {
            //TODO - Handle error
        }
    }

    private fun observer() {
        viewLifecycleOwner.apply {
            observe(mainViewModel.homeViewState, ::handleMainViewState)
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.errorEvent.collectLatest {
                    _binding.root.showSnackBar(
                        it.uiText.asString(requireContext()),
                        Snackbar.LENGTH_LONG
                    )
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.taskEvent.collectLatest { event ->
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

    private fun prepareRecyclerView(taskList: List<Task>, categoryList: List<Category>) {
        _binding.rvTasks.adapter = adapter
        lifecycleScope.launch(Dispatchers.Default) {
            val taskListFilter = taskList.filter { it.categoryId == args.categoryId }
            val recyclerList =
                mainViewModel.getRecyclerViewMainList(taskListFilter, categoryList, TaskFilter.All)
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
                mainViewModel.deleteCategory(args.categoryId)
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
        mainViewModel.onTaskEvent(taskEvent)
    }

    override fun onTaskClicked(taskWithCategory: TaskWithCategory) {
        val directions =
            CategoryDetailsFragmentDirections.actionCategoryDetailsFragmentToTaskDetailFragment(taskWithCategory)
        findNavController().navigate(directions)
    }
}
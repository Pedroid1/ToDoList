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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.FragmentCategoryBinding
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.presentation.adapter.recycler.CategoryAdapter
import com.example.todolistkotlin.presentation.states.MainViewState
import com.example.todolistkotlin.presentation.viewmodel.MainViewModel
import com.example.todolistkotlin.util.observe
import com.example.todolistkotlin.util.setupLoadingView
import com.example.todolistkotlin.util.showSnackBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private lateinit var _binding: FragmentCategoryBinding
    private val mainViewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialWork()
        observer()
    }

    private fun handleMainViewState(state: MainViewState) {
        _binding.loadingLayout.root.setupLoadingView(!state.isComplete)
        if(state.taskList != null && state.categoryList != null) {
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
                    _binding.root.showSnackBar(it.uiText.asString(requireContext()), Snackbar.LENGTH_LONG, R.id.floating_btn)
                }
            }
        }
    }

    private fun prepareRecyclerView(taskList: List<Task>, categoryList: List<Category>) {
        if (categoryList.isEmpty()) {
            _binding.categoryRecycler.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        } else {
            _binding.categoryRecycler.layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        }
        val categoryAdapter = CategoryAdapter(taskList)
        _binding.categoryRecycler.adapter = categoryAdapter
        lifecycleScope.launch(Dispatchers.Default) {
            val categoriesList = mainViewModel.getCategoriesRecyclerItem(categoryList)
            lifecycleScope.launch(Dispatchers.Main) { categoryAdapter.submitList(categoriesList) }
        }
    }

    private fun initialWork() {
    }
}
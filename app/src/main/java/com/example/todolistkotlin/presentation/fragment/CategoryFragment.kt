package com.example.todolistkotlin.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolistkotlin.R
import com.example.todolistkotlin.databinding.FragmentCategoryBinding
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.presentation.adapter.recycler.CategoryAdapter
import com.example.todolistkotlin.presentation.states.CategoryViewState
import com.example.todolistkotlin.presentation.states.HomeViewState
import com.example.todolistkotlin.presentation.viewmodel.CategoryViewModel
import com.example.todolistkotlin.presentation.viewmodel.HomeViewModel
import com.example.todolistkotlin.util.observe
import com.example.todolistkotlin.util.setupLoadingView
import com.example.todolistkotlin.util.showSnackBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private lateinit var _binding: FragmentCategoryBinding
    private val categoryViewModel: CategoryViewModel by lazy { ViewModelProvider(requireActivity())[CategoryViewModel::class.java] }
    private val homeViewModel: HomeViewModel by lazy { ViewModelProvider(requireActivity())[HomeViewModel::class.java] }

    private lateinit var categoryAdapter: CategoryAdapter

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

    private fun setupListeners() {
        _binding.floatingBtn.setOnClickListener {
            navigateToCreateCategoryFragment()
        }
    }

    private fun handleCategoryViewState(state: CategoryViewState) {
        _binding.loadingLayout.root.setupLoadingView(!state.isFetchCompleted)
        if(state.categoryList != null) {
            prepareRecyclerView(state.categoryList)
        }
        state.error?.let {
            _binding.root.showSnackBar(it.uiText.asString(requireContext()), Snackbar.LENGTH_LONG)
        }
    }

    private fun handleHomeViewState(state: HomeViewState) {
        _binding.loadingLayout.root.setupLoadingView(!state.isFetchCompleted)
        if(state.taskList != null) {
            categoryAdapter = CategoryAdapter(state.taskList)
            viewLifecycleOwner.observe(categoryViewModel.categoryViewState, ::handleCategoryViewState)
        }
        state.error?.let {
            _binding.root.showSnackBar(it.uiText.asString(requireContext()), Snackbar.LENGTH_LONG)
        }
    }

    private fun observer() {
        viewLifecycleOwner.apply {
            observe(homeViewModel.homeViewState, ::handleHomeViewState)
        }
        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryViewModel.errorEvent.collectLatest {
                    _binding.root.showSnackBar(it.uiText.asString(requireContext()), Snackbar.LENGTH_LONG, R.id.floating_btn)
                }
            }
        }
    }

    private fun prepareRecyclerView(categoryList: List<Category>) {
        if (categoryList.isEmpty()) {
            _binding.categoryRecycler.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        } else {
            _binding.categoryRecycler.layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        }

        _binding.categoryRecycler.adapter = categoryAdapter
        lifecycleScope.launch(Dispatchers.Default) {
            val categoriesList = categoryViewModel.getCategoriesRecyclerItem(categoryList)
            lifecycleScope.launch(Dispatchers.Main) { categoryAdapter.submitList(categoriesList) }
        }
    }

    private fun initialWork() {
        setupListeners()
    }

    private fun navigateToCreateCategoryFragment() {
        val action = CategoryFragmentDirections.actionCategoryFragmentToCreateCategoryFragment(CreateCategoryFragment.CATEGORY_ROOT_SCREEN)
        findNavController().navigate(action)
    }
}
package com.example.todolistkotlin.presentation.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.todolistkotlin.R
import com.example.todolistkotlin.presentation.model.ColorItem
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.databinding.FragmentCreateCategoryBinding
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.repository.AddCategoryResponse
import com.example.todolistkotlin.presentation.adapter.recycler.ColorItemAdapter
import com.example.todolistkotlin.presentation.adapter.recycler.IconItemAdapter
import com.example.todolistkotlin.presentation.ui_events.CreateCategoryFormEvent
import com.example.todolistkotlin.presentation.viewmodel.CreateCategoryViewModel
import com.example.todolistkotlin.presentation.viewmodel.EmptyViewModel
import com.example.todolistkotlin.presentation.viewmodel.MainViewModel
import com.example.todolistkotlin.util.Utils
import com.example.todolistkotlin.util.hideKeyboard
import com.example.todolistkotlin.util.observe
import com.example.todolistkotlin.util.showSnackBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateCategoryFragment : Fragment(), ColorItemAdapter.onColorSelected,
    IconItemAdapter.onIconSelected {

    companion object {
        const val CREATE_CATEGORY_VIEW_MODEL_KEY = "CREATE_CATEGORY_VIEW_MODEL"
        const val CATEGORY_RESULT = "CATEGORY_RESULT"
        const val CATEGORY_REQUEST_KEY = "CATEGORY_REQUEST_KEY"
        const val CREATE_TASK_ROOT_SCREEN = "CREATE_TASK_SCREEN"
        const val CATEGORY_ROOT_SCREEN = "CATEGORY_SCREEN"
    }

    private lateinit var _binding: FragmentCreateCategoryBinding
    private lateinit var viewModel: CreateCategoryViewModel
    private val mainViewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }
    private val args: CreateCategoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCategoryBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialWork()
        observer()
        prepareViewListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyCreateCategoryViewModel()
    }

    private fun verifyIfExistsCategory(categoryName: String, categoryList: List<Category>): Boolean {
        return categoryList.find { it.name == categoryName } != null
    }

    private fun getCreateCategoryViewModel(): CreateCategoryViewModel {
        return ViewModelProvider(requireActivity())[CREATE_CATEGORY_VIEW_MODEL_KEY, CreateCategoryViewModel::class.java]
    }

    private fun destroyCreateCategoryViewModel() {
        ViewModelProvider(requireActivity())[CREATE_CATEGORY_VIEW_MODEL_KEY, EmptyViewModel::class.java]
    }

    private fun setupBackgroundPreview(color: String) {
        _binding.viewCategoryItem.rootLayout.setCardBackgroundColor(Color.parseColor(color))
    }

    private fun setupIconPreview(icon: Int) {
        _binding.viewCategoryItem.imgCategory.setImageResource(icon)
    }

    private fun setupCreateButtonClickable(clickable: Boolean) {
        _binding.createBtn.isClickable = clickable
    }

    private fun observer() {
        viewLifecycleOwner.apply {
            observe(viewModel.categoryColorItem, ::setupBackgroundPreview)
            observe(viewModel.categoryIconItem, ::setupIconPreview)
            observe(viewModel.createCategoryButtonClickable, ::setupCreateButtonClickable)
        }
    }

    private fun navigateToLastScreen() {
        if (args.rootActivity == CREATE_TASK_ROOT_SCREEN) {
            setFragmentResult(
                CATEGORY_REQUEST_KEY,
                bundleOf(Pair(CATEGORY_RESULT, viewModel.categoryCreated))
            )
        }
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun prepareViewListener() {
        _binding.apply {
            header.setLeftIconListener {
                navigateToLastScreen()
            }

            createBtn.setOnClickListener {
                it.hideKeyboard()
                val categoryName = _binding.categoryEdt.text.toString().trim()

                if (categoryName.isNotEmpty()) {
                    if (!verifyIfExistsCategory(categoryName, mainViewModel.categoriesList)) {
                        viewModel.createCategoryButtonClickable.postValue(false)
                        viewModel.onCategoryEvent(CreateCategoryFormEvent.SubmitCategoryForm)
                        navigateToLastScreen()
                    } else {
                        _binding.categoryInput.error = getString(R.string.category_already_exists)
                    }
                } else {
                    _binding.categoryEdt.error =
                        requireContext().getString(R.string.nome_da_categoria)
                }
            }

            categoryEdt.addTextChangedListener {
                viewCategoryItem.categoryNameTxt.text = it.toString()
                viewModel.onCategoryEvent(CreateCategoryFormEvent.NameChanged(it.toString().trim()))
                _binding.categoryEdt.error = null
            }
        }
    }

    private fun initialWork() {
        viewModel = getCreateCategoryViewModel()

        _binding.categoryEdt.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        val colorItemAdapter =
            ColorItemAdapter(Utils.getCategoryColorsList(requireContext()))
        colorItemAdapter.setOnItemSelectedListener(this)
        _binding.colorRv.adapter = colorItemAdapter

        val iconItemAdapter = IconItemAdapter(Utils.getCategoryIcons())
        iconItemAdapter.setOnIconSelectedListener(this)
        _binding.rvIcons.adapter = iconItemAdapter
    }

    override fun onColorSelected(colorItem: ColorItem) {
        viewModel.onCategoryEvent(CreateCategoryFormEvent.ColorChanged(colorItem.color))
        _binding.root.hideKeyboard()
    }

    override fun onIconSelected(icon: Int) {
        viewModel.onCategoryEvent(CreateCategoryFormEvent.IconChanged(icon))
        _binding.root.hideKeyboard()
    }
}




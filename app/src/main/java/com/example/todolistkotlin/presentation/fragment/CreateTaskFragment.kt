package com.example.todolistkotlin.presentation.fragment

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.example.todolistkotlin.databinding.FragmentCreateTaskBinding
import com.example.todolistkotlin.presentation.ui_events.CreateTaskFormEvent
import com.example.todolistkotlin.presentation.form_validation.CreateTaskFormValidation
import com.example.todolistkotlin.presentation.states.MainViewState
import com.example.todolistkotlin.presentation.viewmodel.*
import com.example.todolistkotlin.util.hideKeyboard
import com.example.todolistkotlin.util.observe
import com.example.todolistkotlin.util.showSnackBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@AndroidEntryPoint
class CreateTaskFragment : Fragment() {

    companion object {
        const val CREATE_TASK_VIEW_MODEL_KEY = "CREATE_TASK"
    }

    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var _binding: FragmentCreateTaskBinding
    private val viewModel: CreateTaskViewModel by lazy { getCreateTaskViewModel() }
    private val mainViewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }

    private lateinit var mediumFill: Drawable
    private lateinit var lowFill: Drawable
    private lateinit var highFill: Drawable
    private lateinit var lowBorder: Drawable
    private lateinit var highBorder: Drawable
    private lateinit var mediumBorder: Drawable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialWork()
        prepareViewListener()
        observer()
    }

    private fun initialWork() {
        setupEdtInputType()
        loadDrawables()
        setupFragmentResultListener()
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener(CreateCategoryFragment.CATEGORY_REQUEST_KEY) { requestKey, bundle ->
            if (requestKey == CreateCategoryFragment.CATEGORY_REQUEST_KEY) {
                val categoryCreated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(
                        CreateCategoryFragment.CATEGORY_RESULT,
                        Category::class.java
                    )
                } else {
                    bundle.getSerializable(CreateCategoryFragment.CATEGORY_RESULT) as Category
                }

                categoryCreated?.let {
                    _binding.filledExposed.setText(it.name)
                    viewModel.onEvent(CreateTaskFormEvent.CategoryChanged(it))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyCreateTaskViewModel()
    }

    private fun getCreateTaskViewModel(): CreateTaskViewModel {
        return ViewModelProvider(requireActivity())[CREATE_TASK_VIEW_MODEL_KEY, CreateTaskViewModel::class.java]
    }

    private fun destroyCreateTaskViewModel() {
        ViewModelProvider(requireActivity())[CREATE_TASK_VIEW_MODEL_KEY, EmptyViewModel::class.java]
    }

    private fun prepareViewListener() {
        _binding.apply {
            header.setLeftIconListener {
                backToHomeFragment()
            }

            taskDateEdt.setOnClickListener {
                it.hideKeyboard()
                val datePicker = DatePickerFragment()
                datePicker.show(requireActivity().supportFragmentManager, null)
            }

            taskHourEdt.setOnClickListener {
                it.hideKeyboard()
                if (viewModel.isDateSelected) {
                    openTimePicker()
                } else {
                    _binding.taskDateInput.error = getString(R.string.first_select_date_error)
                }
            }

            createBtn.setOnClickListener {
                it.hideKeyboard()
                viewModel.onEvent(CreateTaskFormEvent.SubmitTask)
                viewModel.createButtonClickable.postValue(false)
            }

            filledExposed.setOnItemClickListener { parent, _, position, _ ->
                rootLayout.requestFocus()
                when (parent.getItemAtPosition(position).toString()) {
                    activity?.getString(R.string.create_category) -> {
                        viewModel.onEvent(CreateTaskFormEvent.CategoryChanged(null))
                        val action =
                            CreateTaskFragmentDirections.actionCreateTaskFragmentToCreateCategoryFragment(
                                CreateCategoryFragment.CREATE_TASK_ROOT_SCREEN
                            )
                        findNavController().navigate(action)
                    }
                    else -> {
                        viewModel.onEvent(
                            CreateTaskFormEvent.CategoryChanged(
                                getCategoryByName(
                                    parent.getItemAtPosition(position).toString(),
                                    mainViewModel.categoriesList
                                )
                            )
                        )

                    }
                }
            }

            taskNameEdt.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    _binding.taskNameInput.error = null
                }
            }

            taskNameEdt.addTextChangedListener {
                viewModel.onEvent(CreateTaskFormEvent.TitleChanged(it.toString().trim()))
            }

            taskDescriptionEdt.addTextChangedListener {
                viewModel.onEvent(CreateTaskFormEvent.DescriptionChanged(it.toString().trim()))
            }

            filledExposed.setOnFocusChangeListener { view, _ ->
                taskCategoryInput.error = null
                view.hideKeyboard()
            }
        }

        priorityBtnListener()
    }

    private fun getCategoryByName(categoryName: String, categoryList: List<Category>): Category? {
        return categoryList.find { it.name == categoryName }
    }

    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(R.string.select_time)
            .build()

        listenerTimePicker(timePicker)
        timePicker.show(requireActivity().supportFragmentManager, null)
    }

    private fun listenerTimePicker(timePicker: MaterialTimePicker) {
        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            viewModel.onEvent(CreateTaskFormEvent.TimeChanged(hour, minute))
        }
    }

    private fun setupEdtInputType() {
        _binding.taskNameEdt.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        _binding.taskDescriptionEdt.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    }

    private fun loadFilledExposedAdapter(categoryList: List<Category>) {
        arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.drop_down_item,
            getListOfCategoryNames(categoryList)
        )
        _binding.filledExposed.setAdapter(arrayAdapter)
    }

    private fun getListOfCategoryNames(categoryList: List<Category>): List<String> {
        val list = mutableListOf<String>()
        categoryList.forEach {
            list.add(it.name)
        }
        activity?.getString(R.string.create_category)?.let { list.add(it) }
        return list
    }

    private fun loadDrawables() {
        mediumFill =
            ActivityCompat.getDrawable(requireContext(), R.drawable.medium_priority_btn_fill)!!
        lowFill = ActivityCompat.getDrawable(requireContext(), R.drawable.low_priority_btn_fill)!!
        highFill =
            ActivityCompat.getDrawable(requireContext(), R.drawable.high_priority_btn_fill)!!
        lowBorder =
            ActivityCompat.getDrawable(requireContext(), R.drawable.low_priority_btn_border)!!
        highBorder =
            ActivityCompat.getDrawable(requireContext(), R.drawable.high_priority_btn_border)!!
        mediumBorder =
            ActivityCompat.getDrawable(requireContext(), R.drawable.medium_priority_btn_border)!!
    }

    private fun priorityBtnListener() {
        _binding.lowPriorityBtn.setOnClickListener {
            viewModel.onEvent(CreateTaskFormEvent.PriorityChanged(EnumTaskPriority.LOW))
        }
        _binding.mediumPriorityBtn.setOnClickListener {
            viewModel.onEvent(CreateTaskFormEvent.PriorityChanged(EnumTaskPriority.MEDIUM))
        }
        _binding.highPriorityBtn.setOnClickListener {
            viewModel.onEvent(CreateTaskFormEvent.PriorityChanged(EnumTaskPriority.HIGH))
        }
    }

    private fun setupPriorityButtons(priority: EnumTaskPriority) {
        _binding.taskPriorityLayoutError.error = null
        _binding.lowPriorityBtn.background = lowBorder
        _binding.mediumPriorityBtn.background = mediumBorder
        _binding.highPriorityBtn.background = highBorder

        _binding.lowPriorityBtn.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.low_priority_color
            )
        )
        _binding.mediumPriorityBtn.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.medium_priority_color
            )
        )
        _binding.highPriorityBtn.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.high_priority_color
            )
        )

        when (priority) {
            EnumTaskPriority.LOW -> {
                _binding.lowPriorityBtn.background = lowFill
                _binding.lowPriorityBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
            EnumTaskPriority.MEDIUM -> {
                _binding.mediumPriorityBtn.background = mediumFill
                _binding.mediumPriorityBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
            EnumTaskPriority.HIGH -> {
                _binding.highPriorityBtn.background = highFill
                _binding.highPriorityBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
        }
    }

    private fun handleHomeViewState(state: MainViewState) {
        if (state.categoryList != null) {
            loadFilledExposedAdapter(state.categoryList)
        }
        state.error?.let {
            //TODO - Handle error
        }
    }

    private fun setupCreateButtonClickable(clickable: Boolean) {
        _binding.createBtn.isClickable = clickable
    }

    private fun observer() {
        viewLifecycleOwner.apply {
            observe(mainViewModel.homeViewState, ::handleHomeViewState)
            observe(viewModel.selectedPriority, ::setupPriorityButtons)
            observe(viewModel.createButtonClickable, ::setupCreateButtonClickable)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.formErrorChannel.collectLatest { validation ->
                when (validation) {
                    is CreateTaskFormValidation.TitleValidation -> {
                        _binding.taskNameInput.error =
                            if (validation.result.errorMessage != null) validation.result.errorMessage.asString(
                                requireContext()
                            ) else null
                    }
                    is CreateTaskFormValidation.CategoryValidation -> {
                        _binding.taskCategoryInput.error =
                            if (validation.result.errorMessage != null) validation.result.errorMessage.asString(
                                requireContext()
                            ) else null
                    }
                    is CreateTaskFormValidation.DateValidation -> {
                        _binding.taskDateInput.error =
                            if (validation.result.errorMessage != null) validation.result.errorMessage.asString(
                                requireContext()
                            ) else null
                    }
                    is CreateTaskFormValidation.TimeValidation -> {
                        _binding.taskHourInput.error =
                            if (validation.result.errorMessage != null) validation.result.errorMessage.asString(
                                requireContext()
                            ) else null
                    }
                    is CreateTaskFormValidation.PriorityValidation -> {
                        _binding.taskPriorityLayoutError.error =
                            if (validation.result.errorMessage != null) validation.result.errorMessage.asString(
                                requireContext()
                            ) else null
                    }
                }
            }
        }

        viewModel.apply {
            submitTaskSuccessful.observe(viewLifecycleOwner) { successful ->
                if(successful) backToHomeFragment()
            }

            selectedDateLiveData.observe(viewLifecycleOwner) {
                _binding.taskDateEdt.setText(it)
            }
            selectedTimeLiveData.observe(viewLifecycleOwner) {
                _binding.taskHourEdt.setText(it)
            }
        }
    }

    private fun backToHomeFragment() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
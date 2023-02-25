package com.example.todolistkotlin.presentation.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.repository.AddTaskResponse
import com.example.todolistkotlin.domain.use_case.category.CategoryUseCases
import com.example.todolistkotlin.domain.use_case.task.TaskUseCases
import com.example.todolistkotlin.domain.use_case.validation.create_task.ValidationCreateTaskUseCases
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.example.todolistkotlin.presentation.ui_events.CreateTaskFormEvent
import com.example.todolistkotlin.presentation.form_validation.CreateTaskFormValidation
import com.example.todolistkotlin.util.DateUtils
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    private val categoryUseCases: CategoryUseCases,
    private val validationUseCases: ValidationCreateTaskUseCases
) : ViewModel() {

    private val _addTaskResult = MutableLiveData<AddTaskResponse>()
    val addTaskResult: LiveData<AddTaskResponse> get() = _addTaskResult

    private var isTaskForToday: Boolean = false
    val createButtonClickable = ObservableBoolean(true)
    var isDateSelected: Boolean = false
    var isTimeSelected: Boolean = false

    private var title: String = ""
    private var description: String = ""
    private var taskDate = Calendar.getInstance()
    private var categoryId: String? = ""
    private var priority: EnumTaskPriority? = null

    private val _formErrorChannel = Channel<CreateTaskFormValidation>()
    val formErrorChannel = _formErrorChannel.receiveAsFlow()

    private var _selectedPriority = MutableLiveData<EnumTaskPriority>()
    val selectedPriority: LiveData<EnumTaskPriority>
        get() = _selectedPriority

    private val _selectedDateMutableLiveData = MutableLiveData<String>()
    val selectedDateLiveData: LiveData<String>
        get() = _selectedDateMutableLiveData

    private val _selectedTimeMutableLiveDate = MutableLiveData<String>()
    val selectedTimeLiveData: LiveData<String>
        get() = _selectedTimeMutableLiveDate

    fun onEvent(event: CreateTaskFormEvent) {
        when (event) {
            is CreateTaskFormEvent.TitleChanged -> title = event.title
            is CreateTaskFormEvent.DescriptionChanged -> description = event.description
            is CreateTaskFormEvent.CategoryChanged -> categoryId = event.categoryId
            is CreateTaskFormEvent.DateChanged -> {
                setSelectedDate(
                    event.day,
                    event.month,
                    event.year
                )
            }
            is CreateTaskFormEvent.TimeChanged -> {
                setSelectedTime(
                    event.hour,
                    event.minute
                )
            }
            is CreateTaskFormEvent.PriorityChanged -> {
                priority = event.priority
                _selectedPriority.value = event.priority
            }
            is CreateTaskFormEvent.SubmitTask -> {
                createButtonClickable.set(false)
                submitTask()
            }
        }
    }

    private fun submitTask() {
        val titleResult = validationUseCases.validateTitle(title)
        val categoryResult = validationUseCases.validateCategory(categoryId)
        val dateResult = validationUseCases.validateDate(isDateSelected)
        val timeResult =
            validationUseCases.validateTime(taskDate.timeInMillis, isTimeSelected, isTaskForToday)
        val priorityResult = validationUseCases.validatePriority(priority)

        viewModelScope.launch {
            _formErrorChannel.send(CreateTaskFormValidation.TitleValidation(titleResult))
            _formErrorChannel.send(CreateTaskFormValidation.CategoryValidation(categoryResult))
            _formErrorChannel.send(CreateTaskFormValidation.DateValidation(dateResult))
            _formErrorChannel.send(CreateTaskFormValidation.TimeValidation(timeResult))
            _formErrorChannel.send(CreateTaskFormValidation.PriorityValidation(priorityResult))
        }
        val hasError = listOf(
            titleResult,
            categoryResult,
            dateResult,
            timeResult,
            priorityResult
        ).any { !it.successful }
        if (!hasError) {
            viewModelScope.launch(Dispatchers.IO) {
                when (val categoryRef = categoryUseCases.getCategoryRefById(categoryId!!)) {
                    is Response.Success -> {
                        addTask(title, description, categoryRef.data, priority!!, taskDate.timeInMillis)
                    }
                    is Response.Failure -> {
                        _addTaskResult.postValue(Response.Failure(categoryRef.e))
                    }
                }
            }
        } else createButtonClickable.set(true)
    }

    private fun addTask(title: String, description: String, categoryRef: DocumentReference, priority: EnumTaskPriority, taskDate: Long) = viewModelScope.launch(Dispatchers.IO) {
        _addTaskResult.postValue(taskUseCases.addTask(title, description, categoryRef, priority, taskDate))
    }

    private fun setSelectedDate(day: Int, month: Int, year: Int) {
        isDateSelected = true
        taskDate.set(year, month, day)
        val selectedDate = DateUtils.longIntoDate(taskDate.timeInMillis)
        _selectedDateMutableLiveData.value = selectedDate

        isTaskForToday = selectedDate == DateUtils.longIntoDate(Calendar.getInstance().timeInMillis)
        if (isTimeSelected) {
            viewModelScope.launch {
                _formErrorChannel.send(
                    CreateTaskFormValidation.TimeValidation(
                        validationUseCases.validateTime(
                            taskDate.timeInMillis,
                            isTimeSelected,
                            isTaskForToday
                        )
                    )
                )
            }
        }
        viewModelScope.launch {
            _formErrorChannel.send(
                CreateTaskFormValidation.DateValidation(
                    validationUseCases.validateDate(isDateSelected)
                )
            )
        }
    }

    private fun setSelectedTime(hour: Int, minute: Int) {
        isTimeSelected = true
        taskDate.set(Calendar.HOUR_OF_DAY, hour)
        taskDate.set(Calendar.MINUTE, minute)
        _selectedTimeMutableLiveDate.postValue(DateUtils.longIntoTime(taskDate.timeInMillis))

        viewModelScope.launch {
            _formErrorChannel.send(
                CreateTaskFormValidation.TimeValidation(
                    validationUseCases.validateTime(
                        taskDate.timeInMillis,
                        isTimeSelected,
                        isTaskForToday
                    )
                )
            )
        }
    }
}
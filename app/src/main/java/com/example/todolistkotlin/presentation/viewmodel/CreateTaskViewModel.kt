package com.example.todolistkotlin.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.repository.AddTaskResponse
import com.example.todolistkotlin.domain.use_case.task.TaskUseCases
import com.example.todolistkotlin.domain.use_case.validation.create_task.ValidationCreateTaskUseCases
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.example.todolistkotlin.presentation.ui_events.CreateTaskFormEvent
import com.example.todolistkotlin.presentation.form_validation.CreateTaskFormValidation
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.example.todolistkotlin.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases
) : ViewModel() {

    val submitTaskSuccessful = MutableLiveData<Boolean>(false)

    private var isTaskForToday: Boolean = false
    val createButtonClickable = MutableLiveData(true)
    var isDateSelected: Boolean = false
    var isTimeSelected: Boolean = false
    private val validationUseCases = ValidationCreateTaskUseCases()

    private var title: String = ""
    private var description: String = ""
    private var taskDate = Calendar.getInstance()
    private var category: Category? = null
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
            is CreateTaskFormEvent.SubmitTask -> Unit
            else -> createButtonClickable.postValue(true)
        }
        when (event) {
            is CreateTaskFormEvent.TitleChanged -> title = event.title
            is CreateTaskFormEvent.DescriptionChanged -> description = event.description
            is CreateTaskFormEvent.CategoryChanged -> category = event.categoryChanged
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
            is CreateTaskFormEvent.SubmitTask -> submitTask()
        }
    }

    private fun submitTask() {
        val titleResult = validationUseCases.validateTitle(title)
        val categoryResult = validationUseCases.validateCategory(category)
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
            val task = Task(
                "",
                title,
                description,
                category!!.name,
                priority!!,
                taskDate.timeInMillis
            )
            addTask(task)
            submitTaskSuccessful.postValue(true)
        } else createButtonClickable.postValue(true)
    }

    private fun addTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        taskUseCases.addTask(task)
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
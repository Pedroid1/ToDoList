package com.example.todolistkotlin.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.business.MainActivityModel
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.presentation.model.HomeRecyclerViewItem
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.repository.DeleteCategoryResponse
import com.example.todolistkotlin.domain.use_case.category.CategoryUseCases
import com.example.todolistkotlin.domain.use_case.task.TaskUseCases
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.UiText
import com.example.todolistkotlin.presentation.model.CategoryRecyclerViewItem
import com.example.todolistkotlin.presentation.model.TaskWithCategory
import com.example.todolistkotlin.presentation.states.MainViewState
import com.example.todolistkotlin.presentation.states.UserInfoState
import com.example.todolistkotlin.presentation.ui_events.ErrorEvent
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.example.todolistkotlin.util.DateUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class MainViewModel @Inject constructor(
    private val model: MainActivityModel,
    private val taskUseCases: TaskUseCases,
    private val categoryUseCases: CategoryUseCases
) : ViewModel() {

    private var pendingTaskDelete: Task? = null
    private var pendingTaskComplete: Task? = null

    var selectedTimeCalendarFragment: Long? = null

    private val _mainViewState = MutableLiveData<MainViewState>()
    val homeViewState get() = _mainViewState

    private val _userInfoState = MutableLiveData<UserInfoState>()
    val userInfoState get() = _userInfoState

    private val _errorEventChannel = Channel<ErrorEvent>()
    val errorEvent = _errorEventChannel.receiveAsFlow()

    private val _taskEventChannel = Channel<TaskEvent>()
    val taskEvent = _taskEventChannel.receiveAsFlow()

    var taskList: MutableList<Task> = ArrayList()
    var categoriesList: List<Category> = ArrayList()

    val coroutineHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("coroutineHandler", "Exception: $throwable")
    }

    init {
        selectedTimeCalendarFragment = DateUtils.getTimeInMillisResetedTime(Calendar.getInstance())
        getUserInfo()
        _mainViewState.value = MainViewState(
            false,
            null,
            null,
            null
        )
        getTasksFromRepository()
    }

    private fun getUserInfo() = viewModelScope.launch(Dispatchers.IO + coroutineHandler) {
        val user = Firebase.auth.currentUser
        user?.let {
            _userInfoState.postValue(
                UserInfoState(
                    user.displayName,
                    user.email,
                    user.photoUrl
                )
            )
        }
    }

    fun setTaskFilter(taskFilter: TaskFilter) {
        _mainViewState.value = _mainViewState.value?.copy(taskFilter = taskFilter)
    }

    fun onTaskEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.Delete -> {
                viewModelScope.launch {
                    _taskEventChannel.send(event)
                }
                fakeDeletion(event.task)
            }
            is TaskEvent.Complete -> {
                viewModelScope.launch {
                    _taskEventChannel.send(event)
                }
                fakeComplete(event.task)
            }
            is TaskEvent.RestoreComplete -> {
                restoreFakeComplete(event.task)
            }
            is TaskEvent.RestoreDelete -> {
                restoreFakeDeletion(event.task)
            }
        }
    }

    private fun restoreFakeDeletion(task: Task) {
        if (taskList.find { it.id == task.id } == null) {
            taskList.add(task)
            _mainViewState.value = _mainViewState.value?.copy(taskList = taskList)
        }
        pendingTaskDelete = null
    }

    private fun restoreFakeComplete(completeTask: Task) {
        val index = taskList.indexOf(completeTask)
        completeTask.completed = false
        taskList[index] = completeTask
        _mainViewState.value = _mainViewState.value?.copy(taskList = taskList)
        pendingTaskComplete = null
    }

    private fun fakeDeletion(task: Task) {
        pendingTaskDelete = task
        taskList.remove(task)
        _mainViewState.value = _mainViewState.value?.copy(taskList = taskList)
    }

    private fun fakeComplete(completeTask: Task) {
        val index = taskList.indexOf(completeTask)
        completeTask.completed = true
        pendingTaskComplete = completeTask
        taskList[index] = completeTask
        _mainViewState.value = _mainViewState.value?.copy(taskList = taskList)
    }

    //---------------------TASKS----------------

    suspend fun getRecyclerViewMainList(
        taskList: List<Task>,
        categoriesList: List<Category>,
        taskFilter: TaskFilter
    ): List<HomeRecyclerViewItem> {
        return model.getRecyclerViewMainList(taskList, categoriesList, taskFilter)
    }

    suspend fun getRecyclerViewMainListFilterDate(
        baseTimeInMillis: Long,
        taskList: List<Task>,
        categoryList: List<Category>
    ): List<HomeRecyclerViewItem> {
        return model.getRecyclerViewCalendarList(
            baseTimeInMillis,
            taskList,
            categoryList
        )
    }

    private fun getTasksFromRepository() =
        viewModelScope.launch(Dispatchers.IO + coroutineHandler) {
            taskUseCases.getTasks().collect { response ->
                when (response) {
                    is Response.Success -> {
                        pendingTaskDelete?.let {
                            response.data.remove(it)
                        }
                        pendingTaskComplete?.let {
                            var currentIndex: Int? = null
                            var currentTask: Task? = null
                            response.data.forEachIndexed { index, task ->
                                if (task.id == it.id && task.completed != it.completed) {
                                    currentTask = task
                                    currentIndex = index
                                    return@forEachIndexed
                                }
                            }
                            currentTask?.let {
                                response.data.remove(it)
                                it.completed = true
                                response.data.add(currentIndex!!, it)
                            }
                        }
                        taskList = response.data
                        getCategoriesFromRepository()
                    }
                    is Response.Failure -> {
                        _mainViewState.postValue(
                            _mainViewState.value?.copy(
                                error = ErrorEvent(
                                    UiText.StringResource(
                                        R.string.get_data_error
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }

    fun deleteTask(taskId: String) = viewModelScope.launch(Dispatchers.IO + coroutineHandler) {
        when (taskUseCases.deleteTask(taskId)) {
            is Response.Failure -> {
                _errorEventChannel.send(ErrorEvent(UiText.StringResource(R.string.error_removing_task)))
            }
            else -> Unit
        }
    }

    fun completeTask(task: Task) = viewModelScope.launch(Dispatchers.IO + coroutineHandler) {
        when (taskUseCases.updateTask(task)) {
            is Response.Failure -> {
                _errorEventChannel.send(ErrorEvent(UiText.StringResource(R.string.complete_task_error)))
            }
            else -> Unit
        }
    }


    //------------------CATEGORIES---------------------
    private fun getCategoriesFromRepository() =
        viewModelScope.launch(Dispatchers.IO + coroutineHandler) {
            categoryUseCases.getCategories().collect { result ->
                when (result) {
                    is Response.Success -> {
                        categoriesList = result.data
                        taskList = clearTaskWithoutCategory(taskList, categoriesList)
                        _mainViewState.postValue(
                            _mainViewState.value?.copy(
                                isComplete = true,
                                categoryList = result.data,
                                taskList = taskList
                            )
                        )
                    }
                    is Response.Failure -> {
                        _mainViewState.postValue(
                            _mainViewState.value?.copy(
                                error = ErrorEvent(
                                    UiText.StringResource(
                                        R.string.get_data_error
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }

    private fun clearTaskWithoutCategory(taskList: List<Task>, categoriesList: List<Category>) : MutableList<Task> {
        val tempList = taskList.toMutableList()
        taskList.forEach { task ->
            if(categoriesList.find { it.id == task.categoryId } == null) {
                tempList.remove(task)
            }
        }
        return tempList
    }

    fun deleteCategory(id: String) = viewModelScope.launch(Dispatchers.IO + coroutineHandler) {
        launch {
            taskUseCases.deleteTaskByCategory(id)
        }
        launch {
            when (categoryUseCases.deleteCategory(id)) {
                is Response.Failure -> {
                    _errorEventChannel.send(ErrorEvent(UiText.StringResource(R.string.delete_category_error)))
                }
                else -> Unit
            }
        }
    }

    suspend fun getCategoriesRecyclerItem(categoriesList: List<Category>): List<CategoryRecyclerViewItem> {
        return model.getCategoriesRecyclerItem(categoriesList)
    }
}
package com.example.todolistkotlin.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.business.MainActivityModel
import com.example.todolistkotlin.presentation.model.HomeRecyclerViewItem
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.use_case.task.TaskUseCases
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.utils.UiText
import com.example.todolistkotlin.presentation.states.HomeViewState
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
class HomeViewModel @Inject constructor(
    private val model: MainActivityModel,
    private val taskUseCases: TaskUseCases
) : ViewModel() {

    private var pendingTaskDelete: Task? = null
    private var pendingTaskComplete: Task? = null

    var selectedTimeCalendarFragment: Long? = null

    private val _homeViewState = MutableLiveData(HomeViewState())
    val homeViewState get() = _homeViewState

    private val _userInfoState = MutableLiveData<UserInfoState>()
    val userInfoState get() = _userInfoState

    private val _errorEventChannel = Channel<ErrorEvent>()
    val errorEvent = _errorEventChannel.receiveAsFlow()

    private val _taskEventChannel = Channel<TaskEvent>()
    val taskEvent = _taskEventChannel.receiveAsFlow()

    var taskList: MutableList<Task> = ArrayList()


    private val coroutineHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("coroutineHandler", "Exception: $throwable")
    }

    init {
        selectedTimeCalendarFragment = DateUtils.getTimeInMillisResetedTime(Calendar.getInstance())
        getUserInfo()
        getTasksFromRepository()
    }

    private fun getUserInfo() = viewModelScope.launch(Dispatchers.IO) {
        val user = Firebase.auth.currentUser
        user?.let {
            val photoUrl = try {
                user.photoUrl.toString().split("=s").first() + "=s200-c"
            } catch (e: Exception) {
                user.photoUrl.toString()
            }
            _userInfoState.postValue(
                UserInfoState(
                    user.displayName,
                    user.email,
                    photoUrl
                )
            )
        }
    }

    fun setTaskFilter(taskFilter: TaskFilter) {
        _homeViewState.value = _homeViewState.value?.copy(taskFilter = taskFilter)
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
            _homeViewState.value = _homeViewState.value?.copy(taskList = taskList)
        }
        pendingTaskDelete = null
    }

    private fun restoreFakeComplete(completeTask: Task) {
        val index = taskList.indexOf(completeTask)
        completeTask.completed = false
        taskList[index] = completeTask
        _homeViewState.value = _homeViewState.value?.copy(taskList = taskList)
        pendingTaskComplete = null
    }

    private fun fakeDeletion(task: Task) {
        pendingTaskDelete = task
        taskList.remove(task)
        _homeViewState.value = _homeViewState.value?.copy(taskList = taskList)
    }

    private fun fakeComplete(completeTask: Task) {
        val index = taskList.indexOf(completeTask)
        completeTask.completed = true
        pendingTaskComplete = completeTask
        taskList[index] = completeTask
        _homeViewState.value = _homeViewState.value?.copy(taskList = taskList)
    }

    //---------------------TASKS----------------

    suspend fun getRecyclerViewMainList(
        taskList: List<Task>,
        taskFilter: TaskFilter
    ): List<HomeRecyclerViewItem> {
        return model.getRecyclerViewMainList(taskList, taskFilter)
    }

    suspend fun getRecyclerViewMainListFilterDate(
        baseTimeInMillis: Long,
        taskList: List<Task>
    ): List<HomeRecyclerViewItem> {
        return model.getRecyclerViewCalendarList(
            baseTimeInMillis,
            taskList
        )
    }

    private fun getTasksFromRepository() =
        viewModelScope.launch(Dispatchers.IO) {
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

                        Log.d("Teste", "antes")
                        taskList = removeTaskWithNullCategory(response.data).toMutableList()
                        Log.d("Teste", "Passou")
                        updateMainViewState(
                            _homeViewState.value?.copy(
                                isFetchCompleted = true,
                                taskList = taskList
                            )
                        )
                    }
                    is Response.Failure -> {
                        updateMainViewState(
                            _homeViewState.value?.copy(
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

    fun deleteTask(taskId: String) = viewModelScope.launch(Dispatchers.IO) {
        when (taskUseCases.deleteTask(taskId)) {
            is Response.Failure -> {
                _errorEventChannel.send(ErrorEvent(UiText.StringResource(R.string.error_removing_task)))
            }
            else -> Unit
        }
    }

    fun completeTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        when (taskUseCases.updateTask(task)) {
            is Response.Failure -> {
                _errorEventChannel.send(ErrorEvent(UiText.StringResource(R.string.complete_task_error)))
            }
            else -> Unit
        }
    }

    private fun updateMainViewState(copy: HomeViewState?) =
        viewModelScope.launch(Dispatchers.Main) {
            copy?.let { _homeViewState.value = it }
        }

    private fun removeTaskWithNullCategory(taskList: List<Task>): List<Task> {
        Log.d("Teste", "entrou")
        return taskList.filter { it.category != null }
    }
}
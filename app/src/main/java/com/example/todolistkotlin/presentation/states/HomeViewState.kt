package com.example.todolistkotlin.presentation.states

import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.ui_events.ErrorEvent

data class HomeViewState(
    val isFetchCompleted: Boolean = false,
    val error: ErrorEvent? = null,
    val taskList: List<Task>? = null,
    val taskFilter: TaskFilter = TaskFilter.All()
)
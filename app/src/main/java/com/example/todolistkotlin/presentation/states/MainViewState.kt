package com.example.todolistkotlin.presentation.states

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.ui_events.ErrorEvent

data class MainViewState(
    val isComplete: Boolean,
    val error: ErrorEvent?,
    val taskList: List<Task>?,
    val categoryList: List<Category>?,
    val taskFilter: TaskFilter = TaskFilter.All
)
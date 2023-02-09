package com.example.todolistkotlin.presentation.states

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.presentation.ui_events.ErrorEvent

data class CategoryViewState(
    val isFetchCompleted: Boolean = false,
    val error: ErrorEvent? = null,
    val categoryList: List<Category>? = null,
)

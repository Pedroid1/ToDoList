package com.example.todolistkotlin.presentation.model

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task

data class TaskWithCategory(
    val task: Task,
    val category: Category
) : java.io.Serializable
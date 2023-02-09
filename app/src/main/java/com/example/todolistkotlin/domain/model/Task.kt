package com.example.todolistkotlin.domain.model

import com.example.todolistkotlin.enuns.EnumTaskPriority
import java.io.Serializable

data class Task(var id: String = "",
                var title: String = "",
                var description: String = "",
                var category: Category? = null,
                var priority: EnumTaskPriority? = null,
                var dateInMills: Long = -1L,
                var completed: Boolean = false) : Serializable

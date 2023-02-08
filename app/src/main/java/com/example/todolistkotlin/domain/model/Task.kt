package com.example.todolistkotlin.domain.model

import com.example.todolistkotlin.enuns.EnumTaskPriority

data class Task(var id: String = "",
                var title: String = "",
                var description: String = "",
                var categoryId: String = "",
                var priority: EnumTaskPriority = EnumTaskPriority.LOW,
                var dateInMills: Long = -1L,
                var completed: Boolean = false)

package com.example.todolistkotlin.presentation.ui_events

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.enuns.EnumTaskPriority

sealed class CreateTaskFormEvent {
    data class TitleChanged(val title: String) : CreateTaskFormEvent()
    data class DescriptionChanged(val description: String) : CreateTaskFormEvent()
    data class CategoryChanged(val categoryChanged: Category?) : CreateTaskFormEvent()
    data class DateChanged(val day: Int, val month: Int, val year: Int) : CreateTaskFormEvent()
    data class TimeChanged(val hour: Int, val minute: Int) : CreateTaskFormEvent()
    data class PriorityChanged(val priority: EnumTaskPriority) : CreateTaskFormEvent()
    object SubmitTask : CreateTaskFormEvent()
}

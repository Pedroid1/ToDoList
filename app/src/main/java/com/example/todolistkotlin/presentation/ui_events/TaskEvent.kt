package com.example.todolistkotlin.presentation.ui_events

import com.example.todolistkotlin.domain.model.Task

sealed class TaskEvent {
    class Complete(val task: Task) : TaskEvent()
    class Delete(val task: Task) : TaskEvent()
    class RestoreComplete(val task: Task) : TaskEvent()
    class RestoreDelete(val task: Task) : TaskEvent()
}
package com.example.todolistkotlin.domain.use_case.task

import com.example.todolistkotlin.domain.repository.TaskRepository

class DeleteTask(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) = repository.deleteTask(taskId)
}
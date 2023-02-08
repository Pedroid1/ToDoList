package com.example.todolistkotlin.domain.use_case.task

import com.example.todolistkotlin.domain.repository.TaskRepository

class DeleteTaskByCategory(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(categoryId: String) = repository.deleteTasksByCategory(categoryId)
}
package com.example.todolistkotlin.domain.use_case.task

import com.example.todolistkotlin.domain.repository.TaskRepository

class GetTasks(
    private val repository: TaskRepository
) {
    suspend operator fun invoke() = repository.getTasks()
}
package com.example.todolistkotlin.domain.use_case.task

import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.repository.TaskRepository

class UpdateTask(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) = repository.updateTask(task)
}
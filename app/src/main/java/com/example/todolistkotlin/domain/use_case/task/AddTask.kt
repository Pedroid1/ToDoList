package com.example.todolistkotlin.domain.use_case.task

import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.repository.TaskRepository
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.google.firebase.firestore.DocumentReference

class AddTask(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(title: String, description: String, categoryRef: DocumentReference, priority: EnumTaskPriority, taskDate: Long) = repository.addTask(title, description, categoryRef, priority, taskDate)
}
package com.example.todolistkotlin.domain.repository

import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow

typealias Tasks = MutableList<Task>
typealias TasksResponse = Response<Tasks>
typealias AddTaskResponse = Response<Boolean>
typealias DeleteTaskResponse = Response<Boolean>
typealias DeleteTaskByCategoryResponse = Response<Boolean>
typealias UpdateTaskResponse = Response<Boolean>

interface TaskRepository {
    suspend fun getTasks(): Flow<TasksResponse>

    suspend fun addTask(title: String, description: String, categoryRef: DocumentReference, priority: EnumTaskPriority, taskDate: Long): AddTaskResponse

    suspend fun updateTask(task: Task): UpdateTaskResponse

    suspend fun deleteTask(id: String): DeleteTaskResponse

    suspend fun deleteTasksByCategory(id: String): DeleteTaskByCategoryResponse
}
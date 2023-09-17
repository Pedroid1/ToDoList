package com.example.todolistkotlin.data.repository

import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.data.model.TaskEntity
import com.example.todolistkotlin.data.model.toTask
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.repository.AddTaskResponse
import com.example.todolistkotlin.domain.repository.DeleteTaskByCategoryResponse
import com.example.todolistkotlin.domain.repository.DeleteTaskResponse
import com.example.todolistkotlin.domain.repository.TaskRepository
import com.example.todolistkotlin.domain.repository.TasksResponse
import com.example.todolistkotlin.domain.repository.UpdateTaskResponse
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeTaskRepositoryImpl : TaskRepository {

    private val tasks = mutableListOf<Task>()

    override suspend fun getTasks(): Flow<TasksResponse> {
        return flow { emit(Response.Success(tasks)) }
    }

    override suspend fun addTask(
        title: String,
        description: String,
        categoryRef: DocumentReference,
        priority: EnumTaskPriority,
        taskDate: Long
    ): AddTaskResponse {
        val task = TaskEntity("", title, description, categoryRef, priority, taskDate)
        tasks.add(task.toTask(Category("1", "Teste", "", -1)))
        return Response.Success(true)
    }

    override suspend fun updateTask(task: Task): UpdateTaskResponse {
        tasks.find { it.id == task.id }?.let { currentTask ->
            currentTask.apply {
                id = task.id
                title = task.title
                description = task.description
                category = task.category
                priority = task.priority
                dateInMills = task.dateInMills
            }
        }
        return Response.Success(true)
    }

    override suspend fun deleteTask(id: String): DeleteTaskResponse {
        tasks.removeIf { it.id == id }
        return Response.Success(true)
    }

    override suspend fun deleteTasksByCategory(id: String): DeleteTaskByCategoryResponse {
        tasks.removeIf { it.category?.id == id }
        return Response.Success(true)
    }
}
package com.example.todolistkotlin.domain.use_case.task

import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.repository.TaskRepository
import kotlinx.coroutines.flow.map

class GetTasks(
    private val repository: TaskRepository
) {
    suspend operator fun invoke() = repository.getTasks().map { response ->
        when(response) {
            is Response.Success -> {
                Response.Success(removeNullCategoriesAndSort(response.data))
            }
            else -> response
        }
    }

    private fun removeNullCategoriesAndSort(data: MutableList<Task>): MutableList<Task> {
        return data.filter { it.category != null }.sortedBy { it.dateInMills }.toMutableList()
    }
}
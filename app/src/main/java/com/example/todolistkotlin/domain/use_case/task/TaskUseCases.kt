package com.example.todolistkotlin.domain.use_case.task

data class TaskUseCases(
    val getTasks: GetTasks,
    val addTask: AddTask,
    val deleteTask: DeleteTask,
    val deleteTaskByCategory: DeleteTaskByCategory,
    val updateTask: UpdateTask
)

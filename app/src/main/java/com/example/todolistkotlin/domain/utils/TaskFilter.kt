package com.example.todolistkotlin.domain.utils

sealed class TaskFilter {
    object All : TaskFilter()
    object Today : TaskFilter()
    object Upcoming : TaskFilter()
    object Completed : TaskFilter()
}
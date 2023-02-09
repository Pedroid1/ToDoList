package com.example.todolistkotlin.domain.utils

sealed class TaskFilter {
    class All : TaskFilter()
    class Today : TaskFilter()
    class Upcoming : TaskFilter()
    class Completed : TaskFilter()
}
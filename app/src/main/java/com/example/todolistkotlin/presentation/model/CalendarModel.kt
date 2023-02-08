package com.example.todolistkotlin.presentation.model

data class CalendarModel(
    var timeInMillis: Long? = null,
    var haveEvents: Boolean = false,
    var isSelected: Boolean = false
)
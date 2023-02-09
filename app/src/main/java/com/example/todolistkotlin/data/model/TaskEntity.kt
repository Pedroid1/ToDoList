package com.example.todolistkotlin.data.model

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.google.firebase.firestore.DocumentReference

data class TaskEntity(var id: String = "",
                      var title: String = "",
                      var description: String = "",
                      var categoryRef: DocumentReference? = null,
                      var priority: EnumTaskPriority? = null,
                      var dateInMills: Long = -1L,
                      var completed: Boolean = false)

fun TaskEntity.toTask(category: Category?) = Task(
    id, title, description, category, priority, dateInMills, completed
)
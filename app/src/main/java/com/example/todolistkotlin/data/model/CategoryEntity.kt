package com.example.todolistkotlin.data.model

import com.example.todolistkotlin.domain.model.Category

data class CategoryEntity(
    var id: String = "",
    var name: String = "",
    var categoryColor: String = " ",
    var categoryIcon: Int = -1
)

fun CategoryEntity.toCategory() = Category(
    id, name, categoryColor, categoryIcon
)

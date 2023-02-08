package com.example.todolistkotlin.domain.model

import java.io.Serializable

data class Category(
    var id: String = "",
    var name: String = "",
    var categoryColor: String = " ",
    var categoryIcon: Int = -1
) : Serializable

package com.example.todolistkotlin.domain.business

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.presentation.model.HomeRecyclerViewItem
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.repository.*
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.model.CategoryRecyclerViewItem
import kotlinx.coroutines.flow.Flow

interface MainActivityModel {

    suspend fun getCategoriesRecyclerItem(categoriesList: List<Category>): List<CategoryRecyclerViewItem>

    suspend fun getRecyclerViewMainList(
        taskList: List<Task>,
        filter: TaskFilter
    ): List<HomeRecyclerViewItem>

    suspend fun getRecyclerViewCalendarList(
        baseDate: Long,
        taskList: List<Task>
    ): List<HomeRecyclerViewItem>
}
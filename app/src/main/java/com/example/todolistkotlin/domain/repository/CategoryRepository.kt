package com.example.todolistkotlin.domain.repository

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.common.Response
import kotlinx.coroutines.flow.Flow

typealias Categories = List<Category>
typealias CategoriesResponse = Response<Categories>
typealias AddCategoryResponse = Response<Boolean>
typealias DeleteCategoryResponse = Response<Boolean>
typealias UpdateCategoryResponse = Response<Boolean>

interface CategoryRepository {

    suspend fun getCategories(): Flow<CategoriesResponse>

    suspend fun addCategory(category: Category): AddCategoryResponse

    suspend fun updateCategory(category: Category): UpdateCategoryResponse

    suspend fun removeCategory(id: String): DeleteCategoryResponse
}
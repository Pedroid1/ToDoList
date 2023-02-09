package com.example.todolistkotlin.domain.repository

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.common.Response
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow

typealias Categories = List<Category>
typealias CategoriesResponse = Response<Categories>
typealias AddCategoryResponse = Response<Category>
typealias DeleteCategoryResponse = Response<Boolean>
typealias UpdateCategoryResponse = Response<Boolean>
typealias GetCategoryRefById = Response<DocumentReference>

interface CategoryRepository {

    suspend fun getCategories(): Flow<CategoriesResponse>

    suspend fun addCategory(category: Category): AddCategoryResponse

    suspend fun updateCategory(category: Category): UpdateCategoryResponse

    suspend fun removeCategory(id: String): DeleteCategoryResponse

    suspend fun getCategoryReferenceById(categoryId: String): GetCategoryRefById
}
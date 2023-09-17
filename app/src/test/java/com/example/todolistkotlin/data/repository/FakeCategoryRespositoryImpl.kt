package com.example.todolistkotlin.data.repository

import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.repository.AddCategoryResponse
import com.example.todolistkotlin.domain.repository.CategoriesResponse
import com.example.todolistkotlin.domain.repository.CategoryRepository
import com.example.todolistkotlin.domain.repository.DeleteCategoryResponse
import com.example.todolistkotlin.domain.repository.GetCategoryRefById
import com.example.todolistkotlin.domain.repository.UpdateCategoryResponse
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.mockito.Mockito

class FakeCategoryRespositoryImpl : CategoryRepository {

    private val categories = mutableListOf<Category>()
    override suspend fun getCategories(): Flow<CategoriesResponse> = flow {
        emit(Response.Success(categories))
    }

    override suspend fun addCategory(category: Category): AddCategoryResponse {
        categories.add(category)
        return Response.Success(category)
    }

    override suspend fun updateCategory(category: Category): UpdateCategoryResponse {
        categories.find { it.id == category.id }?.apply {
            name = category.name
            categoryColor = category.categoryColor
            categoryIcon = categoryIcon
        }
        return Response.Success(true)
    }

    override suspend fun removeCategory(id: String): DeleteCategoryResponse {
        categories.removeIf { it.id == id }
        return Response.Success(true)
    }

    override suspend fun getCategoryReferenceById(categoryId: String): GetCategoryRefById {
        return Response.Success(Mockito.mock(DocumentReference::class.java))
    }


}
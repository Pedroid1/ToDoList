package com.example.todolistkotlin.data.repository

import com.example.todolistkotlin.di.CategoriesRef
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.repository.AddCategoryResponse
import com.example.todolistkotlin.domain.repository.CategoryRepository
import com.example.todolistkotlin.domain.repository.DeleteCategoryResponse
import com.example.todolistkotlin.domain.repository.UpdateCategoryResponse
import com.example.todolistkotlin.common.Constants.FIELD_NAME
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    @CategoriesRef private val categoriesRef: CollectionReference
) : CategoryRepository {

    private val categoryReferenceOrderByName = categoriesRef.orderBy(FIELD_NAME)

    override suspend fun getCategories() = callbackFlow {
        val snapshotListener = categoryReferenceOrderByName.addSnapshotListener {snapshot, e ->

            val categoriesResponse = if (snapshot != null) {
                val categories = snapshot.toObjects(Category::class.java)
                Response.Success(categories)
            } else {
                Response.Failure(e)
            }
            trySend(categoriesResponse)
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    override suspend fun addCategory(category: Category): AddCategoryResponse {
        return try {
            category.id = category.name
            categoriesRef.document(category.id).set(category).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun updateCategory(category: Category): UpdateCategoryResponse {
        return try {
            categoriesRef.document(category.id).set(category, SetOptions.merge()).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun removeCategory(id: String): DeleteCategoryResponse {
        return try {
            categoriesRef.document(id).delete().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }
}
package com.example.todolistkotlin.domain.use_case.category

import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.map

class GetCategories (
    private val categoryRepository: CategoryRepository
){
    suspend operator fun invoke() = categoryRepository.getCategories().map { response ->
        when(response) {
            is Response.Success -> {
                Response.Success(response.data.sortedBy { it.name })
            }
            else -> response
        }
    }
}
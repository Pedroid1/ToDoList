package com.example.todolistkotlin.domain.use_case.category

import com.example.todolistkotlin.domain.repository.CategoryRepository

class DeleteCategory (
    private val categoryRepository: CategoryRepository
        ){
    suspend operator fun invoke(categoryId: String) = categoryRepository.removeCategory(categoryId)
}
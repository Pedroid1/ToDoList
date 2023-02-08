package com.example.todolistkotlin.domain.use_case.category

import com.example.todolistkotlin.domain.repository.CategoryRepository

class GetCategories (
    private val categoryRepository: CategoryRepository
){
    suspend operator fun invoke() = categoryRepository.getCategories()
}
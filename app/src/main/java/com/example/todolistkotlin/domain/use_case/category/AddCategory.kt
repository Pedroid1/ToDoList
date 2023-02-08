package com.example.todolistkotlin.domain.use_case.category

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.repository.CategoryRepository

class AddCategory (
    private val categoryRepository: CategoryRepository
){
    suspend operator fun invoke(category: Category) = categoryRepository.addCategory(category)
}
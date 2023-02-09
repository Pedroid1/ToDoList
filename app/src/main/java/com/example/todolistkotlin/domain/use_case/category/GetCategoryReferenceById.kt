package com.example.todolistkotlin.domain.use_case.category

import com.example.todolistkotlin.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoryReferenceById @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: String) = repository.getCategoryReferenceById(categoryId)
}
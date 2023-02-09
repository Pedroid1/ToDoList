package com.example.todolistkotlin.domain.use_case.category

data class CategoryUseCases(
    val getCategories: GetCategories,
    val addCategory: AddCategory,
    val deleteCategory: DeleteCategory,
    val getCategoryRefById: GetCategoryReferenceById
)
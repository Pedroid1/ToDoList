package com.example.todolistkotlin.presentation.model

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.enuns.EnumCategoryRecyclerViewType
import com.example.todolistkotlin.presentation.utils.UiText

sealed class CategoryRecyclerViewItem(val type: EnumCategoryRecyclerViewType) {

    class CategoryItem(val category: Category) : CategoryRecyclerViewItem(EnumCategoryRecyclerViewType.CATEGORY)

    class Empty(val uiText: UiText) : CategoryRecyclerViewItem(EnumCategoryRecyclerViewType.EMPTY)
}
package com.example.todolistkotlin.enuns

enum class EnumCategoryRecyclerViewType() {
    CATEGORY,
    EMPTY;

    companion object {
        fun getEnumByOrdinal(index: Int) : EnumCategoryRecyclerViewType {
            return values()[index]
        }
    }
}
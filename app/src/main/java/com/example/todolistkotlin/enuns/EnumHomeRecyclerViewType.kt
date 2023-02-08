package com.example.todolistkotlin.enuns

enum class EnumHomeRecyclerViewType() {
    DATE,
    TASK,
    EMPTY;

    companion object {
        fun getEnumByOrdinal(index: Int) : EnumHomeRecyclerViewType {
            return values()[index]
        }
    }
}
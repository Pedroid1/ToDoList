package com.example.todolistkotlin.presentation.model

import com.example.todolistkotlin.enuns.EnumHomeRecyclerViewType
import com.example.todolistkotlin.presentation.UiText

sealed class HomeRecyclerViewItem(val type: EnumHomeRecyclerViewType) {

    class TaskDateItem(
        val uiText: UiText
    ) : HomeRecyclerViewItem(EnumHomeRecyclerViewType.DATE)

    class TaskItem(
        val item: TaskWithCategory
    ) : HomeRecyclerViewItem(EnumHomeRecyclerViewType.TASK)

    class Empty(val uiText: UiText) : HomeRecyclerViewItem(EnumHomeRecyclerViewType.EMPTY)
}
package com.example.todolistkotlin.presentation.ui_events

import androidx.annotation.DrawableRes

sealed class CreateCategoryFormEvent {
    data class NameChanged(val name: String) : CreateCategoryFormEvent()
    data class IconChanged(@DrawableRes val resId: Int) : CreateCategoryFormEvent()
    data class ColorChanged(val colorHex: String) : CreateCategoryFormEvent()
    object SubmitCategoryForm : CreateCategoryFormEvent()
}
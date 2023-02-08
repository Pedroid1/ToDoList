package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.use_case.validation.ValidationResult
import com.example.todolistkotlin.presentation.UiText

class ValidateCategory {
    operator fun invoke(category: Category?): ValidationResult {
        return if(category == null) {
            ValidationResult(
                successful = false,
                UiText.StringResource(R.string.select_category)
            )
        } else {
            ValidationResult(
                successful = true
            )
        }
    }
}
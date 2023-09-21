package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.use_case.validation.ValidationResult
import com.example.todolistkotlin.presentation.utils.UiText

class ValidateCategory {
    operator fun invoke(categoryId: String?): ValidationResult {
        return if(categoryId.isNullOrEmpty()) {
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
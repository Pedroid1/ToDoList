package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.use_case.validation.ValidationResult
import com.example.todolistkotlin.presentation.UiText

class ValidateTitle {
    operator fun invoke(title: String): ValidationResult {
        return if(title.isEmpty()) {
            ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.type_task_name)
            )
        } else {
            ValidationResult(
                successful = true
            )
        }
    }
}
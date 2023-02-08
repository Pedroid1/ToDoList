package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.use_case.validation.ValidationResult
import com.example.todolistkotlin.presentation.UiText

class ValidateDate {
    operator fun invoke(isDateSelected: Boolean) : ValidationResult {
        return if(!isDateSelected) {
            ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.select_date)
            )
        } else {
            ValidationResult(
                successful = true
            )
        }
    }
}
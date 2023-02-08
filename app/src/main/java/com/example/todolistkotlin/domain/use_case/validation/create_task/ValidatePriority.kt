package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.use_case.validation.ValidationResult
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.example.todolistkotlin.presentation.UiText

class ValidatePriority {
    operator fun invoke(priority: EnumTaskPriority?): ValidationResult {
        return if(priority == null) {
            ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.create_task_select_priority)
            )
        } else {
            ValidationResult(
                successful = true
            )
        }
    }
}
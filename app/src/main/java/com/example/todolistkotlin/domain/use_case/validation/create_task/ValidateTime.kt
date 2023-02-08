package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.use_case.validation.ValidationResult
import com.example.todolistkotlin.presentation.UiText
import java.util.Calendar

class ValidateTime {
    operator fun invoke(time: Long, isTimeSelected: Boolean, isTaskForToday: Boolean) : ValidationResult {
        return if(!isTimeSelected) {
            ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.select_time)
            )
        } else if(isTaskForToday && time < Calendar.getInstance().timeInMillis) {
            ValidationResult(
                successful = false,
                errorMessage = UiText.StringResource(R.string.invalid_time)
            )
        } else {
            ValidationResult(
                successful = true
            )
        }
    }
}
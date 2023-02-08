package com.example.todolistkotlin.presentation.form_validation

import com.example.todolistkotlin.domain.use_case.validation.ValidationResult

sealed class CreateTaskFormValidation {
    class TitleValidation(val result: ValidationResult) : CreateTaskFormValidation()
    class CategoryValidation(val result: ValidationResult) : CreateTaskFormValidation()
    class DateValidation(val result: ValidationResult) : CreateTaskFormValidation()
    class TimeValidation(val result: ValidationResult) : CreateTaskFormValidation()
    class PriorityValidation(val result: ValidationResult) : CreateTaskFormValidation()
}
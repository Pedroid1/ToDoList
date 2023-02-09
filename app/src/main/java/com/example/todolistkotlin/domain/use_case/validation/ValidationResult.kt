package com.example.todolistkotlin.domain.use_case.validation

import com.example.todolistkotlin.presentation.utils.UiText

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: UiText? = null
)

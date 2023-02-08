package com.example.todolistkotlin.domain.use_case.validation.create_task

data class ValidationCreateTaskUseCases(
    val validateTitle: ValidateTitle = ValidateTitle(),
    val validateCategory: ValidateCategory = ValidateCategory(),
    val validateDate: ValidateDate = ValidateDate(),
    val validateTime: ValidateTime = ValidateTime(),
    val validatePriority: ValidatePriority = ValidatePriority()
)
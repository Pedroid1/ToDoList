package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.google.common.truth.Truth.*
import org.junit.Before
import org.junit.Test

class ValidateCategoryTest {

    private lateinit var validateCategory: ValidateCategory

    @Before
    fun setUp() {
        validateCategory = ValidateCategory()
    }

    @Test
    fun `null categoryId returns false`() {
        val result = validateCategory(null)
        assertThat(result.successful).isFalse()
    }

    @Test
    fun `empty categoryId returns false`() {
        val result = validateCategory("")
        assertThat(result.successful).isFalse()
    }

    @Test
    fun `categoryId returns true`() {
        val result = validateCategory("1")
        assertThat(result.successful).isTrue()
    }
}
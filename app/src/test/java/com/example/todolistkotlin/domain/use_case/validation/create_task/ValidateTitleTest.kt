package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.google.common.truth.Truth.*
import org.junit.Before
import org.junit.Test

class ValidateTitleTest {

    private lateinit var validateTitle: ValidateTitle

    @Before
    fun setUp() {
        validateTitle = ValidateTitle()
    }

    @Test
    fun `empty categoryId returns false`() {
        val result = validateTitle("")
        assertThat(result.successful).isFalse()
    }

    @Test
    fun `title returns true`() {
        val result = validateTitle("Teste")
        assertThat(result.successful).isTrue()
    }
}
package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.google.common.truth.Truth.*
import org.junit.Before
import org.junit.Test

class ValidateDateTest {

    private lateinit var validateDate: ValidateDate

    @Before
    fun setUp() {
        validateDate = ValidateDate()
    }

    @Test
    fun `date not selected returns false`() {
        val result = validateDate(false)
        assertThat(result.successful).isFalse()
    }

    @Test
    fun `date selected returns true`() {
        val result = validateDate(true)
        assertThat(result.successful).isTrue()
    }
}
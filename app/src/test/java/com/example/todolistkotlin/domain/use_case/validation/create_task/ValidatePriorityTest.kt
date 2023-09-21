package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.google.common.truth.Truth.*
import org.junit.Before
import org.junit.Test

class ValidatePriorityTest {

    private lateinit var validatePriority: ValidatePriority

    @Before
    fun setUp() {
        validatePriority = ValidatePriority()
    }

    @Test
    fun `null priority returns false`() {
        val result = validatePriority(null)
        assertThat(result.successful).isFalse()
    }

    @Test
    fun `High priority returns true`() {
        val result = validatePriority(EnumTaskPriority.HIGH)
        assertThat(result.successful).isTrue()
    }

    @Test
    fun `medium priority returns true`() {
        val result = validatePriority(EnumTaskPriority.MEDIUM)
        assertThat(result.successful).isTrue()
    }

    @Test
    fun `low priority returns true`() {
        val result = validatePriority(EnumTaskPriority.LOW)
        assertThat(result.successful).isTrue()
    }

}
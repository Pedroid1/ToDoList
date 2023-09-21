package com.example.todolistkotlin.domain.use_case.validation.create_task

import com.google.common.truth.Truth.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class ValidateTimeTest {

    private lateinit var validateTime: ValidateTime

    @Before
    fun setUp() {
        validateTime = ValidateTime()
    }

    @Test
    fun `unselected time returns false`() {
        val result = validateTime(
            time = Calendar.getInstance().timeInMillis,
            isTimeSelected = false,
            isTaskForToday = false
        )
        assertThat(result.successful).isFalse()
    }

    @Test
    fun `task time less than current date when task is for today returns false`() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }
        val yesterday = calendar.timeInMillis
        val result = validateTime(time = yesterday, isTimeSelected = true, isTaskForToday = true)
        assertThat(result.successful).isFalse()
    }

    @Test
    fun `task time greater than current date when task is for today returns true`() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }
        val today = calendar.timeInMillis
        val result = validateTime(time = today, isTimeSelected = true, isTaskForToday = true)
        assertThat(result.successful).isTrue()
    }

    @Test
    fun `task is not for today returns true`() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val today = calendar.timeInMillis
        val result = validateTime(time = today, isTimeSelected = true, isTaskForToday = false)
        assertThat(result.successful).isTrue()
    }

}
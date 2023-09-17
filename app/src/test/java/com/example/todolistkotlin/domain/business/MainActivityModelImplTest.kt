package com.example.todolistkotlin.domain.business

import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.Calendar

class MainActivityModelImplTest {

    private lateinit var mainActivityModelImpl: MainActivityModelImpl
    private lateinit var tasks: MutableList<Task>

    @Before
    fun setUp() {
        mainActivityModelImpl = MainActivityModelImpl()

        ('a'..'z').forEachIndexed { index, c ->
            tasks.add(
                Task(index.toString(), c.toString(), c.toString(), Category(), EnumTaskPriority.HIGH, Calendar.getInstance().timeInMillis, false)
            )
        }
    }

    @Test
    fun `Gerar lista da recyclerView Main com filtro All`() = runBlocking {
        val recyclerList = mainActivityModelImpl.getRecyclerViewMainList(tasks, TaskFilter.All())


    }
}
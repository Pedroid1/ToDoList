package com.example.todolistkotlin.domain.use_case.task

import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.data.repository.FakeTaskRepositoryImpl
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.Calendar

class GetTasksTest {

    private lateinit var getTasks: GetTasks
    private lateinit var fakeRepositoryImpl: FakeTaskRepositoryImpl

    @Before
    fun setUp() {
        fakeRepositoryImpl = FakeTaskRepositoryImpl()
        getTasks = GetTasks(fakeRepositoryImpl)

        addUnorderedTasks()
    }

    private fun addUnorderedTasks() = runBlocking {
        val tasksToInsert = mutableListOf<Task>()
        ('a'..'z').forEachIndexed { index, c ->
            tasksToInsert.add(
                Task(index.toString(), c.toString(), c.toString(), Category(), EnumTaskPriority.HIGH, Calendar.getInstance().timeInMillis, false)
            )
        }
        tasksToInsert.shuffle()
        tasksToInsert.forEach {
            fakeRepositoryImpl.addTask(it.title, it.description, Mockito.mock(DocumentReference::class.java), it.priority!!, it.dateInMills)
        }
    }

    @Test
    fun `Retornar tarefas ordenadas`() = runBlocking {
        getTasks.invoke().collect { response ->
            val taskList = (response as Response.Success<MutableList<Task>>).data
            for(i in 0..taskList.size - 2) {
                assertTrue(taskList[i].dateInMills <= taskList[i + 1].dateInMills)
            }
        }
    }
}
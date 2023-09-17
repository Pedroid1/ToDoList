package com.example.todolistkotlin.domain.use_case.category

import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.data.repository.FakeCategoryRespositoryImpl
import com.example.todolistkotlin.domain.model.Category
import org.junit.Assert.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetCategoriesTest {
    private lateinit var getCategories: GetCategories
    private lateinit var fakeRepositoryImpl: FakeCategoryRespositoryImpl

    @Before
    fun setUp() {
        fakeRepositoryImpl = FakeCategoryRespositoryImpl()
        getCategories = GetCategories(fakeRepositoryImpl)

        addUnorderedCategories()
    }

    private fun addUnorderedCategories() = runBlocking {
        val categoriesToInsert = mutableListOf<Category>()
        ('a'..'z').forEachIndexed { index, c ->
            categoriesToInsert.add(Category(index.toString(), c.toString(), c.toString(), index))
        }
        categoriesToInsert.shuffle()
        categoriesToInsert.forEach {
            fakeRepositoryImpl.addCategory(it)
        }
    }

    @Test
    fun `Retornar lista de categorias ordenadas`() = runBlocking {
        getCategories.invoke().collect { response ->
            val categories = (response as Response.Success).data
            for (i in 0..categories.size - 2) {
                assertTrue(categories[i].name < categories[i + 1].name)
            }
        }
    }
}


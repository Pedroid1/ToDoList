package com.example.todolistkotlin.di

import com.example.todolistkotlin.data.repository.CategoryRepositoryImpl
import com.example.todolistkotlin.data.repository.TaskRepositoryImpl
import com.example.todolistkotlin.domain.repository.CategoryRepository
import com.example.todolistkotlin.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(repository: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(repository: TaskRepositoryImpl): TaskRepository
}
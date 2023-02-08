package com.example.todolistkotlin.di

import com.example.todolistkotlin.domain.business.MainActivityModel
import com.example.todolistkotlin.domain.business.MainActivityModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BusinessModule {
    @Binds
    @Singleton
    abstract fun bindHomeFragmentModel(model: MainActivityModelImpl): MainActivityModel

}
package com.example.todolistkotlin.di

import com.example.todolistkotlin.domain.repository.CategoryRepository
import com.example.todolistkotlin.domain.repository.TaskRepository
import com.example.todolistkotlin.common.Constants.CATEGORIES_COLLECTION
import com.example.todolistkotlin.common.Constants.TASKS_COLLECTION
import com.example.todolistkotlin.common.Constants.USERS_COLLECTION
import com.example.todolistkotlin.domain.use_case.category.*
import com.example.todolistkotlin.domain.use_case.task.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CategoriesRef

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TasksRef

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideFirebaseFireStore() = FirebaseFirestore.getInstance()

    @Provides
    @Named("uid")
    fun provideUserId() = FirebaseAuth.getInstance().currentUser!!.uid

    @Provides
    @CategoriesRef
    fun provideCategoriesRef(
        db: FirebaseFirestore,
        @Named("uid") udi: String
    ) = db.collection(USERS_COLLECTION)
        .document(udi)
        .collection(CATEGORIES_COLLECTION)

    @Provides
    @TasksRef
    fun provideTasksRef(
        db: FirebaseFirestore,
        @Named("uid") uid: String
    ) = db.collection(USERS_COLLECTION)
        .document(uid)
        .collection(TASKS_COLLECTION)

    @Provides
    @Singleton
    fun provideTaskUseCases(
        repo: TaskRepository
    ) = TaskUseCases(
        getTasks = GetTasks(repo),
        addTask = AddTask(repo),
        deleteTask = DeleteTask(repo),
        deleteTaskByCategory = DeleteTaskByCategory(repo),
        updateTask = UpdateTask(repo)
    )

    @Provides
    @Singleton
    fun provideCategoryUseCases(
        repo: CategoryRepository
    ) = CategoryUseCases(
        getCategories = GetCategories(repo),
        addCategory = AddCategory(repo),
        deleteCategory = DeleteCategory(repo),
        getCategoryRefById = GetCategoryReferenceById(repo)
    )

}
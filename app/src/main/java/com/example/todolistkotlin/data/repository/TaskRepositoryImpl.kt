package com.example.todolistkotlin.data.repository

import com.example.todolistkotlin.common.Constants
import com.example.todolistkotlin.di.TasksRef
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.common.Constants.FIELD_DATE
import com.example.todolistkotlin.data.model.TaskEntity
import com.example.todolistkotlin.data.model.toTask
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.repository.*
import com.example.todolistkotlin.enuns.EnumTaskPriority
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    @TasksRef private val tasksRef: CollectionReference
) : TaskRepository {

    private val taskReferenceOrderByData = tasksRef.orderBy(FIELD_DATE)

    override suspend fun getTasks() = callbackFlow {
        val snapshotListener = taskReferenceOrderByData.addSnapshotListener { snapshot, e ->
            val tasksResponse = if (snapshot != null) {
                val tasks = snapshot.toObjects(TaskEntity::class.java)
                val tasksWithCategoryList = mutableListOf<Task>()

                runBlocking {
                    tasks.forEach { taskEntity ->
                        launch(Dispatchers.IO) {
                            val category = async {
                                if(taskEntity.categoryRef != null) taskEntity.categoryRef!!.get().await()
                                    .toObject(Category::class.java)
                                else null
                            }
                            tasksWithCategoryList.add(
                                taskEntity.toTask(category.await())
                            )
                        }
                    }
                }

                Response.Success(tasksWithCategoryList)
            } else {
                Response.Failure(e)
            }
            trySend(tasksResponse)
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    override suspend fun addTask(title: String, description: String, categoryRef: DocumentReference, priority: EnumTaskPriority, taskDate: Long): AddTaskResponse {
        return try {
            val id = tasksRef.document().id
            val task = TaskEntity(
                id,
                title,
                description,
                categoryRef,
                priority,
                taskDate
            )
            tasksRef.document(id).set(task).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun updateTask(task: Task): UpdateTaskResponse {
        return try {
            tasksRef.document(task.id).set(task, SetOptions.merge()).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun deleteTask(id: String): DeleteTaskResponse {
        return try {
            tasksRef.document(id).delete().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun deleteTasksByCategory(id: String): DeleteTaskByCategoryResponse {
        return try {
            val query = tasksRef.whereEqualTo(Constants.FIELD_CATEGORY_ID, id).get().await()
            val batch = FirebaseFirestore.getInstance().batch()
            val taskList = query.documents
            for (task in taskList) {
                batch.delete(task.reference)
            }
            batch.commit().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    /*
    override suspend fun deleteTasksByCategory(id: String): Boolean {
        return suspendCoroutine { continuation ->
            tasksRef.whereEqualTo(Constants.FIELD_CATEGORY_ID, id)
                .get()
                .addOnSuccessListener { query ->
                    val batch = FirebaseFirestore.getInstance().batch()

                    val taskList = query.documents
                    for (task in taskList) {
                        batch.delete(task.reference)
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            continuation.resumeWith(Result.success(true))
                        }
                        .addOnFailureListener {
                            continuation.resumeWith(Result.failure(it))
                        }
                }
                .addOnFailureListener {
                    continuation.resumeWith(Result.failure(it))
                }
        }
    }
     */
}
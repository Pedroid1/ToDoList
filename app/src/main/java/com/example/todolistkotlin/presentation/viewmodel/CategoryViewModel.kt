package com.example.todolistkotlin.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistkotlin.R
import com.example.todolistkotlin.common.Response
import com.example.todolistkotlin.domain.business.MainActivityModel
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.use_case.category.CategoryUseCases
import com.example.todolistkotlin.domain.use_case.task.TaskUseCases
import com.example.todolistkotlin.presentation.utils.UiText
import com.example.todolistkotlin.presentation.model.CategoryRecyclerViewItem
import com.example.todolistkotlin.presentation.states.CategoryViewState
import com.example.todolistkotlin.presentation.ui_events.ErrorEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val model: MainActivityModel,
    private val categoryUseCases: CategoryUseCases,
    private val taskUseCases: TaskUseCases
) : ViewModel() {

    private val _categoryViewState = MutableLiveData(CategoryViewState())
    val categoryViewState get() = _categoryViewState

    private val _errorEventChannel = Channel<ErrorEvent>()
    val errorEvent = _errorEventChannel.receiveAsFlow()

    var categoriesList: List<Category> = ArrayList()

    init {
        getCategoriesFromRepository()
    }

    private fun getCategoriesFromRepository() =
        viewModelScope.launch(Dispatchers.IO) {
            categoryUseCases.getCategories().collect { result ->
                when (result) {
                    is Response.Success -> {
                        categoriesList = result.data
                        updateCategoryViewState(
                            _categoryViewState.value?.copy(
                                isFetchCompleted = true,
                                categoryList = categoriesList
                            )
                        )
                    }
                    is Response.Failure -> {
                        updateCategoryViewState(
                            _categoryViewState.value?.copy(
                                error = ErrorEvent(
                                    UiText.StringResource(
                                        R.string.get_data_error
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }

    fun deleteCategory(id: String) = viewModelScope.launch(Dispatchers.IO) {
        launch {
            taskUseCases.deleteTaskByCategory(id)
        }
        launch {
            when (categoryUseCases.deleteCategory(id)) {
                is Response.Failure -> {
                    _errorEventChannel.send(ErrorEvent(UiText.StringResource(R.string.delete_category_error)))
                }
                else -> Unit
            }
        }
    }

    suspend fun getCategoriesRecyclerItem(categoriesList: List<Category>): List<CategoryRecyclerViewItem> {
        return model.getCategoriesRecyclerItem(categoriesList)
    }

    private fun updateCategoryViewState(state: CategoryViewState?) = viewModelScope.launch(Dispatchers.Main) {
        state?.let { _categoryViewState.value = it }
    }
}
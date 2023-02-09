package com.example.todolistkotlin.presentation.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.domain.repository.AddCategoryResponse
import com.example.todolistkotlin.domain.use_case.category.CategoryUseCases
import com.example.todolistkotlin.presentation.ui_events.CreateCategoryFormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCategoryViewModel @Inject constructor(
    private val categoryUseCases: CategoryUseCases
) : ViewModel() {

    private val _addCategoryResponse = MutableLiveData<AddCategoryResponse>()
    val addCategoryResponse: LiveData<AddCategoryResponse> get() = _addCategoryResponse

    var createCategoryButtonClickable = ObservableBoolean(true)

    private val _categoryColorItem: MutableLiveData<String> = MutableLiveData()
    val categoryColorItem: LiveData<String>
        get() = _categoryColorItem

    private val _categoryIconItem: MutableLiveData<Int> = MutableLiveData()
    val categoryIconItem: LiveData<Int>
        get() = _categoryIconItem

    private var name: String = ""
    private var iconResId: Int? = null
    private var colorHex: String = ""

    fun onCategoryEvent(event: CreateCategoryFormEvent) {
        when (event) {
            is CreateCategoryFormEvent.NameChanged -> {
                name = event.name
            }
            is CreateCategoryFormEvent.IconChanged -> {
                iconResId = event.resId
                _categoryIconItem.postValue(event.resId)
            }
            is CreateCategoryFormEvent.ColorChanged -> {
                colorHex = event.colorHex
                _categoryColorItem.postValue(event.colorHex)
            }
            is CreateCategoryFormEvent.SubmitCategoryForm -> {
                createCategoryButtonClickable.set(false)
                addCategory()
            }
        }
    }

    private fun addCategory() = viewModelScope.launch(Dispatchers.IO) {
        val category = Category("", name, colorHex, iconResId!!)
        _addCategoryResponse.postValue(categoryUseCases.addCategory(category))
    }
}
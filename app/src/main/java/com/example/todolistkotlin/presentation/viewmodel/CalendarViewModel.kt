package com.example.todolistkotlin.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class CalendarViewModel : ViewModel(){
    private val _filterDate = MutableLiveData<Long>()
    val filterDate: LiveData<Long>
        get() = _filterDate

    fun setFilterDateInMillis(timeInMillis: Long) {
        _filterDate.value = timeInMillis
    }

    init {
        _filterDate.value = Calendar.getInstance().timeInMillis
    }
}
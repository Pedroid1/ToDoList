package com.example.todolistkotlin.presentation.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.todolistkotlin.R
import com.example.todolistkotlin.presentation.ui_events.CreateTaskFormEvent
import com.example.todolistkotlin.presentation.viewmodel.CreateTaskViewModel
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var createTaskViewModel: CreateTaskViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        createTaskViewModel = ViewModelProvider(requireActivity())[CreateTaskFragment.CREATE_TASK_VIEW_MODEL_KEY, CreateTaskViewModel::class.java]
        val calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)

        datePickerDialog = DatePickerDialog(requireActivity(), this, year, month, day)
        datePickerDialog.setTitle(getString(R.string.select_date))
        datePickerDialog.setCancelable(true)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        createTaskViewModel.onEvent(CreateTaskFormEvent.DateChanged(dayOfMonth, month, year))
    }
}
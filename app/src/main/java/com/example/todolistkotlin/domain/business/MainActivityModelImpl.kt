package com.example.todolistkotlin.domain.business

import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.presentation.model.HomeRecyclerViewItem
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.utils.UiText
import com.example.todolistkotlin.presentation.model.CategoryRecyclerViewItem
import com.example.todolistkotlin.util.DateUtils
import com.example.todolistkotlin.util.DateUtils.Companion.getTimeInMillisResetedTime
import com.example.todolistkotlin.util.DateUtils.Companion.isTimeInMillsSameDay
import java.util.*
import javax.inject.Inject

class MainActivityModelImpl @Inject constructor() : MainActivityModel {

    private val emptyMessageCategory = UiText.StringResource(R.string.no_categories)
    private val emptyMessageTask = UiText.StringResource(R.string.no_tasks)
    private val emptyMessageFilterDateTask = UiText.StringResource(R.string.no_selected_date_task)
    private val delayedTaskMessage = UiText.StringResource(R.string.delayed_tasks)
    private val todayTaskMessage = UiText.StringResource(R.string.today_tasks)
    private val tomorrowTaskMessage = UiText.StringResource(R.string.tomorrow_tasks)

    //------------------TASKS------------------------------

    override suspend fun getRecyclerViewMainList(
        taskList: List<Task>,
        filter: TaskFilter
    ): List<HomeRecyclerViewItem> {
        val calendar = Calendar.getInstance()
        DateUtils.resetTimeInCalendar(calendar)
        val today = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrow = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val otherDay = calendar.timeInMillis

        val filteredList = when (filter) {
            is TaskFilter.All -> taskList.filter { !it.completed }
            is TaskFilter.Today -> getTodayTasks(taskList)
            is TaskFilter.Upcoming -> getFutureTasks(taskList)
            is TaskFilter.Completed -> getCompletedTasks(taskList)
        }

        val recyclerList: MutableList<HomeRecyclerViewItem> = mutableListOf()
        if (filteredList.isNotEmpty()) {
            when (filter) {
                is TaskFilter.Completed -> recyclerList.addAll(generateTasksWithDates(filteredList))
                else -> {
                    filteredList.groupBy {
                        when {
                            it.dateInMills < today -> TaskGroup.OVERDUE
                            it.dateInMills < tomorrow -> TaskGroup.TODAY
                            it.dateInMills < otherDay -> TaskGroup.TOMORROW
                            else -> TaskGroup.OTHER_DATE
                        }
                    }.forEach { (group, list) ->
                        when (group) {
                            TaskGroup.OVERDUE -> {
                                recyclerList.add(
                                    HomeRecyclerViewItem.TaskDateItem(
                                        delayedTaskMessage
                                    )
                                )
                                recyclerList.addAll(list.map { task ->
                                    HomeRecyclerViewItem.TaskItem(task)
                                })
                            }
                            TaskGroup.TODAY -> {
                                recyclerList.add(HomeRecyclerViewItem.TaskDateItem(todayTaskMessage))
                                recyclerList.addAll(list.map { task ->
                                    HomeRecyclerViewItem.TaskItem(task)
                                })
                            }
                            TaskGroup.TOMORROW -> {
                                recyclerList.add(
                                    HomeRecyclerViewItem.TaskDateItem(
                                        tomorrowTaskMessage
                                    )
                                )
                                recyclerList.addAll(list.map { task ->
                                    HomeRecyclerViewItem.TaskItem(task)
                                })
                            }
                            TaskGroup.OTHER_DATE -> recyclerList.addAll(generateTasksWithDates(list))
                        }
                    }
                }
            }
        } else {
            recyclerList.add(HomeRecyclerViewItem.Empty(emptyMessageTask))
        }
        return recyclerList
    }

    private fun generateTasksWithDates(list: List<Task>): List<HomeRecyclerViewItem> {
        val recyclerList: MutableList<HomeRecyclerViewItem> = mutableListOf()
        var lastDate = DateUtils.getCompleteDateWithoutYear(list.first().dateInMills)
        var lastYear = DateUtils.longIntoYear(list.first().dateInMills)
        recyclerList.add(HomeRecyclerViewItem.TaskDateItem(UiText.DynamicString(lastDate)))

        list.forEach { task ->
            val currentDate = DateUtils.getCompleteDateWithoutYear(task.dateInMills)
            if (currentDate != lastDate) {
                lastDate = currentDate
                val currentYear = DateUtils.longIntoYear(task.dateInMills)
                if (lastYear != currentYear) {
                    lastYear = currentYear
                    recyclerList.add(
                        HomeRecyclerViewItem.TaskDateItem(
                            UiText.DynamicString(
                                DateUtils.getCompleteDate(task.dateInMills)
                            )
                        )
                    )
                } else {
                    recyclerList.add(
                        HomeRecyclerViewItem.TaskDateItem(
                            UiText.DynamicString(
                                currentDate
                            )
                        )
                    )
                }
            }
            recyclerList.add(HomeRecyclerViewItem.TaskItem(task))
        }
        return recyclerList
    }

    private fun getTodayTasks(taskList: List<Task>): List<Task> {
        return taskList.filter {
            isTimeInMillsSameDay(
                Calendar.getInstance().timeInMillis,
                it.dateInMills
            ) && !it.completed
        }
    }

    private fun getCompletedTasks(taskList: List<Task>): List<Task> {
        return taskList.filter { it.completed }
    }

    private fun getFutureTasks(taskList: List<Task>): List<Task> {
        return taskList.filter {
            !isTaskDelayed(it.dateInMills) && !isTimeInMillsSameDay(
                Calendar.getInstance().timeInMillis,
                it.dateInMills
            ) && !it.completed
        }
    }

    override suspend fun getRecyclerViewCalendarList(
        baseDate: Long,
        taskList: List<Task>
    ): List<HomeRecyclerViewItem> {
        val recyclerList: MutableList<HomeRecyclerViewItem> = mutableListOf()
        taskList.filter {
            DateUtils.longIntoDate(baseDate) == DateUtils.longIntoDate(it.dateInMills) && !it.completed
        }.let { list ->
            if(list.isNotEmpty()) {
                recyclerList.addAll(generateTasksWithDates(list))
            } else {
                recyclerList.add(HomeRecyclerViewItem.Empty(emptyMessageTask))
            }
        }
        return recyclerList
    }

    private fun isTaskDelayed(taskDateInMillis: Long): Boolean {
        val taskDate = Calendar.getInstance()
        taskDate.timeInMillis = taskDateInMillis
        val currentDate = Calendar.getInstance()
        getTimeInMillisResetedTime(taskDate)
        getTimeInMillisResetedTime(currentDate)
        return taskDate.before(currentDate)
    }

    override suspend fun getCategoriesRecyclerItem(categoriesList: List<Category>): List<CategoryRecyclerViewItem> {
        val recyclerItemList = mutableListOf<CategoryRecyclerViewItem>()
        if (categoriesList.isEmpty()) {
            recyclerItemList.add(CategoryRecyclerViewItem.Empty(emptyMessageCategory))
        } else {
            recyclerItemList.addAll(categoriesList.map { category ->
                CategoryRecyclerViewItem.CategoryItem(category)
            })
        }
        return recyclerItemList
    }
}

enum class TaskGroup {
    OVERDUE,
    TODAY,
    TOMORROW,
    OTHER_DATE
}
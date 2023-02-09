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
import kotlin.collections.ArrayList

class MainActivityModelImpl @Inject constructor() : MainActivityModel {

    private val emptyMessageCategory: UiText
    private val emptyMessageTask: UiText
    private val emptyMessageFilterDateTask: UiText
    private val delayedTaskMessage: UiText
    private val todayTaskMessage: UiText
    private val tomorrowTaskMessage: UiText

    init {
        emptyMessageCategory = UiText.StringResource(R.string.no_categories)
        emptyMessageTask = UiText.StringResource(R.string.no_tasks)
        emptyMessageFilterDateTask = UiText.StringResource(R.string.no_selected_date_task)
        delayedTaskMessage = UiText.StringResource(R.string.delayed_tasks)
        todayTaskMessage = UiText.StringResource(R.string.today_tasks)
        tomorrowTaskMessage = UiText.StringResource(R.string.tomorrow_tasks)
    }

    //------------------TASKS------------------------------

    private fun addTaskItem(
        task: Task,
        recyclerList: MutableList<HomeRecyclerViewItem>) {
        if (task.category != null) {
            recyclerList.add(HomeRecyclerViewItem.TaskItem(task))
        } else {
            val item = recyclerList.lastOrNull()
            if (item != null && item is HomeRecyclerViewItem.TaskDateItem) {
                recyclerList.removeLast()
            }
        }
    }

    override suspend fun getRecyclerViewMainList(
        taskList: List<Task>,
        filter: TaskFilter
    ): List<HomeRecyclerViewItem> {
        val recyclerList: MutableList<HomeRecyclerViewItem> = mutableListOf()

        var filterList = when (filter) {
            is TaskFilter.All -> {
                getAllTasks(taskList)
            }
            is TaskFilter.Today -> {
                getTodayTasks(taskList)
            }
            is TaskFilter.Upcoming -> {
                getFutureTasks(taskList)
            }
            is TaskFilter.Completed -> {
                getCompletedTasks(taskList)
            }
        }
        filterList = filterList.sortedBy { it.dateInMills }

        if (filterList.isNotEmpty()) {
            when (filter) {
                is TaskFilter.Completed -> {
                    recyclerList.addAll(createRestOfItems(filterList))
                }
                else -> {
                    val delayedTasks =
                        filterList.filter { isTaskDelayed(it.dateInMills) }.toMutableList()
                    recyclerList.addAll(createDelayedItems(delayedTasks))

                    val todayTasks = filterList.filter {
                        isTimeInMillsSameDay(
                            Calendar.getInstance().timeInMillis,
                            it.dateInMills
                        )
                    }
                    recyclerList.addAll(createTodayItems(todayTasks))

                    val tomorrowTasks = filterList.filter { isTaskForTomorrow(it.dateInMills) }
                    recyclerList.addAll(createTomorrowItems(tomorrowTasks))

                    val restOfTasks = filterList.toMutableList()
                    restOfTasks.removeAll(delayedTasks)
                    restOfTasks.removeAll(todayTasks)
                    restOfTasks.removeAll(tomorrowTasks)
                    recyclerList.addAll(createRestOfItems(restOfTasks))
                }
            }
        }
        if (recyclerList.isEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.Empty(emptyMessageTask))
        }
        return recyclerList
    }

    private fun createDelayedItems(
        taskList: List<Task>
    ): List<HomeRecyclerViewItem> {
        val recyclerList = ArrayList<HomeRecyclerViewItem>()
        if (taskList.isNotEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.TaskDateItem(delayedTaskMessage))
            taskList.forEach { task ->
                addTaskItem(task, recyclerList)
            }
        }
        return recyclerList
    }

    private fun createTodayItems(
        taskList: List<Task>
    ): List<HomeRecyclerViewItem> {
        val recyclerList = ArrayList<HomeRecyclerViewItem>()
        if (taskList.isNotEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.TaskDateItem(todayTaskMessage))
            taskList.forEach { task ->
                addTaskItem(task, recyclerList)
            }
        }
        return recyclerList
    }

    private fun createTomorrowItems(
        taskList: List<Task>
    ): List<HomeRecyclerViewItem> {
        val recyclerList = ArrayList<HomeRecyclerViewItem>()
        if (taskList.isNotEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.TaskDateItem(tomorrowTaskMessage))
            taskList.forEach { task ->
                addTaskItem(task, recyclerList)
            }
        }
        return recyclerList
    }

    private fun createRestOfItems(
        taskList: List<Task>
    ): List<HomeRecyclerViewItem> {
        val actualYear = DateUtils.longIntoYear(Calendar.getInstance().timeInMillis)
        val recyclerList = ArrayList<HomeRecyclerViewItem>()
        if (taskList.isNotEmpty()) {
            var currentDate = DateUtils.longIntoDate(taskList[0].dateInMills)
            if (actualYear != DateUtils.longIntoYear(taskList[0].dateInMills)) {
                recyclerList.add(newDateItemWithYear(taskList[0].dateInMills))
            } else {
                recyclerList.add(newDateItemWithoutYear(taskList[0].dateInMills))
            }
            taskList.forEach { task ->
                val currentTaskDate = DateUtils.longIntoDate(task.dateInMills)
                if (currentDate != currentTaskDate) {
                    if (actualYear != DateUtils.longIntoYear(task.dateInMills)) {
                        recyclerList.add(newDateItemWithYear(task.dateInMills))
                    } else {
                        recyclerList.add(newDateItemWithoutYear(task.dateInMills))
                    }
                    currentDate = currentTaskDate
                }
                addTaskItem(task, recyclerList)
            }
        }
        return recyclerList
    }

    private fun getAllTasks(taskList: List<Task>): List<Task> {
        return taskList.filter { !it.completed }
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

    private fun isTaskForTomorrow(dateInMills: Long): Boolean {
        val tomorrowDate = Calendar.getInstance()
        tomorrowDate.add(Calendar.DAY_OF_MONTH, 1)
        return DateUtils.longIntoDate(tomorrowDate.timeInMillis) == DateUtils.longIntoDate(
            dateInMills
        )
    }

    override suspend fun getRecyclerViewCalendarList(
        baseDate: Long,
        taskList: List<Task>
    ): List<HomeRecyclerViewItem> {
        val recyclerList: MutableList<HomeRecyclerViewItem> = mutableListOf()
        val tasksFilter = taskList.filter {
            DateUtils.longIntoDate(baseDate) == DateUtils.longIntoDate(it.dateInMills) && !it.completed
        }
        recyclerList.addAll(createRestOfItems(tasksFilter))
        if (recyclerList.isEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.Empty(emptyMessageTask))
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

    private fun newDateItemWithoutYear(date: Long): HomeRecyclerViewItem.TaskDateItem {
        return HomeRecyclerViewItem.TaskDateItem(
            UiText.DynamicString(DateUtils.getCompleteDateWithoutYear(date))
        )
    }

    private fun newDateItemWithYear(date: Long): HomeRecyclerViewItem.TaskDateItem {
        return HomeRecyclerViewItem.TaskDateItem(
            UiText.DynamicString(
                DateUtils.getCompleteDate(date)
            )
        )
    }

    override suspend fun getCategoriesRecyclerItem(categoriesList: List<Category>): List<CategoryRecyclerViewItem> {
        val recyclerItemList = mutableListOf<CategoryRecyclerViewItem>()
        if (categoriesList.isEmpty()) {
            recyclerItemList.add(CategoryRecyclerViewItem.Empty(emptyMessageCategory))
        } else {
            categoriesList.forEach {
                recyclerItemList.add(CategoryRecyclerViewItem.CategoryItem(it))
            }
        }
        return recyclerItemList
    }
}
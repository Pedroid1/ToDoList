package com.example.todolistkotlin.domain.business

import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.model.Category
import com.example.todolistkotlin.presentation.model.HomeRecyclerViewItem
import com.example.todolistkotlin.domain.model.Task
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.UiText
import com.example.todolistkotlin.presentation.model.CategoryRecyclerViewItem
import com.example.todolistkotlin.presentation.model.TaskWithCategory
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

    private fun joinTaskAndCategory(
        task: Task,
        category: Category?,
        recyclerList: MutableList<HomeRecyclerViewItem>
    ) {
        if (category != null) {
            recyclerList.add(newTaskItem(task, category))
        } else {
            val item = recyclerList.lastOrNull()
            if (item != null && item is HomeRecyclerViewItem.TaskDateItem) {
                recyclerList.removeLast()
            }
        }
    }

    override suspend fun getRecyclerViewMainList(
        taskList: List<Task>,
        categoriesList: List<Category>,
        filter: TaskFilter
    ): List<HomeRecyclerViewItem> {
        val recyclerList: MutableList<HomeRecyclerViewItem> = mutableListOf()

        val filterList = when (filter) {
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
        if (filterList.isNotEmpty()) {
            when (filter) {
                is TaskFilter.Completed -> {
                    recyclerList.addAll(createRestOfItems(filterList, categoriesList))
                }
                else -> {
                    val delayedTasks =
                        filterList.filter { isTaskDelayed(it.dateInMills) }.toMutableList()
                    recyclerList.addAll(createDelayedItems(delayedTasks, categoriesList))

                    val todayTasks = filterList.filter {
                        isTimeInMillsSameDay(
                            Calendar.getInstance().timeInMillis,
                            it.dateInMills
                        )
                    }
                    recyclerList.addAll(createTodayItems(todayTasks, categoriesList))

                    val tomorrowTasks = filterList.filter { isTaskForTomorrow(it.dateInMills) }
                    recyclerList.addAll(createTomorrowItems(tomorrowTasks, categoriesList))

                    val restOfTasks = filterList.toMutableList()
                    restOfTasks.removeAll(delayedTasks)
                    restOfTasks.removeAll(todayTasks)
                    restOfTasks.removeAll(tomorrowTasks)
                    recyclerList.addAll(createRestOfItems(restOfTasks, categoriesList))
                }
            }
        }
        if (recyclerList.isEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.Empty(emptyMessageTask))
        }
        return recyclerList
    }

    private fun createDelayedItems(
        taskList: List<Task>,
        categoriesList: List<Category>
    ): List<HomeRecyclerViewItem> {
        val recyclerList = ArrayList<HomeRecyclerViewItem>()
        if (taskList.isNotEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.TaskDateItem(delayedTaskMessage))
            taskList.forEach { task ->
                val category = findCategoryWithId(task.categoryId, categoriesList)
                joinTaskAndCategory(task, category, recyclerList)
            }
        }
        return recyclerList
    }

    private fun createTodayItems(
        taskList: List<Task>,
        categoriesList: List<Category>
    ): List<HomeRecyclerViewItem> {
        val recyclerList = ArrayList<HomeRecyclerViewItem>()
        if (taskList.isNotEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.TaskDateItem(todayTaskMessage))
            taskList.forEach { task ->
                val category = findCategoryWithId(task.categoryId, categoriesList)
                joinTaskAndCategory(task, category, recyclerList)
            }
        }
        return recyclerList
    }

    private fun createTomorrowItems(
        taskList: List<Task>,
        categoriesList: List<Category>
    ): List<HomeRecyclerViewItem> {
        val recyclerList = ArrayList<HomeRecyclerViewItem>()
        if (taskList.isNotEmpty()) {
            recyclerList.add(HomeRecyclerViewItem.TaskDateItem(tomorrowTaskMessage))
            taskList.forEach { task ->
                val category = findCategoryWithId(task.categoryId, categoriesList)
                joinTaskAndCategory(task, category, recyclerList)
            }
        }
        return recyclerList
    }

    private fun createRestOfItems(
        taskList: List<Task>,
        categoriesList: List<Category>
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
                val category = findCategoryWithId(task.categoryId, categoriesList)
                joinTaskAndCategory(task, category, recyclerList)
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

    private fun findCategoryWithId(id: String, categoriesList: List<Category>): Category? {
        return categoriesList.find { it.id == id }
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
        taskList: List<Task>,
        categoriesList: List<Category>
    ): List<HomeRecyclerViewItem> {
        val recyclerList: MutableList<HomeRecyclerViewItem> = mutableListOf()
        val tasksFilter = taskList.filter {
            DateUtils.longIntoDate(baseDate) == DateUtils.longIntoDate(it.dateInMills) && !it.completed
        }
        recyclerList.addAll(createRestOfItems(tasksFilter, categoriesList))
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

    private fun newTaskItem(task: Task, category: Category): HomeRecyclerViewItem.TaskItem {
        return HomeRecyclerViewItem.TaskItem(TaskWithCategory(task, category))
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
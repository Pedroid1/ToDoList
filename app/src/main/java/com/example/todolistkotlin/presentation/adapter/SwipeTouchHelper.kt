package com.example.todolistkotlin.presentation.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistkotlin.R
import com.example.todolistkotlin.domain.utils.TaskFilter
import com.example.todolistkotlin.presentation.model.HomeRecyclerViewItem
import com.example.todolistkotlin.enuns.EnumHomeRecyclerViewType
import com.example.todolistkotlin.presentation.adapter.recycler.HomeRecyclerAdapter
import com.example.todolistkotlin.presentation.ui_events.TaskEvent

class SwipeTouchHelper(
    dragDirs: Int,
    swipeDirs: Int,
    private var adapter: HomeRecyclerAdapter,
    private val onEventListener: OnTaskEvent
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    private var taskFilter: TaskFilter = TaskFilter.All()

    fun setTaskFilter(taskFilter: TaskFilter) {
        this.taskFilter = taskFilter
    }

    interface OnTaskEvent {
        fun onTaskEvent(taskEvent: TaskEvent)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (isTaskViewType(viewHolder.itemViewType)) {
            when (taskFilter) {
                is TaskFilter.Completed -> {
                    val swipeFlags = ItemTouchHelper.LEFT
                    makeMovementFlags(0, swipeFlags)
                }
                else -> {
                    val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    makeMovementFlags(0, swipeFlags)
                }
            }
        } else {
            makeMovementFlags(0, 0)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    private fun isTaskViewType(itemViewType: Int): Boolean {
        return EnumHomeRecyclerViewType.getEnumByOrdinal(itemViewType) == EnumHomeRecyclerViewType.TASK
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val recyclerItem = adapter.currentList[position] as HomeRecyclerViewItem.TaskItem

        when (direction) {
            ItemTouchHelper.LEFT -> {
                onEventListener.onTaskEvent(TaskEvent.Delete(recyclerItem.task))
            }
            ItemTouchHelper.RIGHT -> {
                onEventListener.onTaskEvent(TaskEvent.Complete(recyclerItem.task))
            }
            else -> Unit
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val corners = 24f
        val itemView = viewHolder.itemView
        val context = itemView.context

        val backgroundPaint = Paint()
        val background = RectF(
            itemView.left.toFloat(),
            itemView.top.toFloat(),
            itemView.right.toFloat(),
            itemView.bottom.toFloat()
        )

        if (dX > 0) {
            backgroundPaint.color = ActivityCompat.getColor(context, R.color.green_50_transparency)
            c.drawRoundRect(background, corners, corners, backgroundPaint)
            drawText(
                context.getString(R.string.touch_helper_complete_item),
                c,
                background,
                itemView,
                false
            )
        } else {
            backgroundPaint.color = ActivityCompat.getColor(context, R.color.red_20_transparency)
            c.drawRoundRect(background, corners, corners, backgroundPaint)
            drawText(
                context.getString(R.string.touch_helper_remove_item),
                c,
                background,
                itemView,
                true
            )
        }
    }

    private fun drawText(
        text: String,
        c: Canvas,
        background: RectF,
        itemView: View,
        isRemove: Boolean
    ) {
        val padding = 30
        val textSize = 45f

        val textPaint = TextPaint()
        textPaint.color = Color.WHITE
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize

        val typeface = ResourcesCompat.getFont(itemView.context, R.font.poppins_medium)
        textPaint.typeface = typeface

        val textWidth = textPaint.measureText(text)
        if (isRemove) c.drawText(
            text,
            itemView.right - textWidth - padding,
            background.centerY() + textSize / 2,
            textPaint
        ) else c.drawText(
            text,
            (itemView.left + padding).toFloat(),
            background.centerY() + textSize / 2,
            textPaint
        )
    }
}
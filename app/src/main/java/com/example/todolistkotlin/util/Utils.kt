package com.example.todolistkotlin.util

import android.app.Activity
import android.content.Context
import com.example.todolistkotlin.R
import com.example.todolistkotlin.common.Constants.NIGHT_KEY
import com.example.todolistkotlin.presentation.model.ColorItem

class Utils {

    companion object {
        fun getCategoryColorsList(context: Context): List<ColorItem> {
            return listOf(
                ColorItem(context.getString(R.string.category_color_1)),
                ColorItem(context.getString(R.string.category_color_2)),
                ColorItem(context.getString(R.string.category_color_3)),
                ColorItem(context.getString(R.string.category_color_4)),
                ColorItem(context.getString(R.string.category_color_5)),
                ColorItem(context.getString(R.string.category_color_6)),
                ColorItem(context.getString(R.string.category_color_7)),
                ColorItem(context.getString(R.string.category_color_8))
            )
        }

        fun getCategoryIcons(): List<Int> {
            return listOf(
                R.drawable.ic_shop,
                R.drawable.ic_edit,
                R.drawable.ic_psychology,
                R.drawable.ic_self_improvement,
                R.drawable.ic_directions_car,
                R.drawable.ic_directions_run,
                R.drawable.ic_sports_esports,
                R.drawable.ic_flight,
                R.drawable.ic_fitness_center,
                R.drawable.ic_sports_soccer,
                R.drawable.ic_motorcycle,
                R.drawable.ic_sports_tennis,
                R.drawable.ic_shopping_basket,
                R.drawable.ic_sports_motorsports,
                R.drawable.ic_attach_money,
                R.drawable.ic_credit_card,
                R.drawable.ic_monitoring,
                R.drawable.ic_shopping_bag,
                R.drawable.ic_payments,
                R.drawable.ic_wallet,
                R.drawable.ic_star
            )
        }

        fun isNightMode(activity: Activity) : Boolean {
            return activity.getSharedPreferences("MODE", Context.MODE_PRIVATE).getBoolean(NIGHT_KEY, false)
        }
    }
}
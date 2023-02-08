package com.example.todolistkotlin.util

import android.app.Activity
import com.example.todolistkotlin.R

class AnimationUtils {

    companion object {

        fun openActivitySlideAnimation(activity: Activity) {
            activity.overridePendingTransition(
                R.anim.slide_right_to_left_enter,
                R.anim.slide_right_to_left_out
            )
        }
        fun closeActivitySlideAnimation(activity: Activity) {
            activity.overridePendingTransition(
                R.anim.slide_left_to_right_enter,
                R.anim.slide_left_to_right_out
            )
        }
    }
}
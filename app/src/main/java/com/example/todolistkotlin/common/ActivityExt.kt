package com.example.todolistkotlin.common

import androidx.fragment.app.FragmentActivity

fun FragmentActivity.navigateBack() {
    this.onBackPressedDispatcher.onBackPressed()
}
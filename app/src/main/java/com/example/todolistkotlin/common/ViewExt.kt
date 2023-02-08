package com.example.todolistkotlin.util

import android.app.Service
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.example.todolistkotlin.R
import com.example.todolistkotlin.presentation.ui_events.ErrorEvent
import com.example.todolistkotlin.presentation.ui_events.TaskEvent
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

fun View.hideKeyboard() {
    (this.context.getSystemService(Service.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.toVisible() {
    this.visibility = View.VISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.toInvisible() {
    this.visibility = View.GONE
}

fun View.setupLoadingView(active: Boolean) {
    if (active) this.toVisible() else this.toGone()
}

fun View.showSnackBar(message: String, timeLength: Int, anchorView: Int? = null) {
    val snackBar = Snackbar.make(this, message, timeLength)
    anchorView?.let { snackBar.setAnchorView(anchorView) }
    snackBar.show()
}

fun View.showSnackBarWithAction(
    message: String,
    actionMessage: String,
    buttonAction: (Any) -> Unit,
    dismissAction: (Any) -> Unit,
    dataAction: Any,
    timeLength: Int,
    anchorView: Int? = null
) {
    val snackBar = Snackbar.make(
        this,
        message,
        timeLength
    ).setAction(actionMessage) {}.addCallback(object : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            when (event) {
                DISMISS_EVENT_ACTION -> buttonAction(dataAction)
                else -> dismissAction(dataAction)
            }
        }
    })
    anchorView?.let { snackBar.setAnchorView(it) }
    snackBar.show()
}

fun ImageView.loadImage(@DrawableRes resId: Int) = Picasso.get().load(resId).into(this)

fun ImageView.loadImage(url: String) = Picasso.get().load(url).into(this)
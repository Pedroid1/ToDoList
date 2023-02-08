package com.example.todolistkotlin.presentation.states

import android.net.Uri

data class UserInfoState(
    val username: String?,
    val email: String?,
    val imageUrl: Uri?
)
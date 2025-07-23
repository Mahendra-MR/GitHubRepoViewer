package com.example.githubrepoviewer.model

data class User(
    val login: String,
    val name: String?,
    val avatar_url: String,
    val location: String?,
    val bio: String?,
    val blog: String?,
    val followers: Int,
    val following: Int
)

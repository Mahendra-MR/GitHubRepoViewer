package com.example.githubrepoviewer.model

data class Repo(
    val name: String,
    val description: String?,
    val stargazers_count: Int,
    val forks_count: Int,
    val owner: Owner,
    val html_url: String,
    val updated_at: String
)

data class Owner(
    val login: String,
    val avatar_url: String
)

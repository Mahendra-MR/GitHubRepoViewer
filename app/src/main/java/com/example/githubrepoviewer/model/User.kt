package com.example.githubrepoviewer.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String? = null,
    val login: String,
    @SerialName("avatar_url") val avatar_url: String,
    val location: String? = null,
    val bio: String? = null,
    @SerialName("public_repos") val publicRepos: Int,
    val followers: Int,
    val following: Int
)

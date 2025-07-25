package com.example.githubrepoviewer.data

import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.model.User
import com.example.githubrepoviewer.network.RateLimitResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GitHubApiService {

    // 🔍 Public user repositories
    @GET("users/{username}/repos")
    suspend fun getUserRepos(@Path("username") username: String): List<Repo>

    // 🔍 Public user profile by username
    @GET("users/{username}")
    suspend fun getUserProfile(@Path("username") username: String): User

    // 🔐 Authenticated user profile (requires Bearer token)
    @GET("user")
    suspend fun getAuthenticatedUserProfile(
        @Header("Authorization") authHeader: String,
        @Header("User-Agent") userAgent: String = "GitHubRepoViewer"
    ): User

    // 📄 Fetch README of a repository
    @GET("repos/{owner}/{repo}/readme")
    suspend fun getRepoReadme(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): ResponseBody

    // ⏳ GitHub API rate limit
    @GET("rate_limit")
    suspend fun getRateLimit(): RateLimitResponse
}

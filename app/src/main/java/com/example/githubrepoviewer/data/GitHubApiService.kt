package com.example.githubrepoviewer.data

import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.model.User
import com.example.githubrepoviewer.network.RateLimitResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApiService {

    @GET("users/{username}/repos")
    suspend fun getUserRepos(@Path("username") username: String): List<Repo>

    @GET("users/{username}")
    suspend fun getUserProfile(@Path("username") username: String): User

    @GET("repos/{owner}/{repo}/readme")
    suspend fun getRepoReadme(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): ResponseBody

    //endpoint for rate limit info
    @GET("rate_limit")
    suspend fun getRateLimit(): RateLimitResponse
}

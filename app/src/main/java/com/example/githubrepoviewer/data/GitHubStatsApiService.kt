package com.example.githubrepoviewer.data

import retrofit2.http.GET
import retrofit2.http.Query

data class CountResponse(val total_count: Int)

interface GitHubStatsApiService {
    @GET("search/repositories")
    suspend fun getTotalPublicRepos(
        @Query("q") query: String = "stars:>1"
    ): CountResponse

    @GET("search/users")
    suspend fun getTotalUsers(
        @Query("q") query: String = "followers:>1"
    ): CountResponse
}

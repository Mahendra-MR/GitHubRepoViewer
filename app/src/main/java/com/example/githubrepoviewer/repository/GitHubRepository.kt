package com.example.githubrepoviewer.repository

import com.example.githubrepoviewer.data.RetrofitClient
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.model.User
import com.example.githubrepoviewer.network.RateLimitResponse
import okhttp3.ResponseBody

class GitHubRepository {

    suspend fun getUserRepos(username: String): List<Repo> {
        return RetrofitClient.api.getUserRepos(username)
    }

    suspend fun getUserProfile(username: String): User {
        return RetrofitClient.api.getUserProfile(username)
    }

    suspend fun getRepoReadme(owner: String, repoName: String): ResponseBody {
        return RetrofitClient.api.getRepoReadme(owner, repoName)
    }

    suspend fun getGitHubGlobalStats(): Pair<Int, Int> {
        val repoStatsResponse = RetrofitClient.statsApi.getTotalPublicRepos()
        val userStatsResponse = RetrofitClient.statsApi.getTotalUsers()

        val totalRepos = repoStatsResponse.total_count
        val totalUsers = userStatsResponse.total_count

        return Pair(totalRepos, totalUsers)
    }

    suspend fun getRateLimit(): RateLimitResponse {
        return RetrofitClient.api.getRateLimit()
    }
}

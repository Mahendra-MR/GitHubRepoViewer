package com.example.githubrepoviewer.repository

import com.example.githubrepoviewer.data.RetrofitClient
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.model.User
import com.example.githubrepoviewer.network.RateLimitResponse
import okhttp3.ResponseBody

class GitHubRepository {

    // 🔍 Get public repositories for a given username
    suspend fun getUserRepos(username: String): List<Repo> {
        return RetrofitClient.api.getUserRepos(username)
    }

    // 🔍 Get public user profile by username
    suspend fun getUserProfile(username: String): User {
        return RetrofitClient.api.getUserProfile(username)
    }

    // 🔐 Get authenticated user profile using token (expects token already in "Bearer ..." format)
    suspend fun getAuthenticatedUserProfile(token: String): User {
        return RetrofitClient.api.getAuthenticatedUserProfile(token)
    }

    // 📄 Get README file of a repository
    suspend fun getRepoReadme(owner: String, repoName: String): ResponseBody {
        return RetrofitClient.api.getRepoReadme(owner, repoName)
    }

    // 🌍 Get global GitHub statistics (total repos & users)
    suspend fun getGitHubGlobalStats(): Pair<Int, Int> {
        val repoStatsResponse = RetrofitClient.statsApi.getTotalPublicRepos()
        val userStatsResponse = RetrofitClient.statsApi.getTotalUsers()

        val totalRepos = repoStatsResponse.total_count
        val totalUsers = userStatsResponse.total_count

        return Pair(totalRepos, totalUsers)
    }

    // ⏳ Get GitHub API rate limit info
    suspend fun getRateLimit(): RateLimitResponse {
        return RetrofitClient.api.getRateLimit()
    }
}

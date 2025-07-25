package com.example.githubrepoviewer.repository

import android.util.Log
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

    // 🔐 Get authenticated user profile using token - FIXED VERSION
    suspend fun getAuthenticatedUserProfile(token: String): User {
        Log.d("GitHubRepository", "Making authenticated API call with token: ${token.take(10)}...")

        // 🔥 FIXED: The GitHubApiService expects the full "Bearer token" format
        // Since we're passing it directly to the @Header annotation, we need the full format
        // The RetrofitClient interceptor should NOT add another Bearer prefix for this case
        return RetrofitClient.api.getAuthenticatedUserProfile(
            authHeader = "Bearer $token",
            userAgent = "GitHubRepoViewer"
        )
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
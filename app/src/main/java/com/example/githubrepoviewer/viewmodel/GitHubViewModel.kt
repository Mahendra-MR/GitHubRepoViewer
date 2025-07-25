package com.example.githubrepoviewer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.model.User
import com.example.githubrepoviewer.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

// Ktor
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class GitHubViewModel : ViewModel() {

    private val repository = GitHubRepository()

    private val _repos = MutableStateFlow<List<Repo>>(emptyList())
    val repos: StateFlow<List<Repo>> = _repos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _globalStats = MutableStateFlow(Pair(0, 0))  // (repos, users)
    val globalStats: StateFlow<Pair<Int, Int>> = _globalStats

    private val _rateLimitResetTime = MutableStateFlow<Long?>(null)
    val rateLimitResetTime: StateFlow<Long?> = _rateLimitResetTime

    fun fetchRepos(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val repos = repository.getUserRepos(username)
                _repos.value = repos.sortedByDescending { it.updated_at }
                if (repos.isEmpty()) {
                    _error.value = "No repositories found."
                }
            } catch (e: HttpException) {
                _repos.value = emptyList()
                when (e.code()) {
                    403 -> {
                        checkRateLimit()
                        _error.value = "Rate limit exceeded. Try again later."
                    }
                    404 -> _error.value = "User not found."
                    else -> _error.value = "Server error: ${e.code()}"
                }
            } catch (e: Exception) {
                _repos.value = emptyList()
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUserProfile(username: String) {
        viewModelScope.launch {
            try {
                _userProfile.value = repository.getUserProfile(username)
            } catch (e: HttpException) {
                _userProfile.value = null
                _error.value = when (e.code()) {
                    404 -> "User not found."
                    else -> "Error fetching profile: ${e.code()}"
                }
            } catch (e: Exception) {
                _userProfile.value = null
                _error.value = "Error fetching profile: ${e.message}"
            }
        }
    }

    fun fetchAuthenticatedUserProfile(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _userProfile.value = null
            try {
                Log.d("GitHubViewModel", "Fetching authenticated profile with token: ${token.take(10)}...")
                val user = repository.getAuthenticatedUserProfile(token)
                _userProfile.value = user
                Log.d("GitHubViewModel", "Authenticated user: ${user.login}")

                // Also fetch their public repositories
                val repos = repository.getUserRepos(user.login)
                _repos.value = repos.sortedByDescending { it.updated_at }

            } catch (e: HttpException) {
                Log.e("GitHubViewModel", "HTTP error ${e.code()}: ${e.message()}")
                _userProfile.value = null
                _repos.value = emptyList()
                _error.value = when (e.code()) {
                    401 -> "Authentication failed. Please login again."
                    403 -> "Access forbidden. Check token permissions."
                    404 -> "User not found."
                    else -> "HTTP error ${e.code()}: ${e.message()}"
                }
            } catch (e: Exception) {
                Log.e("GitHubViewModel", "Network error: ${e.message}", e)
                _userProfile.value = null
                _repos.value = emptyList()
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun fetchRepoReadme(username: String, repoName: String): String? {
        return try {
            val response = repository.getRepoReadme(username, repoName)
            val jsonString = response.string()

            val contentRegex = """"content"\s*:\s*"([^"]*)"""".toRegex()
            val encodedContent = contentRegex.find(jsonString)
                ?.groups?.get(1)?.value
                ?.replace("\\n", "")

            encodedContent?.let {
                val decodedBytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                decodedBytes.toString(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.e("GitHubViewModel", "Error decoding README: ${e.message}")
            null
        }
    }

    fun fetchGlobalGitHubStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getGitHubGlobalStats()
                _globalStats.value = stats
            } catch (e: Exception) {
                _error.value = "Error fetching GitHub global stats: ${e.message}"
            }
        }
    }

    private suspend fun checkRateLimit() {
        try {
            val response = repository.getRateLimit()
            _rateLimitResetTime.value = response.rate.reset
        } catch (_: Exception) {
            // Ignored silently
        }
    }

    fun clearUserData() {
        _userProfile.value = null
        _repos.value = emptyList()
        _error.value = null
        _rateLimitResetTime.value = null
    }

    suspend fun testTokenDirectly(token: String): String {
        return try {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            Log.d("TokenTest", "Testing token: ${token.take(20)}...")

            val response = client.get("https://api.github.com/user") {
                header("Authorization", "Bearer $token")
                header("User-Agent", "GitHubRepoViewer")
            }

            Log.d("TokenTest", "Response: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<String>()
                Log.d("TokenTest", "Token valid: ${body.take(200)}")
                "Token is valid"
            } else {
                Log.e("TokenTest", "Token test failed: ${response.status}")
                "Token test failed: ${response.status}"
            }
        } catch (e: Exception) {
            Log.e("TokenTest", "Token test exception: ${e.message}")
            "Token test exception: ${e.message}"
        }
    }
}

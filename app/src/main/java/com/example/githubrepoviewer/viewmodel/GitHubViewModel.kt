package com.example.githubrepoviewer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.model.User
import com.example.githubrepoviewer.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Base64

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
                if (e.code() == 403) {
                    checkRateLimit()
                    _error.value = "Rate limit exceeded. Try again later."
                } else {
                    _error.value = if (e.code() == 404) "User not found!" else "Server error: ${e.code()}"
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
                    404 -> "User not found!"
                    else -> "Error fetching profile: ${e.code()}"
                }
            } catch (e: Exception) {
                _userProfile.value = null
                _error.value = "Error fetching profile: ${e.message}"
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
                String(Base64.getDecoder().decode(it))
            }
        } catch (e: Exception) {
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
            // Ignore silently
        }
    }

    fun clearUserData() {
        _userProfile.value = null
        _repos.value = emptyList()
        _error.value = null
        _rateLimitResetTime.value = null
    }
}

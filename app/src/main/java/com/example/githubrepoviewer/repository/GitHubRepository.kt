package com.example.githubrepoviewer.repository

import com.example.githubrepoviewer.data.RetrofitClient
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.model.User
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
}

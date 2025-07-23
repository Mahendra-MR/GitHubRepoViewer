package com.example.githubrepoviewer.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.util.debounce
import com.example.githubrepoviewer.viewmodel.GitHubViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MainScreen(viewModel: GitHubViewModel, navController: NavController) {
    var username by remember { mutableStateOf(TextFieldValue("")) }

    val repos by viewModel.repos.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = loading)
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    // Real-time search with debounce
    LaunchedEffect(username.text) {
        debounceJob = debounce(coroutineScope, 500L, debounceJob) {
            if (username.text.isNotBlank()) {
                viewModel.fetchRepos(username.text)
                viewModel.fetchUserProfile(username.text)
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Enter GitHub Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Something went wrong.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    userProfile?.let { profile ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(profile.avatar_url),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(end = 16.dp)
                                )
                                Column {
                                    Text(
                                        profile.name ?: profile.login,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    profile.bio?.let {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(it, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Location: ${profile.location ?: "Unknown"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    if (repos.isEmpty()) {
                        Text(
                            text = "No repositories found",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = {
                                viewModel.fetchRepos(username.text)
                                viewModel.fetchUserProfile(username.text)
                            }
                        ) {
                            LazyColumn {
                                items(repos) { repo ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                val repoJson = URLEncoder.encode(
                                                    Gson().toJson(repo),
                                                    StandardCharsets.UTF_8.toString()
                                                )
                                                navController.navigate("repo_detail?repoJson=$repoJson")
                                            },
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(16.dp)) {
                                            Image(
                                                painter = rememberAsyncImagePainter(repo.owner.avatar_url),
                                                contentDescription = "Avatar",
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .padding(end = 16.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = repo.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = repo.description ?: "No description",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "⭐ ${repo.stargazers_count}   🍴 ${repo.forks_count}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

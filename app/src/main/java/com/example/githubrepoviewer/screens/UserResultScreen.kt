@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.example.githubrepoviewer.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.navigation.Screen
import com.example.githubrepoviewer.viewmodel.GitHubViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun UserResultScreen(
    username: String,
    viewModel: GitHubViewModel,
    navController: NavController
) {
    val repos by viewModel.repos.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val swipeState = rememberSwipeRefreshState(isRefreshing = isLoading)

    var searchText by remember { mutableStateOf(TextFieldValue(username)) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Fetch initial data only once
    LaunchedEffect(key1 = username) {
        viewModel.clearUserData()
        viewModel.fetchUserProfile(username)
        viewModel.fetchRepos(username)
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search bar
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search another GitHub username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        val trimmed = searchText.text.trim()
                        if (trimmed.isNotEmpty()) {
                            viewModel.clearUserData()
                            navController.navigate(Screen.UserResult.createRoute(trimmed))
                            keyboardController?.hide()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (userProfile != null && error == null) {
                val profile = userProfile!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text(profile.name ?: profile.login, style = MaterialTheme.typography.titleLarge)
                            profile.bio?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Location: ${profile.location ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (error != null) {
                Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            SwipeRefresh(
                state = swipeState,
                onRefresh = {
                    viewModel.clearUserData()
                    viewModel.fetchUserProfile(searchText.text.trim())
                    viewModel.fetchRepos(searchText.text.trim())
                }
            ) {
                if (repos.isEmpty() && !isLoading && error == null) {
                    Text("No repositories found", modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn {
                        items(repos) { repo ->
                            RepoItem(repo = repo) {
                                val encodedJson = URLEncoder.encode(
                                    Gson().toJson(repo),
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate(Screen.RepoDetail.createRoute(repo))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RepoItem(repo: Repo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(repo.owner.avatar_url),
                contentDescription = "Repo Owner Avatar",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = repo.name, style = MaterialTheme.typography.titleMedium)
                Text(text = repo.description ?: "No description", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "⭐ ${repo.stargazers_count} | 🍴 ${repo.forks_count}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

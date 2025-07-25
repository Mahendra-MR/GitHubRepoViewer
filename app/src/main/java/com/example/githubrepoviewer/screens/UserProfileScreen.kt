@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.githubrepoviewer.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.githubrepoviewer.model.User
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.navigation.Screen
import com.example.githubrepoviewer.util.TokenStore
import com.example.githubrepoviewer.viewmodel.GitHubViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: GitHubViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    val userProfile by viewModel.userProfile.collectAsState()
    val repos by viewModel.repos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        val token = tokenStore.getToken()
        if (!token.isNullOrBlank()) {
            viewModel.fetchAuthenticatedUserProfile(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My GitHub Profile") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                userProfile != null -> {
                    UserProfileContent(userProfile!!, repos, navController)
                }
                else -> {
                    Text("You are not logged in.", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(user: User, repos: List<Repo>, navController: NavController) {
    val realRepoCount = repos.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(user.avatar_url),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(user.name ?: user.login, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("@${user.login}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!user.bio.isNullOrBlank()) {
            Text("Bio", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(user.bio!!, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Divider()
        Spacer(modifier = Modifier.height(12.dp))

        Text("Stats", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat("Repos", realRepoCount.toString())
            ProfileStat("Followers", user.followers.toString())
            ProfileStat("Following", user.following.toString())
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(12.dp))

        Text("Repositories", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (repos.isEmpty()) {
            Text("No public repositories found.")
        } else {
            repos.forEach { repo ->
                RepoItemClickable(repo = repo, onClick = {
                    val encoded = URLEncoder.encode(Gson().toJson(repo), StandardCharsets.UTF_8.toString())
                    navController.navigate(Screen.RepoDetail.createRoute(repo))
                })
            }
        }
    }
}

@Composable
fun RepoItemClickable(repo: Repo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = repo.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(repo.description!!, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("⭐ ${repo.stargazers_count}    🍴 ${repo.forks_count}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

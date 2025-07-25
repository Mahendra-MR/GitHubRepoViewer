package com.example.githubrepoviewer.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.githubrepoviewer.data.RetrofitClient
import com.example.githubrepoviewer.model.User
import com.example.githubrepoviewer.util.TokenStore
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen() {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val token = tokenStore.getToken()
                Log.d("ProfileScreen", "Retrieved token: $token")

                if (token.isNullOrBlank()) {
                    error = "🔒 You are not logged in."
                } else {
                    val authHeader = "Bearer $token"
                    val userAgent = "GitHubRepoViewer"

                    user = RetrofitClient.api.getAuthenticatedUserProfile(
                        authHeader = authHeader,
                        userAgent = userAgent
                    )

                    Log.d("ProfileScreen", "Loaded user: ${user?.login}")
                }
            } catch (e: Exception) {
                error = "⚠️ Failed to load profile: ${e.message?.take(150)}"
                Log.e("ProfileScreen", "Error: ${e.message}", e)
            } finally {
                loading = false
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
            user != null -> UserProfileContent(user!!)
        }
    }
}

@Composable
fun UserProfileContent(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(user.avatar_url),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 12.dp)
        )

        Text(
            text = user.name ?: "No Name",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "@${user.login}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!user.bio.isNullOrBlank()) {
            Text(
                text = user.bio!!,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileStat("Repos", user.publicRepos.toString())
            ProfileStat("Followers", user.followers.toString())
            ProfileStat("Following", user.following.toString())
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

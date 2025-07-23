package com.example.githubrepoviewer.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.viewmodel.GitHubViewModel

@Composable
fun RepoDetailScreen(repo: Repo, viewModel: GitHubViewModel) {
    val context = LocalContext.current
    var readmeText by remember { mutableStateOf("Loading...") }

    // Fetch README content if no description is available
    LaunchedEffect(repo) {
        if (repo.description.isNullOrBlank()) {
            val readme = viewModel.fetchRepoReadme(repo.owner.login, repo.name)
            readmeText = readme?.let {
                // Clean raw markdown minimally, optionally enhance with formatting libraries later
                it.replace(Regex("<[^>]*>"), "")
                    .replace(Regex("(?m)^#+\\s*"), "") // Remove markdown headers
                    .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1") // [text](url) → text
                    .replace("```", "")
                    .trim()
            } ?: "No description available."
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Repo title
            Text(text = repo.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))

            // Owner info
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(repo.owner.avatar_url),
                    contentDescription = "Owner Avatar",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Owner: ${repo.owner.login}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stars and forks
            Text(
                text = "⭐ Stars: ${repo.stargazers_count}   🍴 Forks: ${repo.forks_count}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // GitHub Link
            ClickableText(
                text = AnnotatedString("View on GitHub →"),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repo.html_url))
                    context.startActivity(intent)
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description or full README
            Text(
                text = repo.description ?: readmeText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

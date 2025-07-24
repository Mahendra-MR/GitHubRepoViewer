@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.example.githubrepoviewer.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.githubrepoviewer.navigation.Screen
import com.example.githubrepoviewer.viewmodel.GitHubViewModel

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: GitHubViewModel = viewModel()
) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val globalStats = viewModel.globalStats.collectAsState().value ?: (0 to 0)

    LaunchedEffect(Unit) {
        viewModel.fetchGlobalGitHubStats()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🚀 GitHub Repo Viewer",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 12.dp)
            )

            SearchBar(username) { input ->
                if (input.isNotBlank()) {
                    navController.navigate(Screen.UserResult.createRoute(input.trim()))
                    keyboardController?.hide()
                }
            }

            GitHubStatsSection(globalStats)

            InsightSection()

            ContributionPlaceholder()
        }
    }
}

@Composable
fun SearchBar(username: TextFieldValue, onSearch: (String) -> Unit) {
    var localUsername by remember { mutableStateOf(username) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        TextField(
            value = localUsername,
            onValueChange = { localUsername = it },
            label = { Text("Search GitHub Username") },
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(localUsername.text)
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onSearch(localUsername.text)
                keyboardController?.hide()
            },
            modifier = Modifier.align(Alignment.End),
            enabled = localUsername.text.isNotBlank()
        ) {
            Text("Search")
        }
    }
}

@Composable
fun GitHubStatsSection(globalStats: Pair<Int, Int>) {
    Column {
        Text(
            text = "🌍 GitHub Global Stats",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(title = "Total Repos", value = globalStats.first.toString())
            StatCard(title = "Total Users", value = globalStats.second.toString())
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    val backgroundColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceVariant,
        label = "CardBG"
    )

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun InsightSection() {
    Column {
        Text(
            text = "💡 Developer Fun Fact",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Every minute, hundreds of stars are given to GitHub repos around the world 🌟",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ContributionPlaceholder() {
    Column {
        Text(
            text = "📈 Contribution Overview",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "User contribution graph will appear here upon profile search.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

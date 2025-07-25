@file:OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.githubrepoviewer.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.githubrepoviewer.navigation.Screen
import com.example.githubrepoviewer.util.TokenStore
import com.example.githubrepoviewer.viewmodel.GitHubViewModel
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: GitHubViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val globalStats = viewModel.globalStats.collectAsState().value ?: (0 to 0)
    val tokenStore = remember { TokenStore(context) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchGlobalGitHubStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GitHub Repo Viewer") },
                actions = {
                    SettingsMenu(
                        onLogout = {
                            scope.launch {
                                tokenStore.clearToken()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                }
                            }
                        },
                        onProfile = {
                            navController.navigate(Screen.AuthenticatedProfile.route)
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SearchBar(username) { input ->
                if (input.isNotBlank()) {
                    navController.navigate(Screen.UserResult.createRoute(input.trim()))
                    keyboardController?.hide()
                }
            }

            GitHubStatsSection(globalStats)
            TopLanguagesSection()
            InsightSection()
            TrendingTopicsSection()
        }
    }
}

@Composable
fun SettingsMenu(
    onLogout: () -> Unit,
    onProfile: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Logout") },
            onClick = {
                expanded = false
                onLogout()
            }
        )
        DropdownMenuItem(
            text = { Text("My GitHub Profile") },
            onClick = {
                expanded = false
                onProfile()
            }
        )
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
            modifier = Modifier.fillMaxWidth(),
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
        Text("GitHub Global Stats", style = MaterialTheme.typography.titleMedium)
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
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun TopLanguagesSection() {
    val languages = listOf("JavaScript", "Python", "Java", "TypeScript", "C++", "Go", "Rust", "Kotlin")

    Column {
        Text("Most Popular Languages", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            mainAxisSpacing = 12.dp,
            crossAxisSpacing = 8.dp
        ) {
            languages.forEach { lang ->
                AssistChip(
                    onClick = { /* Navigate to filtered search by language */ },
                    label = { Text(lang) }
                )
            }
        }
    }
}

@Composable
fun InsightSection() {
    val funFacts = listOf(
        "Every minute, hundreds of ⭐ stars are given to GitHub repos around the world.",
        "Linus Torvalds created Git in 2005 to manage the Linux kernel.",
        "GitHub was founded in 2008 and acquired by Microsoft in 2018.",
        "Octocat, GitHub’s mascot, has over 40 official versions.",
        "There are over 370 million public GitHub repositories!",
        "GitHub Copilot is powered by OpenAI and suggests code as you type.",
        "Python has been the most forked language in recent years.",
        "GitHub Actions allows automation directly inside your repos.",
        "The term 'bug' originated from a real moth found in a computer in 1947.",
        "The first website ever created is still online at info.cern.ch"
    )

    Column {
        Text("Developer Fun Facts", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        funFacts.take(5).forEach { fact ->
            Text(
                text = "• $fact",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun TrendingTopicsSection() {
    val topics = listOf("machine-learning", "android", "web3", "game-dev", "docker", "devops", "ai", "security")

    Column {
        Text("Trending Topics", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            mainAxisSpacing = 12.dp,
            crossAxisSpacing = 8.dp
        ) {
            topics.forEach { topic ->
                SuggestionChip(
                    onClick = { /* Navigate to topic */ },
                    label = { Text("#$topic") }
                )
            }
        }
    }
}

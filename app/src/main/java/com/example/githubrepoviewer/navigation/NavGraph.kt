package com.example.githubrepoviewer.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.githubrepoviewer.viewmodel.GitHubViewModel
import com.example.githubrepoviewer.screens.DashboardScreen
import com.example.githubrepoviewer.screens.UserResultScreen
import com.example.githubrepoviewer.screens.RepoDetailScreen
import com.example.githubrepoviewer.model.Repo
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// 📌 Define navigation routes
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")

    object UserResult : Screen("user_result/{username}") {
        fun createRoute(username: String): String = "user_result/$username"
    }

    object RepoDetail : Screen("repo_detail") {
        fun createRoute(repo: Repo): String {
            val json = Uri.encode(Gson().toJson(repo))
            return "repo_detail?repoJson=$json"
        }
    }
}

@Composable
fun AppNavGraph(viewModel: GitHubViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {

        // 🏠 Dashboard (Home Page)
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        // 👤 User Result Screen
        composable(
            route = Screen.UserResult.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            UserResultScreen(username = username, viewModel = viewModel, navController = navController)
        }

        // 📄 Repository Detail Screen
        composable(
            route = "${Screen.RepoDetail.route}?repoJson={repoJson}",
            arguments = listOf(navArgument("repoJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedJson = backStackEntry.arguments?.getString("repoJson") ?: ""
            val decodedJson = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8.toString())
            val repo = Gson().fromJson(decodedJson, Repo::class.java)
            RepoDetailScreen(repo = repo, viewModel = viewModel)
        }
    }
}

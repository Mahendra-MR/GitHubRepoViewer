package com.example.githubrepoviewer.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.githubrepoviewer.viewmodel.GitHubViewModel
import com.example.githubrepoviewer.model.Repo
import com.example.githubrepoviewer.screens.*
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Login : Screen("login")
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

    object AuthenticatedProfile : Screen("authenticated_profile")
}

@Composable
fun AppNavGraph(
    viewModel: GitHubViewModel,
    startDestination: String = Screen.Login.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // Dashboard Screen
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // Searched User Result
        composable(
            route = Screen.UserResult.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            UserResultScreen(username = username, viewModel = viewModel, navController = navController)
        }

        // Repo Detail Screen
        composable(
            route = "${Screen.RepoDetail.route}?repoJson={repoJson}",
            arguments = listOf(navArgument("repoJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedJson = backStackEntry.arguments?.getString("repoJson") ?: ""
            val decodedJson = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8.toString())
            val repo = Gson().fromJson(decodedJson, Repo::class.java)
            RepoDetailScreen(repo = repo, viewModel = viewModel)
        }

        // Authenticated GitHub Profile
        composable(Screen.AuthenticatedProfile.route) {
            UserProfileScreen(navController = navController)
        }
    }
}

package com.example.githubrepoviewer.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.githubrepoviewer.viewmodel.GitHubViewModel
import com.example.githubrepoviewer.screens.MainScreen
import com.example.githubrepoviewer.screens.RepoDetailScreen
import com.example.githubrepoviewer.model.Repo
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Main : Screen("main")

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

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(viewModel = viewModel, navController = navController)
        }

        composable(
            route = "${Screen.RepoDetail.route}?repoJson={repoJson}",
            arguments = listOf(
                navArgument("repoJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedJson = backStackEntry.arguments?.getString("repoJson")
            val decodedJson = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8.toString())
            val repo = Gson().fromJson(decodedJson, Repo::class.java)
            RepoDetailScreen(repo = repo, viewModel = viewModel)
        }
    }
}

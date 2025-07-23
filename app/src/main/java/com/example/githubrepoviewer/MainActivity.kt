package com.example.githubrepoviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.githubrepoviewer.navigation.AppNavGraph
import com.example.githubrepoviewer.viewmodel.GitHubViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: GitHubViewModel = viewModel()
            AppNavGraph(viewModel = viewModel)
        }
    }
}

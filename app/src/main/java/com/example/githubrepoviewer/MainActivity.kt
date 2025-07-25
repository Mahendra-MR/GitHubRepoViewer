package com.example.githubrepoviewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.githubrepoviewer.data.RetrofitClient
import com.example.githubrepoviewer.navigation.AppNavGraph
import com.example.githubrepoviewer.navigation.Screen
import com.example.githubrepoviewer.util.TokenStore
import com.example.githubrepoviewer.viewmodel.GitHubViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenStore = TokenStore(applicationContext)
        RetrofitClient.init(applicationContext)

        val shouldNavigateToDashboard = intent?.getBooleanExtra("navigate_to_dashboard", false) ?: false

        setContent {
            val viewModel: GitHubViewModel = viewModel()
            if (shouldNavigateToDashboard) {
                AppNavGraph(viewModel = viewModel, startDestination = Screen.Dashboard.route)
            } else {
                AppNavGraph(viewModel = viewModel)
            }
        }

        // Catch browser redirect and handle token
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleOAuthRedirect(intent)
    }

    private fun handleOAuthRedirect(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "myapp" && uri.host == "callback") {
                val code = uri.getQueryParameter("code")
                val error = uri.getQueryParameter("error")

                if (!code.isNullOrEmpty()) {
                    exchangeCodeForToken(code)
                } else if (!error.isNullOrEmpty()) {
                    Log.e("OAuth", "OAuth error: $error")
                }
            }
        }
    }

    private fun exchangeCodeForToken(code: String) {
        lifecycleScope.launch {
            try {
                val client = HttpClient(CIO) {
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        })
                    }
                }

                val response = client.get("https://github-oauth-proxy-uuuo.onrender.com/exchange_token") {
                    parameter("code", code)
                }

                val responseText = response.body<String>()
                val json = JSONObject(responseText)

                if (json.has("error")) {
                    val error = json.getString("error")
                    val description = json.optString("error_description", "")
                    Log.e("OAuth", "Token exchange error: $error - $description")
                    return@launch
                }

                if (json.has("access_token")) {
                    val accessToken = json.getString("access_token")
                    tokenStore.saveToken(accessToken)

                    // Navigate directly to dashboard after login success
                    val dashboardIntent = Intent(this@MainActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("navigate_to_dashboard", true)
                    }
                    startActivity(dashboardIntent)
                    finish()
                } else {
                    Log.e("OAuth", "No access_token in response: $responseText")
                }

            } catch (e: Exception) {
                Log.e("OAuth", "Token exchange failed: ${e.message}", e)
            }
        }
    }
}

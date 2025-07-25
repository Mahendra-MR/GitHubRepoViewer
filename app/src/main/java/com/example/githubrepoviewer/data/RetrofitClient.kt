package com.example.githubrepoviewer.data

import android.content.Context
import android.util.Log
import com.example.githubrepoviewer.util.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://api.github.com/"

    private var retrofit: Retrofit? = null
    private var _api: GitHubApiService? = null
    private var _statsApi: GitHubStatsApiService? = null

    fun init(context: Context) {
        val tokenStore = TokenStore(context)

        // Enhanced logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest: Request = chain.request()

            Log.d("RetrofitClient", "🔍 Processing request: ${originalRequest.url}")

            // Check if Authorization header is already present
            val existingAuth = originalRequest.header("Authorization")

            if (existingAuth != null) {
                Log.d("RetrofitClient", "✅ Authorization header already present: ${existingAuth.take(30)}...")
                // Don't modify - pass through as-is
                val response = chain.proceed(originalRequest)
                Log.d("RetrofitClient", "📨 Response code: ${response.code}")
                return@Interceptor response
            }

            // Only add auth for requests without existing auth header
            val isUserEndpoint = originalRequest.url.encodedPath == "/user"

            val newRequest = if (isUserEndpoint) {
                val token = runBlocking { tokenStore.getToken() }
                Log.d("RetrofitClient", "🔍 Retrieved token: ${token?.take(20)}...")

                if (!token.isNullOrEmpty()) {
                    Log.d("RetrofitClient", "✅ Adding Authorization header from interceptor")
                    originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("User-Agent", "GitHubRepoViewer")
                        .build()
                } else {
                    originalRequest
                }
            } else {
                originalRequest
            }

            val response = chain.proceed(newRequest)
            Log.d("RetrofitClient", "📨 Response code: ${response.code}")

            if (!response.isSuccessful) {
                Log.e("RetrofitClient", "❌ Request failed: ${response.code}")
            }

            response
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        _api = retrofit!!.create(GitHubApiService::class.java)
        _statsApi = retrofit!!.create(GitHubStatsApiService::class.java)
    }

    val api: GitHubApiService
        get() = _api ?: throw IllegalStateException("Retrofit not initialized. Call RetrofitClient.init(context) first.")

    val statsApi: GitHubStatsApiService
        get() = _statsApi ?: throw IllegalStateException("Retrofit not initialized. Call RetrofitClient.init(context) first.")
}
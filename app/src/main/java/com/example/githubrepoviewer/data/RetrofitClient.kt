package com.example.githubrepoviewer.data

import android.content.Context
import com.example.githubrepoviewer.util.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://api.github.com/"

    private var retrofit: Retrofit? = null
    private var _api: GitHubApiService? = null
    private var _statsApi: GitHubStatsApiService? = null

    fun init(context: Context) {
        val tokenStore = TokenStore(context)

        val authInterceptor = Interceptor { chain ->
            val originalRequest: Request = chain.request()
            val token = runBlocking { tokenStore.getToken() }
            val newRequest = if (!token.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }
            chain.proceed(newRequest)
        }

        val client = OkHttpClient.Builder()
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

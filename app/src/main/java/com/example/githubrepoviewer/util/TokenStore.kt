package com.example.githubrepoviewer.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Define extension property for Context
val Context.dataStore by preferencesDataStore(name = "auth")

class TokenStore(private val context: Context) {

    private val tokenKey = stringPreferencesKey("github_token")

    // ✅ Reactive token flow
    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[tokenKey] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data
            .map { prefs -> prefs[tokenKey] }
            .first()
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
        }
    }
}

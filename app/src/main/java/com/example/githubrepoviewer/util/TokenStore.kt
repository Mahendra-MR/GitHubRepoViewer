package com.example.githubrepoviewer.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Create a singleton DataStore instance using extension property
val Context.dataStore by preferencesDataStore(name = "auth")

class TokenStore(private val context: Context) {

    private val tokenKey = stringPreferencesKey("github_token")

    // Observe token changes
    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[tokenKey] }

    // Save token persistently
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    // Read token once
    suspend fun getToken(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[tokenKey] }
            .first()
    }

    // Clear token on logout
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
}

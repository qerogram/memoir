package com.memoir.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.memoir.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore for authentication tokens and flags
 * Uses encrypted preferences for secure storage
 */

// Extension property to create DataStore instance
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_prefs"
)

@Singleton
class AuthDataStore @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.authDataStore

    // Preference keys
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey(Constants.DataStoreKeys.ACCESS_TOKEN)
        val REFRESH_TOKEN = stringPreferencesKey(Constants.DataStoreKeys.REFRESH_TOKEN)
        val ACCESS_TOKEN_EXPIRY = stringPreferencesKey(Constants.DataStoreKeys.ACCESS_TOKEN_EXPIRY)
        val REFRESH_TOKEN_EXPIRY = stringPreferencesKey(Constants.DataStoreKeys.REFRESH_TOKEN_EXPIRY)
        val ONBOARDING_COMPLETED = booleanPreferencesKey(Constants.DataStoreKeys.ONBOARDING_COMPLETED)
        val USER_ID = stringPreferencesKey(Constants.DataStoreKeys.USER_ID)
    }

    /**
     * Save access token
     */
    suspend fun saveAccessToken(token: String, expiryTimestamp: String) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = token
            preferences[Keys.ACCESS_TOKEN_EXPIRY] = expiryTimestamp
        }
    }

    /**
     * Save refresh token
     */
    suspend fun saveRefreshToken(token: String, expiryTimestamp: String) {
        dataStore.edit { preferences ->
            preferences[Keys.REFRESH_TOKEN] = token
            preferences[Keys.REFRESH_TOKEN_EXPIRY] = expiryTimestamp
        }
    }

    /**
     * Save both tokens at once
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        accessExpiry: String,
        refreshExpiry: String
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.REFRESH_TOKEN] = refreshToken
            preferences[Keys.ACCESS_TOKEN_EXPIRY] = accessExpiry
            preferences[Keys.REFRESH_TOKEN_EXPIRY] = refreshExpiry
        }
    }

    /**
     * Get access token
     */
    suspend fun getAccessToken(): String? {
        return dataStore.data.firstOrNull()?.get(Keys.ACCESS_TOKEN)
    }

    /**
     * Get refresh token
     */
    suspend fun getRefreshToken(): String? {
        return dataStore.data.firstOrNull()?.get(Keys.REFRESH_TOKEN)
    }

    /**
     * Get access token expiry
     */
    suspend fun getAccessTokenExpiry(): String? {
        return dataStore.data.firstOrNull()?.get(Keys.ACCESS_TOKEN_EXPIRY)
    }

    /**
     * Get refresh token expiry
     */
    suspend fun getRefreshTokenExpiry(): String? {
        return dataStore.data.firstOrNull()?.get(Keys.REFRESH_TOKEN_EXPIRY)
    }

    /**
     * Check if user is authenticated (has valid tokens)
     */
    fun isAuthenticatedFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val accessToken = preferences[Keys.ACCESS_TOKEN]
            val refreshToken = preferences[Keys.REFRESH_TOKEN]
            !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return isAuthenticatedFlow().firstOrNull() ?: false
    }

    /**
     * Save user ID
     */
    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[Keys.USER_ID] = userId
        }
    }

    /**
     * Get user ID
     */
    suspend fun getUserId(): String? {
        return dataStore.data.firstOrNull()?.get(Keys.USER_ID)
    }

    /**
     * Save onboarding completion status
     */
    suspend fun saveOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    /**
     * Get onboarding completion status
     */
    fun getOnboardingCompletedFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] ?: false
        }
    }

    suspend fun getOnboardingCompleted(): Boolean {
        return dataStore.data.firstOrNull()?.get(Keys.ONBOARDING_COMPLETED) ?: false
    }

    /**
     * Clear all auth data (logout)
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Clear only tokens (keep user ID and onboarding status)
     */
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.ACCESS_TOKEN)
            preferences.remove(Keys.REFRESH_TOKEN)
            preferences.remove(Keys.ACCESS_TOKEN_EXPIRY)
            preferences.remove(Keys.REFRESH_TOKEN_EXPIRY)
        }
    }
}

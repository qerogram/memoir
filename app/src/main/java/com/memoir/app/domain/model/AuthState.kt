package com.memoir.app.domain.model

/**
 * Authentication state sealed class
 */
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val userExists: Boolean, val onboardingCompleted: Boolean) : AuthState()
    data class Error(val message: String, val code: String? = null) : AuthState()
}

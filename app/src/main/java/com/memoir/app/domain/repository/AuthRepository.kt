package com.memoir.app.domain.repository

import com.memoir.app.data.remote.dto.AuthResponse
import com.memoir.app.util.Result

/**
 * Authentication repository interface
 */
interface AuthRepository {

    /**
     * Login with Kakao OAuth code
     */
    suspend fun loginWithKakao(code: String): Result<AuthResponse>

    /**
     * Refresh access token
     */
    suspend fun refreshToken(): Result<AuthResponse>

    /**
     * Logout user (clear local tokens)
     */
    suspend fun logout()

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean
}

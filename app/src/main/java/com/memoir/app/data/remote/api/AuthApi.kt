package com.memoir.app.data.remote.api

import com.memoir.app.data.remote.dto.AuthResponse
import com.memoir.app.data.remote.dto.KakaoAuthRequest
import com.memoir.app.data.remote.dto.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Authentication API interface
 */
interface AuthApi {

    /**
     * Exchange Kakao OAuth code for access and refresh tokens
     * POST /api/v1/auth/kakao
     */
    @POST("api/v1/auth/kakao")
    suspend fun exchangeKakaoToken(
        @Body request: KakaoAuthRequest
    ): Response<AuthResponse>

    /**
     * Refresh access token using refresh token
     * POST /api/v1/auth/refresh
     */
    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthResponse>
}

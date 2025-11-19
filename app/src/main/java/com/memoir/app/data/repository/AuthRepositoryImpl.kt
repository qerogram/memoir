package com.memoir.app.data.repository

import com.memoir.app.data.local.datastore.AuthDataStore
import com.memoir.app.data.remote.api.AuthApi
import com.memoir.app.data.remote.dto.AuthResponse
import com.memoir.app.data.remote.dto.KakaoAuthRequest
import com.memoir.app.data.remote.dto.RefreshTokenRequest
import com.memoir.app.domain.repository.AuthRepository
import com.memoir.app.util.Constants
import com.memoir.app.util.Result
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Implementation of AuthRepository
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val authDataStore: AuthDataStore
) : AuthRepository {

    override suspend fun loginWithKakao(code: String): Result<AuthResponse> {
        return try {
            val request = KakaoAuthRequest(kakaoOauthCode = code)
            val response = authApi.exchangeKakaoToken(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Save tokens to DataStore
                val now = ZonedDateTime.now()
                val accessExpiry = now.plusDays(Constants.ACCESS_TOKEN_EXPIRY_DAYS.toLong())
                val refreshExpiry = now.plusDays(Constants.REFRESH_TOKEN_EXPIRY_DAYS.toLong())

                authDataStore.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    accessExpiry = accessExpiry.toString(),
                    refreshExpiry = refreshExpiry.toString()
                )

                // Save onboarding status
                authDataStore.saveOnboardingCompleted(authResponse.onboardingCompleted)

                Result.Success(authResponse)
            } else {
                val errorCode = response.code().toString()
                val errorMessage = response.message() ?: "로그인에 실패했습니다"
                Result.Error(errorMessage, errorCode)
            }
        } catch (e: Exception) {
            Result.Error(
                message = "네트워크 오류가 발생했습니다: ${e.localizedMessage}",
                code = Constants.ErrorCodes.NETWORK_ERROR,
                throwable = e
            )
        }
    }

    override suspend fun refreshToken(): Result<AuthResponse> {
        return try {
            val currentRefreshToken = authDataStore.getRefreshToken()
                ?: return Result.Error("Refresh token not found", Constants.ErrorCodes.TOKEN_INVALID)

            val request = RefreshTokenRequest(refreshToken = currentRefreshToken)
            val response = authApi.refreshToken(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Save new tokens (rotation)
                val now = ZonedDateTime.now()
                val accessExpiry = now.plusDays(Constants.ACCESS_TOKEN_EXPIRY_DAYS.toLong())
                val refreshExpiry = now.plusDays(Constants.REFRESH_TOKEN_EXPIRY_DAYS.toLong())

                authDataStore.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    accessExpiry = accessExpiry.toString(),
                    refreshExpiry = refreshExpiry.toString()
                )

                Result.Success(authResponse)
            } else {
                val errorCode = response.code().toString()
                val errorMessage = response.message() ?: "토큰 갱신에 실패했습니다"
                Result.Error(errorMessage, errorCode)
            }
        } catch (e: Exception) {
            Result.Error(
                message = "토큰 갱신 중 오류가 발생했습니다: ${e.localizedMessage}",
                code = Constants.ErrorCodes.NETWORK_ERROR,
                throwable = e
            )
        }
    }

    override suspend fun logout() {
        authDataStore.clearAll()
    }

    override suspend fun isAuthenticated(): Boolean {
        return authDataStore.isAuthenticated()
    }
}

package com.memoir.app.data.repository

import com.memoir.app.data.local.datastore.AuthDataStore
import com.memoir.app.data.remote.api.AuthApi
import com.memoir.app.data.remote.dto.AuthResponse
import com.memoir.app.data.remote.dto.KakaoAuthRequest
import com.memoir.app.data.remote.dto.RefreshTokenRequest
import com.memoir.app.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Unit tests for AuthRepositoryImpl
 */
class AuthRepositoryImplTest {

    private lateinit var repository: AuthRepositoryImpl
    private lateinit var authApi: AuthApi
    private lateinit var authDataStore: AuthDataStore

    @Before
    fun setup() {
        authApi = mockk()
        authDataStore = mockk(relaxed = true)
        repository = AuthRepositoryImpl(authApi, authDataStore)
    }

    @Test
    fun `loginWithKakao should save tokens and return Success when API succeeds`() = runTest {
        // Given
        val kakaoCode = "kakao_oauth_code_123"
        val authResponse = AuthResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            userExists = true,
            onboardingCompleted = false
        )

        coEvery {
            authApi.exchangeKakaoToken(KakaoAuthRequest(kakaoCode))
        } returns Response.success(authResponse)

        // When
        val result = repository.loginWithKakao(kakaoCode)

        // Then
        assertTrue(result is Result.Success)
        if (result is Result.Success) {
            assertEquals(authResponse, result.data)
        }

        coVerify {
            authDataStore.saveTokens(
                accessToken = "access_token",
                refreshToken = "refresh_token",
                accessExpiry = any(),
                refreshExpiry = any()
            )
            authDataStore.saveOnboardingCompleted(false)
        }
    }

    @Test
    fun `loginWithKakao should return Error when API fails`() = runTest {
        // Given
        val kakaoCode = "kakao_oauth_code_123"
        coEvery {
            authApi.exchangeKakaoToken(any())
        } returns Response.error(401, "Unauthorized".toResponseBody())

        // When
        val result = repository.loginWithKakao(kakaoCode)

        // Then
        assertTrue(result is Result.Error)
        if (result is Result.Error) {
            assertEquals("401", result.code)
        }
    }

    @Test
    fun `loginWithKakao should return Error when network exception occurs`() = runTest {
        // Given
        val kakaoCode = "kakao_oauth_code_123"
        coEvery {
            authApi.exchangeKakaoToken(any())
        } throws Exception("Network error")

        // When
        val result = repository.loginWithKakao(kakaoCode)

        // Then
        assertTrue(result is Result.Error)
        if (result is Result.Error) {
            assertTrue(result.message.contains("네트워크 오류"))
        }
    }

    @Test
    fun `refreshToken should save new tokens and return Success when API succeeds`() = runTest {
        // Given
        val currentRefreshToken = "current_refresh_token"
        val newAuthResponse = AuthResponse(
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
            userExists = true,
            onboardingCompleted = true
        )

        coEvery { authDataStore.getRefreshToken() } returns currentRefreshToken
        coEvery {
            authApi.refreshToken(RefreshTokenRequest(currentRefreshToken))
        } returns Response.success(newAuthResponse)

        // When
        val result = repository.refreshToken()

        // Then
        assertTrue(result is Result.Success)
        if (result is Result.Success) {
            assertEquals(newAuthResponse, result.data)
        }

        coVerify {
            authDataStore.saveTokens(
                accessToken = "new_access_token",
                refreshToken = "new_refresh_token",
                accessExpiry = any(),
                refreshExpiry = any()
            )
        }
    }

    @Test
    fun `refreshToken should return Error when refresh token is null`() = runTest {
        // Given
        coEvery { authDataStore.getRefreshToken() } returns null

        // When
        val result = repository.refreshToken()

        // Then
        assertTrue(result is Result.Error)
        if (result is Result.Error) {
            assertTrue(result.message.contains("Refresh token not found"))
        }
    }

    @Test
    fun `refreshToken should return Error when API fails`() = runTest {
        // Given
        coEvery { authDataStore.getRefreshToken() } returns "refresh_token"
        coEvery {
            authApi.refreshToken(any())
        } returns Response.error(401, "Unauthorized".toResponseBody())

        // When
        val result = repository.refreshToken()

        // Then
        assertTrue(result is Result.Error)
        if (result is Result.Error) {
            assertEquals("401", result.code)
        }
    }

    @Test
    fun `logout should clear all data from AuthDataStore`() = runTest {
        // When
        repository.logout()

        // Then
        coVerify { authDataStore.clearAll() }
    }

    @Test
    fun `isAuthenticated should return value from AuthDataStore`() = runTest {
        // Given
        coEvery { authDataStore.isAuthenticated() } returns true

        // When
        val result = repository.isAuthenticated()

        // Then
        assertTrue(result)
        coVerify { authDataStore.isAuthenticated() }
    }
}

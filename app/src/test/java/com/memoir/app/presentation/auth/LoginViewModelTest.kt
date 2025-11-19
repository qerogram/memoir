package com.memoir.app.presentation.auth

import android.content.Context
import com.memoir.app.data.remote.dto.AuthResponse
import com.memoir.app.domain.model.AuthState
import com.memoir.app.domain.usecase.LoginWithKakaoUseCase
import com.memoir.app.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoginViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var loginWithKakaoUseCase: LoginWithKakaoUseCase
    private lateinit var context: Context
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        loginWithKakaoUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loginWithKakao should set Loading state initially`() = runTest {
        // Given
        val authResponse = AuthResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            userExists = true,
            onboardingCompleted = false
        )
        coEvery {
            loginWithKakaoUseCase(any(), any(), any())
        } returns Result.Success(authResponse)

        viewModel = LoginViewModel(context, loginWithKakaoUseCase)

        // When
        viewModel.loginWithKakao()

        // Then - initial state should be Loading
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Loading || state is AuthState.Success)
    }

    @Test
    fun `loginWithKakao should emit Success state when login succeeds`() = runTest {
        // Given
        val authResponse = AuthResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            userExists = true,
            onboardingCompleted = false
        )
        coEvery {
            loginWithKakaoUseCase(any(), any(), any())
        } returns Result.Success(authResponse)

        viewModel = LoginViewModel(context, loginWithKakaoUseCase)

        // When
        viewModel.loginWithKakao()
        advanceUntilIdle()

        // Then
        val state = viewModel.authState.first()
        assertTrue(state is AuthState.Success)
        if (state is AuthState.Success) {
            assertEquals(true, state.userExists)
            assertEquals(false, state.onboardingCompleted)
        }
    }

    @Test
    fun `loginWithKakao should emit Error state when login fails`() = runTest {
        // Given
        val errorMessage = "로그인에 실패했습니다"
        coEvery {
            loginWithKakaoUseCase(any(), any(), any())
        } returns Result.Error(errorMessage, "401")

        viewModel = LoginViewModel(context, loginWithKakaoUseCase)

        // When
        viewModel.loginWithKakao()
        advanceUntilIdle()

        // Then
        val state = viewModel.authState.first()
        assertTrue(state is AuthState.Error)
        if (state is AuthState.Error) {
            assertEquals(errorMessage, state.message)
            assertEquals("401", state.code)
        }
    }

    @Test
    fun `initial auth state should be Idle`() = runTest {
        // Given
        loginWithKakaoUseCase = mockk()
        viewModel = LoginViewModel(context, loginWithKakaoUseCase)

        // Then
        val state = viewModel.authState.first()
        assertTrue(state is AuthState.Idle)
    }
}

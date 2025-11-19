package com.memoir.app.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memoir.app.domain.model.AuthState
import com.memoir.app.domain.usecase.LoginWithKakaoUseCase
import com.memoir.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Login screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loginWithKakaoUseCase: LoginWithKakaoUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun loginWithKakao() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = loginWithKakaoUseCase(
                onLoginStart = {
                    // Already set loading state
                },
                onLoginSuccess = {
                    // Token received from Kakao
                },
                onLoginFailure = { error ->
                    _authState.value = AuthState.Error(
                        message = error.localizedMessage ?: "카카오 로그인에 실패했습니다"
                    )
                }
            )

            _authState.value = when (result) {
                is Result.Success -> {
                    AuthState.Success(
                        userExists = result.data.userExists,
                        onboardingCompleted = result.data.onboardingCompleted
                    )
                }
                is Result.Error -> {
                    AuthState.Error(
                        message = result.message,
                        code = result.code
                    )
                }
                is Result.Loading -> AuthState.Loading
            }
        }
    }
}

package com.memoir.app.domain.usecase

import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.memoir.app.data.remote.dto.AuthResponse
import com.memoir.app.domain.repository.AuthRepository
import com.memoir.app.util.Result
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Use case for logging in with Kakao
 */
class LoginWithKakaoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        onLoginStart: () -> Unit,
        onLoginSuccess: (OAuthToken) -> Unit,
        onLoginFailure: (Throwable) -> Unit
    ): Result<AuthResponse> {
        return try {
            onLoginStart()

            // Get Kakao OAuth token
            val kakaoToken = getKakaoToken(
                onSuccess = onLoginSuccess,
                onFailure = onLoginFailure
            ) ?: return Result.Error("카카오 로그인에 실패했습니다")

            // Exchange Kakao token for backend tokens
            val result = authRepository.loginWithKakao(kakaoToken.accessToken)

            result
        } catch (e: Exception) {
            Result.Error(
                message = "로그인 중 오류가 발생했습니다: ${e.localizedMessage}",
                throwable = e
            )
        }
    }

    private suspend fun getKakaoToken(
        onSuccess: (OAuthToken) -> Unit,
        onFailure: (Throwable) -> Unit
    ): OAuthToken? = suspendCancellableCoroutine { continuation ->
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> {
                    onFailure(error)
                    continuation.resume(null)
                }
                token != null -> {
                    onSuccess(token)
                    continuation.resume(token)
                }
                else -> {
                    continuation.resume(null)
                }
            }
        }

        // Check if Kakao Talk is available
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(
                android.app.Application()
            )
        ) {
            // Login with Kakao Talk
            UserApiClient.instance.loginWithKakaoTalk(
                context = android.app.Application(),
                callback = callback
            )
        } else {
            // Login with Kakao Account (web)
            UserApiClient.instance.loginWithKakaoAccount(
                context = android.app.Application(),
                callback = callback
            )
        }
    }
}

package com.memoir.app.domain.usecase

import com.memoir.app.data.remote.dto.AuthResponse
import com.memoir.app.domain.repository.AuthRepository
import com.memoir.app.util.Result
import javax.inject.Inject

/**
 * Use case for refreshing access token
 */
class RefreshTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): Result<AuthResponse> {
        return authRepository.refreshToken()
    }
}

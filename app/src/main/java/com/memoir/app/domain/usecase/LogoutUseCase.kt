package com.memoir.app.domain.usecase

import com.memoir.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for logging out
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke() {
        authRepository.logout()
    }
}

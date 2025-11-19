package com.memoir.app.data.repository

import com.memoir.app.data.local.database.UserDao
import com.memoir.app.data.local.database.UserEntity
import com.memoir.app.data.local.datastore.AuthDataStore
import com.memoir.app.data.remote.api.UserApi
import com.memoir.app.data.remote.dto.ProfileRequest
import com.memoir.app.domain.model.User
import com.memoir.app.domain.model.UserProfile
import com.memoir.app.domain.model.UserStatus
import com.memoir.app.domain.repository.UserRepository
import com.memoir.app.util.Constants
import com.memoir.app.util.Result
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Implementation of UserRepository
 */
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val authDataStore: AuthDataStore
) : UserRepository {

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        return try {
            val request = ProfileRequest(
                name = profile.name,
                role = profile.role,
                industryCode = profile.industryCode.name,
                growthGoals = profile.growthGoals
            )

            val response = userApi.submitProfile(request)

            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!

                // Save user ID
                authDataStore.saveUserId(profileResponse.userId)

                // Save to local database
                val userEntity = UserEntity(
                    id = profileResponse.userId,
                    kakaoId = "", // Will be updated when fetching user details
                    name = profileResponse.profile.name,
                    role = profileResponse.profile.role,
                    industryCode = profileResponse.profile.industryCode,
                    growthGoals = profileResponse.profile.growthGoals,
                    photoUrl = profileResponse.profile.photoUrl,
                    onboardingCompletedAt = profileResponse.onboardingCompletedAt,
                    createdAt = ZonedDateTime.now().toString(),
                    lastLoginAt = ZonedDateTime.now().toString()
                )
                userDao.insertUser(userEntity)

                // Mark onboarding as completed
                authDataStore.saveOnboardingCompleted(true)

                Result.Success(Unit)
            } else {
                val errorCode = response.code()
                val errorMessage = when (errorCode) {
                    409 -> "이미 프로필이 등록되어 있습니다"
                    400 -> "입력 정보를 확인해 주세요"
                    else -> "프로필 등록에 실패했습니다"
                }
                Result.Error(errorMessage, errorCode.toString())
            }
        } catch (e: Exception) {
            Result.Error(
                message = "프로필 등록 중 오류가 발생했습니다: ${e.localizedMessage}",
                code = Constants.ErrorCodes.NETWORK_ERROR,
                throwable = e
            )
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val userId = authDataStore.getUserId()
            if (userId.isNullOrBlank()) {
                return Result.Success(null)
            }

            // Try to fetch from local database first
            val userEntity = userDao.getUserById(userId)
            if (userEntity != null) {
                val user = User(
                    id = userEntity.id,
                    kakaoId = userEntity.kakaoId,
                    createdAt = ZonedDateTime.parse(userEntity.createdAt),
                    lastLoginAt = ZonedDateTime.parse(userEntity.lastLoginAt),
                    status = UserStatus.ACTIVE
                )
                return Result.Success(user)
            }

            // Fetch from API if not in local database
            val response = userApi.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val userMeResponse = response.body()!!
                val userData = userMeResponse.user

                val user = User(
                    id = userData.id,
                    kakaoId = userData.kakaoId,
                    createdAt = ZonedDateTime.parse(userData.createdAt),
                    lastLoginAt = ZonedDateTime.parse(userData.lastLoginAt),
                    status = when (userData.status.uppercase()) {
                        "ACTIVE" -> UserStatus.ACTIVE
                        "INACTIVE" -> UserStatus.INACTIVE
                        "LOCKED" -> UserStatus.LOCKED
                        else -> UserStatus.ACTIVE
                    }
                )

                // Cache in local database
                if (userMeResponse.profile != null) {
                    val userEntity = UserEntity(
                        id = userData.id,
                        kakaoId = userData.kakaoId,
                        name = userMeResponse.profile.name,
                        role = userMeResponse.profile.role,
                        industryCode = userMeResponse.profile.industryCode,
                        growthGoals = userMeResponse.profile.growthGoals,
                        photoUrl = userMeResponse.profile.photoUrl,
                        onboardingCompletedAt = if (userMeResponse.flags.onboardingCompleted) {
                            ZonedDateTime.now().toString()
                        } else null,
                        createdAt = userData.createdAt,
                        lastLoginAt = userData.lastLoginAt
                    )
                    userDao.insertUser(userEntity)
                }

                Result.Success(user)
            } else {
                Result.Error("사용자 정보를 가져올 수 없습니다", response.code().toString())
            }
        } catch (e: Exception) {
            Result.Error(
                message = "사용자 정보 조회 중 오류가 발생했습니다: ${e.localizedMessage}",
                code = Constants.ErrorCodes.NETWORK_ERROR,
                throwable = e
            )
        }
    }

    override suspend fun updateOnboardingStatus(completed: Boolean) {
        authDataStore.saveOnboardingCompleted(completed)
    }

    override suspend fun getOnboardingStatus(): Boolean {
        return authDataStore.getOnboardingCompleted()
    }
}

package com.memoir.app.data.repository

import com.memoir.app.data.local.database.UserDao
import com.memoir.app.data.local.database.UserEntity
import com.memoir.app.data.local.datastore.AuthDataStore
import com.memoir.app.data.remote.api.UserApi
import com.memoir.app.data.remote.dto.ProfileData
import com.memoir.app.data.remote.dto.ProfileRequest
import com.memoir.app.data.remote.dto.ProfileResponse
import com.memoir.app.data.remote.dto.UserData
import com.memoir.app.data.remote.dto.UserFlags
import com.memoir.app.data.remote.dto.UserMeResponse
import com.memoir.app.domain.model.IndustryCode
import com.memoir.app.domain.model.UserProfile
import com.memoir.app.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.time.ZonedDateTime

/**
 * Unit tests for UserRepositoryImpl
 */
class UserRepositoryImplTest {

    private lateinit var repository: UserRepositoryImpl
    private lateinit var userApi: UserApi
    private lateinit var userDao: UserDao
    private lateinit var authDataStore: AuthDataStore

    @Before
    fun setup() {
        userApi = mockk()
        userDao = mockk(relaxed = true)
        authDataStore = mockk(relaxed = true)
        repository = UserRepositoryImpl(userApi, userDao, authDataStore)
    }

    @Test
    fun `saveProfile should save user data and return Success when API succeeds`() = runTest {
        // Given
        val profile = UserProfile(
            userId = "",
            name = "홍길동",
            role = "백엔드 개발자",
            industryCode = IndustryCode.STARTUP,
            growthGoals = "올해 목표는 FastAPI를 마스터하는 것입니다." + "x".repeat(100)
        )

        val profileResponse = ProfileResponse(
            userId = "user-123",
            profile = ProfileData(
                name = "홍길동",
                role = "백엔드 개발자",
                industryCode = "STARTUP",
                growthGoals = profile.growthGoals,
                photoUrl = null,
                phone = null,
                birthDate = null
            ),
            onboardingCompletedAt = ZonedDateTime.now().toString()
        )

        coEvery {
            userApi.submitProfile(any())
        } returns Response.success(profileResponse)

        // When
        val result = repository.saveProfile(profile)

        // Then
        assertTrue(result is Result.Success)

        coVerify {
            authDataStore.saveUserId("user-123")
            authDataStore.saveOnboardingCompleted(true)
            userDao.insertUser(any())
        }
    }

    @Test
    fun `saveProfile should return Error when API returns 409 conflict`() = runTest {
        // Given
        val profile = UserProfile(
            userId = "",
            name = "홍길동",
            role = "백엔드 개발자",
            industryCode = IndustryCode.STARTUP,
            growthGoals = "올해 목표는 FastAPI를 마스터하는 것입니다." + "x".repeat(100)
        )

        coEvery {
            userApi.submitProfile(any())
        } returns Response.error(409, "Conflict".toResponseBody())

        // When
        val result = repository.saveProfile(profile)

        // Then
        assertTrue(result is Result.Error)
        if (result is Result.Error) {
            assertTrue(result.message.contains("이미 프로필이 등록되어 있습니다"))
            assertEquals("409", result.code)
        }
    }

    @Test
    fun `saveProfile should return Error when network exception occurs`() = runTest {
        // Given
        val profile = UserProfile(
            userId = "",
            name = "홍길동",
            role = "백엔드 개발자",
            industryCode = IndustryCode.STARTUP,
            growthGoals = "올해 목표는 FastAPI를 마스터하는 것입니다." + "x".repeat(100)
        )

        coEvery {
            userApi.submitProfile(any())
        } throws Exception("Network error")

        // When
        val result = repository.saveProfile(profile)

        // Then
        assertTrue(result is Result.Error)
        if (result is Result.Error) {
            assertTrue(result.message.contains("프로필 등록 중 오류가 발생했습니다"))
        }
    }

    @Test
    fun `getCurrentUser should return null when userId is not saved`() = runTest {
        // Given
        coEvery { authDataStore.getUserId() } returns null

        // When
        val result = repository.getCurrentUser()

        // Then
        assertTrue(result is Result.Success)
        if (result is Result.Success) {
            assertNull(result.data)
        }
    }

    @Test
    fun `getCurrentUser should return cached user from database when available`() = runTest {
        // Given
        val userId = "user-123"
        val userEntity = UserEntity(
            id = userId,
            kakaoId = "kakao-123",
            name = "홍길동",
            role = "백엔드 개발자",
            industryCode = "STARTUP",
            growthGoals = "성장 목표",
            photoUrl = null,
            onboardingCompletedAt = null,
            createdAt = ZonedDateTime.now().toString(),
            lastLoginAt = ZonedDateTime.now().toString()
        )

        coEvery { authDataStore.getUserId() } returns userId
        coEvery { userDao.getUserById(userId) } returns userEntity

        // When
        val result = repository.getCurrentUser()

        // Then
        assertTrue(result is Result.Success)
        if (result is Result.Success) {
            assertNotNull(result.data)
            assertEquals(userId, result.data?.id)
            assertEquals("kakao-123", result.data?.kakaoId)
        }
    }

    @Test
    fun `getCurrentUser should fetch from API when not in database`() = runTest {
        // Given
        val userId = "user-123"
        val userMeResponse = UserMeResponse(
            user = UserData(
                id = userId,
                kakaoId = "kakao-123",
                status = "ACTIVE",
                createdAt = ZonedDateTime.now().toString(),
                lastLoginAt = ZonedDateTime.now().toString()
            ),
            profile = ProfileData(
                name = "홍길동",
                role = "백엔드 개발자",
                industryCode = "STARTUP",
                growthGoals = "성장 목표",
                photoUrl = null,
                phone = null,
                birthDate = null
            ),
            flags = UserFlags(onboardingCompleted = true)
        )

        coEvery { authDataStore.getUserId() } returns userId
        coEvery { userDao.getUserById(userId) } returns null
        coEvery { userApi.getCurrentUser() } returns Response.success(userMeResponse)

        // When
        val result = repository.getCurrentUser()

        // Then
        assertTrue(result is Result.Success)
        if (result is Result.Success) {
            assertNotNull(result.data)
            assertEquals(userId, result.data?.id)
        }

        coVerify { userDao.insertUser(any()) }
    }

    @Test
    fun `getCurrentUser should return Error when API fails`() = runTest {
        // Given
        val userId = "user-123"
        coEvery { authDataStore.getUserId() } returns userId
        coEvery { userDao.getUserById(userId) } returns null
        coEvery { userApi.getCurrentUser() } returns Response.error(
            404,
            "Not Found".toResponseBody()
        )

        // When
        val result = repository.getCurrentUser()

        // Then
        assertTrue(result is Result.Error)
        if (result is Result.Error) {
            assertEquals("404", result.code)
        }
    }

    @Test
    fun `updateOnboardingStatus should save to AuthDataStore`() = runTest {
        // When
        repository.updateOnboardingStatus(true)

        // Then
        coVerify { authDataStore.saveOnboardingCompleted(true) }
    }

    @Test
    fun `getOnboardingStatus should return value from AuthDataStore`() = runTest {
        // Given
        coEvery { authDataStore.getOnboardingCompleted() } returns true

        // When
        val result = repository.getOnboardingStatus()

        // Then
        assertTrue(result)
        coVerify { authDataStore.getOnboardingCompleted() }
    }
}

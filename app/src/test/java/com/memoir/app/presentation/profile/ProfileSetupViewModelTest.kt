package com.memoir.app.presentation.profile

import com.memoir.app.domain.model.IndustryCode
import com.memoir.app.domain.usecase.SaveProfileUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProfileSetupViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSetupViewModelTest {

    private lateinit var viewModel: ProfileSetupViewModel
    private lateinit var saveProfileUseCase: SaveProfileUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        saveProfileUseCase = mockk()
        viewModel = ProfileSetupViewModel(saveProfileUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateName with valid Korean name should clear error`() = runTest {
        // When
        viewModel.updateName("홍길동")

        // Then
        val state = viewModel.state.first()
        assertEquals("홍길동", state.name)
        assertNull(state.nameError)
    }

    @Test
    fun `updateName with invalid name should set error`() = runTest {
        // When - English name is not allowed
        viewModel.updateName("John")

        // Then
        val state = viewModel.state.first()
        assertEquals("John", state.name)
        assertNotNull(state.nameError)
    }

    @Test
    fun `updateName with too short name should set error`() = runTest {
        // When - Single character
        viewModel.updateName("김")

        // Then
        val state = viewModel.state.first()
        assertEquals("김", state.name)
        assertNotNull(state.nameError)
    }

    @Test
    fun `updateRole with valid role should clear error`() = runTest {
        // When
        viewModel.updateRole("백엔드 개발자")

        // Then
        val state = viewModel.state.first()
        assertEquals("백엔드 개발자", state.role)
        assertNull(state.roleError)
    }

    @Test
    fun `updateIndustry should update industry field`() = runTest {
        // When
        viewModel.updateIndustry(IndustryCode.STARTUP)

        // Then
        val state = viewModel.state.first()
        assertEquals(IndustryCode.STARTUP, state.industry)
        assertNull(state.industryError)
    }

    @Test
    fun `updateGrowthGoals with valid goals should clear error`() = runTest {
        // When - 100+ characters
        val goals = "올해 목표는 FastAPI와 Kotlin을 마스터하는 것입니다. " +
                "클린 아키텍처를 적용한 실무 프로젝트를 완성하고, " +
                "테스트 커버리지 80% 이상을 달성하겠습니다."
        viewModel.updateGrowthGoals(goals)

        // Then
        val state = viewModel.state.first()
        assertEquals(goals, state.growthGoals)
        assertNull(state.growthGoalsError)
    }

    @Test
    fun `updateGrowthGoals with too short goals should set error`() = runTest {
        // When - Less than 100 characters
        viewModel.updateGrowthGoals("짧은 목표")

        // Then
        val state = viewModel.state.first()
        assertEquals("짧은 목표", state.growthGoals)
        assertNotNull(state.growthGoalsError)
    }

    @Test
    fun `toggleTos should toggle acceptance`() = runTest {
        // When
        viewModel.toggleTos()
        val state1 = viewModel.state.first()
        assertTrue(state1.tosAccepted)

        viewModel.toggleTos()
        val state2 = viewModel.state.first()
        assertFalse(state2.tosAccepted)
    }

    @Test
    fun `togglePrivacy should toggle acceptance`() = runTest {
        // When
        viewModel.togglePrivacy()
        val state1 = viewModel.state.first()
        assertTrue(state1.privacyAccepted)

        viewModel.togglePrivacy()
        val state2 = viewModel.state.first()
        assertFalse(state2.privacyAccepted)
    }

    @Test
    fun `toggleDeposit should toggle acceptance`() = runTest {
        // When
        viewModel.toggleDeposit()
        val state1 = viewModel.state.first()
        assertTrue(state1.depositAccepted)

        viewModel.toggleDeposit()
        val state2 = viewModel.state.first()
        assertFalse(state2.depositAccepted)
    }

    @Test
    fun `form should be valid when all fields are correctly filled`() = runTest {
        // Given - Fill all fields correctly
        val goals = "올해 목표는 FastAPI와 Kotlin을 마스터하는 것입니다. " +
                "클린 아키텍처를 적용한 실무 프로젝트를 완성하고, " +
                "테스트 커버리지 80% 이상을 달성하겠습니다."

        // When
        viewModel.updateName("홍길동")
        viewModel.updateRole("백엔드 개발자")
        viewModel.updateIndustry(IndustryCode.STARTUP)
        viewModel.updateGrowthGoals(goals)
        viewModel.toggleTos()
        viewModel.togglePrivacy()
        viewModel.toggleDeposit()

        // Then
        val state = viewModel.state.first()
        assertTrue(state.isFormValid)
    }

    @Test
    fun `form should be invalid when name is missing`() = runTest {
        // Given
        val goals = "올해 목표는 FastAPI와 Kotlin을 마스터하는 것입니다. " +
                "클린 아키텍처를 적용한 실무 프로젝트를 완성하고, " +
                "테스트 커버리지 80% 이상을 달성하겠습니다."

        // When - Missing name
        viewModel.updateRole("백엔드 개발자")
        viewModel.updateIndustry(IndustryCode.STARTUP)
        viewModel.updateGrowthGoals(goals)
        viewModel.toggleTos()
        viewModel.togglePrivacy()
        viewModel.toggleDeposit()

        // Then
        val state = viewModel.state.first()
        assertFalse(state.isFormValid)
    }

    @Test
    fun `form should be invalid when terms are not accepted`() = runTest {
        // Given
        val goals = "올해 목표는 FastAPI와 Kotlin을 마스터하는 것입니다. " +
                "클린 아키텍처를 적용한 실무 프로젝트를 완성하고, " +
                "테스트 커버리지 80% 이상을 달성하겠습니다."

        // When - Not accepting TOS
        viewModel.updateName("홍길동")
        viewModel.updateRole("백엔드 개발자")
        viewModel.updateIndustry(IndustryCode.STARTUP)
        viewModel.updateGrowthGoals(goals)
        // NOT toggling TOS/Privacy/Deposit

        // Then
        val state = viewModel.state.first()
        assertFalse(state.isFormValid)
    }

    @Test
    fun `submitProfile should succeed when form is valid`() = runTest {
        // Given
        val goals = "올해 목표는 FastAPI와 Kotlin을 마스터하는 것입니다. " +
                "클린 아키텍처를 적용한 실무 프로젝트를 완성하고, " +
                "테스트 커버리지 80% 이상을 달성하겠습니다."

        coEvery { saveProfileUseCase(any()) } returns Result.Success(Unit)

        // Fill form
        viewModel.updateName("홍길동")
        viewModel.updateRole("백엔드 개발자")
        viewModel.updateIndustry(IndustryCode.STARTUP)
        viewModel.updateGrowthGoals(goals)
        viewModel.toggleTos()
        viewModel.togglePrivacy()
        viewModel.toggleDeposit()

        var onSuccessCalled = false

        // When
        viewModel.submitProfile { onSuccessCalled = true }
        advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertFalse(state.isSubmitting)
        assertNull(state.submitError)
        assertTrue(onSuccessCalled)
    }

    @Test
    fun `submitProfile should fail when usecase returns error`() = runTest {
        // Given
        val goals = "올해 목표는 FastAPI와 Kotlin을 마스터하는 것입니다. " +
                "클린 아키텍처를 적용한 실무 프로젝트를 완성하고, " +
                "테스트 커버리지 80% 이상을 달성하겠습니다."
        val errorMessage = "네트워크 오류"

        coEvery { saveProfileUseCase(any()) } returns Result.Error(errorMessage)

        // Fill form
        viewModel.updateName("홍길동")
        viewModel.updateRole("백엔드 개발자")
        viewModel.updateIndustry(IndustryCode.STARTUP)
        viewModel.updateGrowthGoals(goals)
        viewModel.toggleTos()
        viewModel.togglePrivacy()
        viewModel.toggleDeposit()

        var onSuccessCalled = false

        // When
        viewModel.submitProfile { onSuccessCalled = true }
        advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertFalse(state.isSubmitting)
        assertEquals(errorMessage, state.submitError)
        assertFalse(onSuccessCalled)
    }
}

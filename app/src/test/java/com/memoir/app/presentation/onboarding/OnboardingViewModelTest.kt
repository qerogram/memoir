package com.memoir.app.presentation.onboarding

import com.memoir.app.domain.repository.UserRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OnboardingViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var userRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
        viewModel = OnboardingViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial screen should be 1`() = runTest {
        // Then
        val screen = viewModel.currentScreen.first()
        assertEquals(1, screen)
    }

    @Test
    fun `nextScreen should increment screen number`() = runTest {
        // When
        viewModel.nextScreen()

        // Then
        val screen = viewModel.currentScreen.first()
        assertEquals(2, screen)
    }

    @Test
    fun `nextScreen should not exceed screen 4`() = runTest {
        // When - Navigate to screen 4
        viewModel.nextScreen() // 2
        viewModel.nextScreen() // 3
        viewModel.nextScreen() // 4
        val screen1 = viewModel.currentScreen.first()
        assertEquals(4, screen1)

        // When - Try to go beyond screen 4
        viewModel.nextScreen() // Still 4
        val screen2 = viewModel.currentScreen.first()

        // Then - Should stay at 4
        assertEquals(4, screen2)
    }

    @Test
    fun `completeOnboarding should set loading state and call callback`() = runTest {
        // Given
        var onCompleteCalled = false

        // When
        viewModel.completeOnboarding { onCompleteCalled = true }
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.first())
        assertTrue(onCompleteCalled)
    }

    @Test
    fun `initial loading state should be false`() = runTest {
        // Then
        val isLoading = viewModel.isLoading.first()
        assertFalse(isLoading)
    }
}

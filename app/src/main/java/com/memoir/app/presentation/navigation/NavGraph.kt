package com.memoir.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.memoir.app.presentation.auth.LoginScreen
import com.memoir.app.presentation.auth.LoginViewModel
import com.memoir.app.presentation.feed.CohortFeedScreen
import com.memoir.app.presentation.onboarding.OnboardingScreen
import com.memoir.app.presentation.onboarding.OnboardingViewModel
import com.memoir.app.presentation.profile.ProfileSetupScreen
import com.memoir.app.presentation.profile.ProfileSetupViewModel

/**
 * Navigation graph for Memoir app
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { onboardingCompleted ->
                    if (onboardingCompleted) {
                        navController.navigate(Screen.CohortFeed.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onOnboardingComplete = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            val viewModel: ProfileSetupViewModel = hiltViewModel()
            ProfileSetupScreen(
                viewModel = viewModel,
                onProfileSubmitted = {
                    navController.navigate(Screen.CohortFeed.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CohortFeed.route) {
            CohortFeedScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

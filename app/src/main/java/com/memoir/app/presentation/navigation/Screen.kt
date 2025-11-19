package com.memoir.app.presentation.navigation

/**
 * Sealed class representing app screens
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Onboarding : Screen("onboarding")
    data object ProfileSetup : Screen("profile_setup")
    data object CohortFeed : Screen("cohort_feed")
}

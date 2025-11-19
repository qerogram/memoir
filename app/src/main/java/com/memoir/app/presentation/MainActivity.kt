package com.memoir.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.memoir.app.data.local.datastore.AuthDataStore
import com.memoir.app.domain.usecase.CheckOnboardingStatusUseCase
import com.memoir.app.presentation.navigation.NavGraph
import com.memoir.app.presentation.navigation.Screen
import com.memoir.app.presentation.ui.theme.MemoirTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Main activity for Memoir app
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authDataStore: AuthDataStore

    @Inject
    lateinit var checkOnboardingStatusUseCase: CheckOnboardingStatusUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoirTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MemoirApp(
                        authDataStore = authDataStore,
                        checkOnboardingStatusUseCase = checkOnboardingStatusUseCase
                    )
                }
            }
        }
    }
}

@Composable
fun MemoirApp(
    authDataStore: AuthDataStore,
    checkOnboardingStatusUseCase: CheckOnboardingStatusUseCase
) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Determine start destination based on auth and onboarding status
    LaunchedEffect(Unit) {
        val isAuthenticated = authDataStore.isAuthenticatedFlow().firstOrNull() ?: false
        val onboardingCompleted = if (isAuthenticated) {
            checkOnboardingStatusUseCase()
        } else {
            false
        }

        startDestination = when {
            !isAuthenticated -> Screen.Login.route
            !onboardingCompleted -> Screen.Onboarding.route
            else -> Screen.CohortFeed.route
        }
    }

    // Show NavGraph once start destination is determined
    startDestination?.let { destination ->
        NavGraph(
            navController = navController,
            startDestination = destination
        )
    }
}

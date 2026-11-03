package com.focusblock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focusblock.ui.home.HomeScreen
import com.focusblock.ui.onboarding.OnboardingScreen
import com.focusblock.ui.onboarding.OnboardingViewModel
import com.focusblock.ui.onboarding.PlayDisclosureScreen
import com.focusblock.ui.profile.ProfileEditorScreen
import com.focusblock.ui.profile.ProfileEditorViewModel
import com.focusblock.ui.profile.ProfileWizardScreen
import com.focusblock.ui.profile.ProfileWizardViewModel
import com.focusblock.ui.settings.SettingsScreen
import com.focusblock.ui.settings.SettingsViewModel

sealed interface Screen {
    val route: String

    data object ONBOARDING : Screen {
        override val route: String = "onboarding"
    }

    data object PLAY_DISCLOSURE : Screen {
        override val route: String = "play_disclosure"
    }

    data object HOME : Screen {
        override val route: String = "home"
    }

    data object PROFILE_EDITOR : Screen {
        override val route: String = "profile_editor?profileId={profileId}"

        const val ARG_PROFILE_ID = "profileId"

        fun createRoute(profileId: Int? = null): String {
            return if (profileId == null) "profile_editor?profileId=-1"
            else "profile_editor?profileId=$profileId"
        }
    }

    data object PROFILE_WIZARD : Screen {
        override val route: String = "profile_wizard"
    }

    data object SETTINGS : Screen {
        override val route: String = "settings"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val appViewModel: AppNavigationViewModel = hiltViewModel()
    val startDestination by appViewModel.startDestination.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val start = startDestination
    if (start == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Screen.ONBOARDING.route) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val permissions by onboardingViewModel.permissions.collectAsStateWithLifecycle()

            OnboardingScreen(
                permissionsState = permissions,
                onRefreshPermissions = { onboardingViewModel.refreshPermissions() },
                onFixAccessibility = {
                    runCatching {
                        context.startActivity(onboardingViewModel.accessibilitySettingsIntent())
                    }
                },
                onFixDeviceAdmin = {
                    runCatching {
                        context.startActivity(onboardingViewModel.deviceAdminRequestIntent())
                    }
                },
                onComplete = {
                    appViewModel.completeOnboarding {
                        navController.navigate(Screen.HOME.route) {
                            popUpTo(Screen.ONBOARDING.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.PLAY_DISCLOSURE.route) {
            PlayDisclosureScreen(
                onComplete = {
                    appViewModel.acceptPlayDisclosure {
                        val next = if (appViewModel.onboardingCompleted.value) {
                            Screen.HOME.route
                        } else {
                            Screen.ONBOARDING.route
                        }
                        navController.navigate(next) {
                            popUpTo(Screen.PLAY_DISCLOSURE.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.HOME.route) {
            HomeScreen(
                onAddProfile = { navController.navigate(Screen.PROFILE_WIZARD.route) },
                onEditProfile = { profileId ->
                    navController.navigate(Screen.PROFILE_EDITOR.createRoute(profileId))
                },
                onOpenSettings = { navController.navigate(Screen.SETTINGS.route) }
            )
        }
        composable(Screen.PROFILE_WIZARD.route) {
            val viewModel: ProfileWizardViewModel = hiltViewModel()
            ProfileWizardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.PROFILE_EDITOR.route,
            arguments = listOf(
                navArgument(Screen.PROFILE_EDITOR.ARG_PROFILE_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { entry ->
            val viewModel: ProfileEditorViewModel = hiltViewModel()
            val profileId = entry.arguments?.getInt(Screen.PROFILE_EDITOR.ARG_PROFILE_ID) ?: -1
            LaunchedEffect(profileId) { viewModel.loadById(profileId) }
            ProfileEditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SETTINGS.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

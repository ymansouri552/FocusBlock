package com.focusblock.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focusblock.ui.home.HomeScreen
import com.focusblock.ui.onboarding.OnboardingScreen
import com.focusblock.ui.profile.ProfileEditorScreen
import com.focusblock.ui.profile.ProfileEditorViewModel
import com.focusblock.ui.settings.SettingsScreen

sealed interface Screen {
    val route: String

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

    data object SETTINGS : Screen {
        override val route: String = "settings"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.HOME.route) {
        composable(Screen.HOME.route) {
            HomeScreen(
                onAddProfile = { navController.navigate(Screen.PROFILE_EDITOR.createRoute()) },
                onEditProfile = { profileId ->
                    navController.navigate(Screen.PROFILE_EDITOR.createRoute(profileId))
                },
                onOpenSettings = { navController.navigate(Screen.SETTINGS.route) }
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
            val viewModel: com.focusblock.ui.settings.SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

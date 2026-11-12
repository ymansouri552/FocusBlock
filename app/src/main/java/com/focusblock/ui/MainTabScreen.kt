package com.focusblock.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.focusblock.ui.components.FocusBlockBottomBar
import com.focusblock.ui.components.MainTab
import com.focusblock.ui.home.HomeScreen
import com.focusblock.ui.stats.StatsScreen

@Composable
fun MainTabScreen(
    onAddProfile: () -> Unit,
    onEditProfile: (Int) -> Unit,
    onOpenSettings: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(MainTab.FOCUS) }

    when (currentTab) {
        MainTab.FOCUS -> {
            HomeScreen(
                onAddProfile = onAddProfile,
                onEditProfile = onEditProfile,
                onOpenSettings = onOpenSettings,
                onOpenStats = { currentTab = MainTab.STATISTICS },
                bottomBar = {
                    FocusBlockBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                }
            )
        }
        MainTab.STATISTICS -> {
            StatsScreen(
                onOpenSettings = onOpenSettings,
                onAddProfileForApp = { _ ->
                    onAddProfile()
                },
                bottomBar = {
                    FocusBlockBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                }
            )
        }
    }
}

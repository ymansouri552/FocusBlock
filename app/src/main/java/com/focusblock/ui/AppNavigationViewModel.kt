package com.focusblock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.BuildConfig
import com.focusblock.data.local.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            val onboardingDone = settingsDataStore.isOnboardingCompleted()
            _onboardingCompleted.value = onboardingDone
            val disclosureAccepted = settingsDataStore.isPlayDisclosureAccepted()

            _startDestination.value = when {
                BuildConfig.FLAVOR == "play" && !disclosureAccepted -> Screen.PLAY_DISCLOSURE.route
                !onboardingDone -> Screen.ONBOARDING.route
                else -> Screen.HOME.route
            }
        }
    }

    fun completeOnboarding(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
            _onboardingCompleted.value = true
            onDone()
        }
    }

    fun acceptPlayDisclosure(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            settingsDataStore.setPlayDisclosureAccepted(true)
            onDone()
        }
    }
}

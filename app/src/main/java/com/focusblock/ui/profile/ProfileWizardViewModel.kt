package com.focusblock.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.domain.model.InstalledApp
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ProfilePreset
import com.focusblock.domain.model.ProfilePresets
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.usecase.GetInstalledAppsUseCase
import com.focusblock.domain.usecase.SaveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileWizardUiState(
    val step: Int = 1,
    val selectedPresetId: String = "deep_work",
    val profileName: String = "Deep Work",
    val selectedPackages: Set<String> = emptySet(),
    val schedule: Schedule = ProfilePresets.presets.first().defaultSchedule,
    val strict: Boolean = false,
    val installedApps: List<InstalledApp> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class ProfileWizardViewModel @Inject constructor(
    private val saveProfileUseCase: SaveProfileUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileWizardUiState())
    val uiState: StateFlow<ProfileWizardUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = getInstalledAppsUseCase.getInstalledApps()
            _uiState.update { it.copy(installedApps = apps) }
        }
    }

    fun selectPreset(preset: ProfilePreset) {
        _uiState.update { current ->
            current.copy(
                selectedPresetId = preset.id,
                profileName = if (preset.defaultName.isNotBlank()) preset.defaultName else current.profileName,
                schedule = preset.defaultSchedule,
                strict = preset.defaultStrict,
                error = null
            )
        }
    }

    fun setProfileName(name: String) {
        _uiState.update { it.copy(profileName = name, error = null) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun togglePackage(packageName: String) {
        _uiState.update { current ->
            val updated = if (current.selectedPackages.contains(packageName)) {
                current.selectedPackages - packageName
            } else {
                current.selectedPackages + packageName
            }
            current.copy(selectedPackages = updated, error = null)
        }
    }

    fun selectAllFilteredApps(filteredPackages: List<String>) {
        _uiState.update { current ->
            current.copy(selectedPackages = current.selectedPackages + filteredPackages)
        }
    }

    fun clearAllSelectedPackages() {
        _uiState.update { it.copy(selectedPackages = emptySet()) }
    }

    fun setSchedule(schedule: Schedule) {
        _uiState.update { it.copy(schedule = schedule, error = null) }
    }

    fun toggleStrict(strict: Boolean) {
        _uiState.update { it.copy(strict = strict) }
    }

    fun nextStep(): Boolean {
        val state = _uiState.value
        when (state.step) {
            1 -> {
                if (state.profileName.trim().isBlank()) {
                    _uiState.update { it.copy(error = "Please enter a profile name") }
                    return false
                }
            }
            2 -> {
                if (state.selectedPackages.isEmpty()) {
                    _uiState.update { it.copy(error = "Please select at least one app to block") }
                    return false
                }
            }
            3 -> {
                if (state.schedule.daysOfWeek.isEmpty()) {
                    _uiState.update { it.copy(error = "Please select at least one active day") }
                    return false
                }
            }
        }
        if (state.step < 4) {
            _uiState.update { it.copy(step = it.step + 1, error = null) }
            return true
        }
        return false
    }

    fun prevStep(): Boolean {
        if (_uiState.value.step > 1) {
            _uiState.update { it.copy(step = it.step - 1, error = null) }
            return true
        }
        return false
    }

    fun goToStep(step: Int) {
        if (step in 1..4) {
            _uiState.update { it.copy(step = step, error = null) }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return
        if (state.profileName.trim().isBlank()) {
            _uiState.update { it.copy(error = "Please enter a profile name") }
            return
        }
        if (state.selectedPackages.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one app") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val profile = Profile(
                    id = 0,
                    name = state.profileName.trim(),
                    blockedApps = state.selectedPackages,
                    strict = state.strict,
                    enabled = true,
                    schedules = listOf(state.schedule)
                )
                saveProfileUseCase.invoke(profile)
                onSuccess()
            } catch (t: Throwable) {
                _uiState.update { it.copy(isSaving = false, error = t.message ?: "Failed to save profile") }
            }
        }
    }
}

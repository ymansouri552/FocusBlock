package com.focusblock.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.domain.model.InstalledApp
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ProfilePreset
import com.focusblock.domain.model.ProfilePresets
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.domain.model.ScheduleType
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

    fun setScheduleMode(mode: ScheduleMode) {
        _uiState.update { current ->
            val updatedSchedule = if (mode == ScheduleMode.BLOCK) {
                current.schedule.copy(
                    mode = ScheduleMode.BLOCK,
                    type = ScheduleType.RECURRING,
                    startTime = current.schedule.startTime ?: java.time.LocalTime.of(9, 0),
                    endTime = current.schedule.endTime ?: java.time.LocalTime.of(17, 0)
                )
            } else {
                current.schedule.copy(
                    mode = ScheduleMode.USAGE_LIMIT,
                    type = if (current.schedule.type == ScheduleType.HOURLY || current.schedule.type == ScheduleType.DAILY || current.schedule.type == ScheduleType.RECURRING) {
                        current.schedule.type
                    } else {
                        ScheduleType.DAILY
                    },
                    usageLimitMinutes = if (current.schedule.usageLimitMinutes > 0) current.schedule.usageLimitMinutes else 30
                )
            }
            current.copy(schedule = updatedSchedule, error = null)
        }
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
                // Mode selection is always valid (defaults to preset mode)
            }
            4 -> {
                if (state.schedule.mode == ScheduleMode.BLOCK) {
                    if (state.schedule.daysOfWeek.isEmpty()) {
                        _uiState.update { it.copy(error = "Please select at least one active day") }
                        return false
                    }
                } else {
                    if (state.schedule.usageLimitMinutes <= 0) {
                        _uiState.update { it.copy(error = "Please specify an allowance of at least 1 minute") }
                        return false
                    }
                    if (state.schedule.daysOfWeek.isEmpty()) {
                        _uiState.update { it.copy(error = "Please select at least one active day") }
                        return false
                    }
                }
            }
        }
        if (state.step < 5) {
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
        if (step in 1..5) {
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

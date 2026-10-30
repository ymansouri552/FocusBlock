package com.focusblock.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import com.focusblock.domain.usecase.BlockedAppsUseCase
import com.focusblock.domain.usecase.GetProfilesUseCase
import com.focusblock.domain.usecase.QuickBlockUseCase
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.domain.usecase.SystemPermissionChecker
import com.focusblock.domain.usecase.SystemPermissionsState
import com.focusblock.domain.usecase.ToggleProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val blockedAppsUseCase: BlockedAppsUseCase,
    private val toggleProfileUseCase: ToggleProfileUseCase,
    private val quickBlockUseCase: QuickBlockUseCase,
    private val settingsDataStore: SettingsDataStore,
    val permissionChecker: SystemPermissionChecker
) : ViewModel() {

    val profiles: StateFlow<List<Profile>> = getProfilesUseCase.invoke()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val blockedPackages: StateFlow<Set<String>> = blockedAppsUseCase.blockedPackagesNow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val strictModeState: StateFlow<StrictModeState> = settingsDataStore.strictModeState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StrictModeState())

    private val _permissionsState = MutableStateFlow(permissionChecker.checkPermissions())
    val permissionsState: StateFlow<SystemPermissionsState> = _permissionsState.asStateFlow()

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = false)
            settingsDataStore.strictModeState.collect { state ->
                StrictModeRules.independentState = state
            }
        }
    }

    fun refreshPermissions() {
        _permissionsState.value = permissionChecker.checkPermissions()
    }

    fun toggleProfile(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            val result = toggleProfileUseCase.invoke(id, enabled)
            when (result) {
                is ToggleProfileUseCase.ToggleResult.Failure -> {
                    _ui.value = _ui.value.copy(error = result.reason)
                }
                is ToggleProfileUseCase.ToggleResult.Success -> {
                    _ui.value = _ui.value.copy(error = null)
                }
            }
        }
    }

    fun armStrictMode(termType: StrictTermType, customEndInstant: Instant? = null) {
        viewModelScope.launch {
            try {
                settingsDataStore.armStrictMode(termType, customEndInstant)
                _ui.value = _ui.value.copy(error = null)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.localizedMessage ?: "Failed to arm strict mode")
            }
        }
    }

    fun disarmStrictMode() {
        viewModelScope.launch {
            val current = settingsDataStore.getStrictModeState()
            val now = Instant.now()
            if (!current.isArmed) {
                return@launch
            }
            if (current.endInstant != null && now.isBefore(current.endInstant)) {
                _ui.value = _ui.value.copy(error = "Cannot disarm: Strict Mode term has not expired yet.")
                return@launch
            }
            settingsDataStore.disarmStrictMode()
            _ui.value = _ui.value.copy(error = null)
        }
    }

    fun startQuickBlock(durationMinutes: Int, apps: Set<String>) {
        viewModelScope.launch {
            try {
                quickBlockUseCase.startQuickBlock(
                    profileName = "Quick Block (${durationMinutes}m)",
                    packageNames = apps,
                    durationMinutes = durationMinutes
                )
                _ui.value = _ui.value.copy(error = null)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.localizedMessage ?: "Failed to start quick block")
            }
        }
    }

    fun clearError() {
        _ui.value = _ui.value.copy(error = null)
    }

    data class HomeUiState(
        val loading: Boolean = false,
        val error: String? = null
    )
}

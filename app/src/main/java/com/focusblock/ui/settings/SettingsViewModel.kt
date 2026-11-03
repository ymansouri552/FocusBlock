package com.focusblock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.data.local.datastore.SettingsKeys
import com.focusblock.data.local.datastore.StrictLevel
import com.focusblock.domain.usecase.EmergencyUnlockStatus
import com.focusblock.domain.usecase.EmergencyUnlockUseCase
import com.focusblock.domain.usecase.TrustedClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: SettingsDataStore,
    private val trustedClock: TrustedClock,
    private val emergencyUnlockUseCase: EmergencyUnlockUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val level = dataStore.flowString(SettingsKeys.strictLevel, StrictLevel.AGGRESSIVE.value).first()
            val blockBrowsers = dataStore.flowBoolean(SettingsKeys.blockBrowsersInStrict, false).first()
            val emergencyEnabled = dataStore.getEmergencyUnlockEnabled()
            val emergencyMins = dataStore.getEmergencyUnlockMinutes()
            val theme = dataStore.themeMode.first()
            val strictState = dataStore.getStrictModeState()
            val isStrict = strictState.isEffectivelyActive(trustedClock.now())

            _ui.value = SettingsUiState(
                strictLevel = level,
                blockBrowsersInStrict = blockBrowsers,
                emergencyUnlockEnabled = emergencyEnabled,
                emergencyUnlockMinutes = emergencyMins,
                themeMode = theme,
                isStrictActive = isStrict,
                emergencyUnlockStatus = emergencyUnlockUseCase.getStatus(trustedClock.now())
            )

            dataStore.strictModeState.collect { state ->
                val active = state.isEffectivelyActive(trustedClock.now())
                _ui.value = _ui.value.copy(
                    isStrictActive = active,
                    emergencyUnlockStatus = emergencyUnlockUseCase.getStatus(trustedClock.now())
                )
            }
        }
    }

    /** Re-evaluates the strict term and emergency countdown; called by the screen once per second. */
    fun refreshRuntimeState() {
        viewModelScope.launch {
            refreshRuntimeStateNow()
        }
    }

    private suspend fun refreshRuntimeStateNow() {
        val now = trustedClock.now()
        val strictState = dataStore.getStrictModeState()
        _ui.value = _ui.value.copy(
            isStrictActive = strictState.isEffectivelyActive(now),
            emergencyUnlockStatus = emergencyUnlockUseCase.getStatus(now)
        )
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStore.setThemeMode(mode)
            _ui.value = _ui.value.copy(themeMode = mode)
        }
    }

    fun setBlockBrowsersInStrict(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.writeBoolean(SettingsKeys.blockBrowsersInStrict, enabled)
            _ui.value = _ui.value.copy(blockBrowsersInStrict = enabled)
        }
    }

    fun setStrictLevel(level: String) {
        viewModelScope.launch {
            dataStore.writeString(SettingsKeys.strictLevel, level)
            _ui.value = _ui.value.copy(strictLevel = level)
        }
    }

    fun setEmergencyUnlockEnabled(enabled: Boolean) {
        if (_ui.value.isStrictActive) return
        viewModelScope.launch {
            // Configuration changes only happen outside strict mode, so any stored request is stale.
            emergencyUnlockUseCase.cancelUnlock()
            dataStore.setEmergencyUnlockEnabled(enabled)
            _ui.value = _ui.value.copy(
                emergencyUnlockEnabled = enabled,
                emergencyUnlockStatus = emergencyUnlockUseCase.getStatus(trustedClock.now())
            )
        }
    }

    fun setEmergencyUnlockMinutes(minutes: Int) {
        if (_ui.value.isStrictActive) return
        viewModelScope.launch {
            emergencyUnlockUseCase.cancelUnlock()
            dataStore.setEmergencyUnlockMinutes(minutes)
            _ui.value = _ui.value.copy(emergencyUnlockMinutes = minutes)
        }
    }

    fun requestEmergencyUnlock() {
        viewModelScope.launch {
            if (!_ui.value.isStrictActive) return@launch
            emergencyUnlockUseCase.requestUnlock()
            refreshRuntimeStateNow()
        }
    }

    fun cancelEmergencyUnlock() {
        viewModelScope.launch {
            emergencyUnlockUseCase.cancelUnlock()
            refreshRuntimeStateNow()
        }
    }

    fun confirmEmergencyUnlock() {
        viewModelScope.launch {
            emergencyUnlockUseCase.confirmUnlock(trustedClock.now())
            refreshRuntimeStateNow()
        }
    }

    data class SettingsUiState(
        val strictLevel: String = StrictLevel.AGGRESSIVE.value,
        val blockBrowsersInStrict: Boolean = false,
        val emergencyUnlockEnabled: Boolean = false,
        val emergencyUnlockMinutes: Int = 15,
        val themeMode: String = "DARK",
        val isStrictActive: Boolean = false,
        val emergencyUnlockStatus: EmergencyUnlockStatus = EmergencyUnlockStatus.Disabled
    )
}

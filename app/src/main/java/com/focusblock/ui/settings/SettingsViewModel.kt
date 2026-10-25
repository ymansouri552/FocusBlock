package com.focusblock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.data.local.datastore.SettingsKeys
import com.focusblock.data.local.datastore.StrictLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.flowString(SettingsKeys.strictLevel, StrictLevel.TARGETED.value).first().let { level ->
                _ui.value = _ui.value.copy(strictLevel = level)
            }
        }
    }

    fun setStrictLevel(level: String) {
        viewModelScope.launch {
            dataStore.writeString(SettingsKeys.strictLevel, level)
            _ui.value = _ui.value.copy(strictLevel = level)
        }
    }

    data class SettingsUiState(
        val strictLevel: String = StrictLevel.TARGETED.value
    )
}
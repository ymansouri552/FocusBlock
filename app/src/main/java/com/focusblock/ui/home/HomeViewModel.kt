package com.focusblock.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.domain.usecase.BlockedAppsUseCase
import com.focusblock.domain.usecase.GetProfilesUseCase
import com.focusblock.domain.usecase.ToggleProfileUseCase
import com.focusblock.domain.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val blockedAppsUseCase: BlockedAppsUseCase,
    private val toggleProfileUseCase: ToggleProfileUseCase
) : ViewModel() {

    val profiles: StateFlow<List<Profile>> = getProfilesUseCase.invoke()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val blockedPackages: StateFlow<Set<String>> = blockedAppsUseCase.blockedPackagesNow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true)
        }
    }

    fun toggleProfile(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            val result = toggleProfileUseCase.invoke(id, enabled)
            when (result) {
                is ToggleProfileUseCase.ToggleResult.Failure -> {
                    _ui.value = _ui.value.copy(error = result.reason)
                }
                is ToggleProfileUseCase.ToggleResult.Success -> {
                    // Success - no error
                    _ui.value = _ui.value.copy(error = null)
                }
            }
        }
    }

    data class HomeUiState(
        val loading: Boolean = false,
        val error: String? = null
    )
}
package com.focusblock.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.domain.model.Profile
import com.focusblock.domain.repository.ProfileRepository
import com.focusblock.domain.usecase.DeleteProfileUseCase
import com.focusblock.domain.usecase.SaveProfileUseCase
import com.focusblock.domain.usecase.StrictModeRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    private val saveProfileUseCase: SaveProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileEditorUiState())
    val ui: StateFlow<ProfileEditorUiState> = _ui.asStateFlow()

    /** Snapshot of the profile as loaded from the database (id == 0 when new). */
    private var original: Profile = Profile()
    private var loadedId: Int = Int.MIN_VALUE

    var profile: Profile by mutableStateOf(Profile())
        private set

    fun load(profile: Profile) {
        original = profile
        this.profile = profile
    }

    fun loadById(id: Int) {
        if (id == loadedId) return
        loadedId = id
        viewModelScope.launch {
            val loaded = if (id < 0) {
                Profile()
            } else {
                profileRepository.getProfileById(id).first() ?: Profile()
            }
            original = loaded
            profile = loaded
        }
    }

    fun setName(name: String) {
        profile = profile.copy(name = name)
    }

    fun toggleStrict(enabled: Boolean) {
        profile = profile.copy(strict = enabled)
    }

    fun addApps(apps: Set<String>) {
        profile = profile.copy(blockedApps = profile.blockedApps + apps)
    }

    fun removeApp(app: String) {
        profile = profile.copy(blockedApps = profile.blockedApps - app)
    }

    fun canSave(now: Instant = Instant.now()): StrictModeRules.EditResult {
        return saveProfileUseCase.canSave(original, profile, now)
    }

    fun save() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            val check = canSave()
            if (!check.allowed) {
                _ui.value = _ui.value.copy(saving = false, error = check.reason)
                return@launch
            }
            if (profile.id == 0) {
                saveProfileUseCase.invoke(profile)
            } else {
                saveProfileUseCase.update(profile)
            }
            original = profile
            _ui.value = _ui.value.copy(saving = false)
        }
    }

    fun delete(onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            val canDelete = StrictModeRules.canDelete(original, Instant.now())
            if (!canDelete.allowed) {
                _ui.value = _ui.value.copy(saving = false, error = canDelete.reason)
                return@launch
            }
            deleteProfileUseCase.invoke(original)
            _ui.value = _ui.value.copy(saving = false)
            onDeleted()
        }
    }

    data class ProfileEditorUiState(
        val appPickerOpen: Boolean = false,
        val scheduleOpen: Boolean = false,
        val strictConfirmOpen: Boolean = false,
        val saving: Boolean = false,
        val error: String? = null
    )
}

package com.focusblock.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.domain.model.InstalledApp
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.repository.ProfileRepository
import com.focusblock.domain.usecase.DeleteProfileUseCase
import com.focusblock.domain.usecase.GetInstalledAppsUseCase
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
    private val profileRepository: ProfileRepository,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileEditorUiState())
    val ui: StateFlow<ProfileEditorUiState> = _ui.asStateFlow()

    /** Snapshot of the profile as loaded from the database (id == 0 when new). */
    private var original: Profile = Profile()
    private var loadedId: Int = Int.MIN_VALUE

    var profile: Profile by mutableStateOf(Profile())
        private set

    val originalBlockedApps: Set<String>
        get() = original.blockedApps

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

    fun toggleEnabled(enabled: Boolean) {
        profile = profile.copy(enabled = enabled)
    }

    fun toggleStrict(enabled: Boolean) {
        profile = profile.copy(strict = enabled)
    }

    fun setStrictConfirmOpen(open: Boolean) {
        _ui.value = _ui.value.copy(strictConfirmOpen = open)
    }

    fun addApps(apps: Set<String>) {
        profile = profile.copy(blockedApps = profile.blockedApps + apps)
    }

    fun removeApp(app: String) {
        profile = profile.copy(blockedApps = profile.blockedApps - app)
    }

    // ── App picker ──────────────────────────────────────────────────────

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _selectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackages: StateFlow<Set<String>> = _selectedPackages.asStateFlow()

    fun setAppPickerOpen(open: Boolean) {
        _ui.value = _ui.value.copy(appPickerOpen = open)
        if (open) {
            viewModelScope.launch {
                _selectedPackages.value = profile.blockedApps
                _installedApps.value = getInstalledAppsUseCase.getInstalledApps()
            }
        } else {
            _selectedPackages.value = emptySet()
        }
    }

    fun toggleSelectedPackage(pkg: String) {
        val current = _selectedPackages.value
        _selectedPackages.value = if (current.contains(pkg)) {
            current - pkg
        } else {
            current + pkg
        }
    }

    fun confirmAppSelection() {
        val newBlocked = if (StrictModeRules.isStrictActive(original)) {
            original.blockedApps + _selectedPackages.value
        } else {
            _selectedPackages.value
        }
        profile = profile.copy(blockedApps = newBlocked)
        setAppPickerOpen(false)
    }

    // ── Schedule editor ─────────────────────────────────────────────────

    private val _editingSchedule = MutableStateFlow<Schedule?>(null)
    val editingSchedule: StateFlow<Schedule?> = _editingSchedule.asStateFlow()

    private val _scheduleEditorOpen = MutableStateFlow(false)
    val scheduleEditorOpen: StateFlow<Boolean> = _scheduleEditorOpen.asStateFlow()

    private val _scheduleEditorIndex = MutableStateFlow<Int?>(null)
    val scheduleEditorIndex: StateFlow<Int?> = _scheduleEditorIndex.asStateFlow()

    fun setScheduleEditorOpen(open: Boolean, schedule: Schedule? = null, index: Int? = null) {
        _scheduleEditorOpen.value = open
        _editingSchedule.value = schedule
        _scheduleEditorIndex.value = index
        _ui.value = _ui.value.copy(scheduleOpen = open)
    }

    fun setEditingSchedule(schedule: Schedule) {
        _editingSchedule.value = schedule
    }

    fun addSchedule(schedule: Schedule) {
        val current = profile.schedules.toMutableList()
        current += schedule
        profile = profile.copy(schedules = current)
    }

    fun updateSchedule(index: Int, schedule: Schedule) {
        val current = profile.schedules.toMutableList()
        if (index >= 0 && index < current.size) {
            current[index] = schedule
            profile = profile.copy(schedules = current)
        }
    }

    fun removeSchedule(index: Int) {
        val current = profile.schedules.toMutableList()
        if (index >= 0 && index < current.size) {
            current.removeAt(index)
            profile = profile.copy(schedules = current)
        }
        setScheduleEditorOpen(false)
    }

    fun canSave(now: Instant = Instant.now()): StrictModeRules.EditResult {
        return saveProfileUseCase.canSave(original, profile, now)
    }

    fun save(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            val trimmedName = profile.name.trim()
            if (trimmedName.isBlank()) {
                _ui.value = _ui.value.copy(saving = false, error = "Profile name cannot be empty")
                return@launch
            }
            profile = profile.copy(name = trimmedName)
            val check = canSave()
            if (!check.allowed) {
                _ui.value = _ui.value.copy(saving = false, error = check.reason)
                return@launch
            }
            if (profile.id == 0) {
                val newId = saveProfileUseCase.invoke(profile)
                profile = profile.copy(id = newId.toInt())
            } else {
                saveProfileUseCase.update(profile)
            }
            original = profile
            _ui.value = _ui.value.copy(saving = false)
            onSuccess()
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

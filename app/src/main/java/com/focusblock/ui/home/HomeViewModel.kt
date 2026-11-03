package com.focusblock.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import com.focusblock.domain.repository.BlockLogRepository
import com.focusblock.domain.usecase.BlockedAppsUseCase
import com.focusblock.domain.usecase.EmergencyUnlockUseCase
import com.focusblock.domain.usecase.GetProfilesUseCase
import com.focusblock.domain.usecase.QuickBlockUseCase
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.domain.usecase.SystemPermissionChecker
import com.focusblock.domain.usecase.SystemPermissionsState
import com.focusblock.domain.usecase.ToggleProfileUseCase
import com.focusblock.domain.usecase.TrustedClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.focusblock.domain.model.BlockLog
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class EmptyBlockLogRepository : BlockLogRepository {
    override fun insert(log: BlockLog): Long = 0L
    override fun getLogsForDay(day: Long) = flowOf(emptyList<BlockLog>())
    override fun getLogsForPackage(packageName: String) = flowOf(emptyList<BlockLog>())
    override fun getAllLogs() = flowOf(emptyList<BlockLog>())
    override fun deleteLogsOlderThan(thresholdMs: Long): Int = 0
    override fun clearAllLogs(): Int = 0
}

data class FocusStats(
    val focusedHoursThisWeek: Float = 0f,
    val dailyHours: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val streakDays: Int = 0,
    val blockedResisted: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val blockedAppsUseCase: BlockedAppsUseCase,
    private val toggleProfileUseCase: ToggleProfileUseCase,
    private val quickBlockUseCase: QuickBlockUseCase,
    private val settingsDataStore: SettingsDataStore,
    private val emergencyUnlockUseCase: EmergencyUnlockUseCase,
    private val trustedClock: TrustedClock,
    private val blockLogRepository: BlockLogRepository = EmptyBlockLogRepository(),
    val permissionChecker: SystemPermissionChecker
) : ViewModel() {

    val profiles: StateFlow<List<Profile>> = getProfilesUseCase.invoke()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val blockedPackages: StateFlow<Set<String>> = blockedAppsUseCase.blockedPackagesNow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val strictModeState: StateFlow<StrictModeState> = settingsDataStore.strictModeState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StrictModeState())

    // ─────────────────────────────────────────────────────────────────────────────
    // Minimal local tracking for stats strip: focused hours, streak, and blocked attempts
    // ─────────────────────────────────────────────────────────────────────────────
    val focusStats: StateFlow<FocusStats> = blockLogRepository.getAllLogs().map { logs ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val logsByDate = logs.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
        }

        val past7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val dailyHours = past7Days.map { date ->
            val count = logsByDate[date]?.size ?: 0
            if (count > 0) (count * 0.75f).coerceIn(0.5f, 8.0f) else 0f
        }
        val totalWeekHours = dailyHours.sum()

        var streak = 0
        var checkDate = today
        if (!logsByDate.containsKey(today)) {
            checkDate = today.minusDays(1)
        }
        while (logsByDate.containsKey(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        FocusStats(
            focusedHoursThisWeek = totalWeekHours,
            dailyHours = dailyHours,
            streakDays = streak,
            blockedResisted = logs.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusStats())

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

    /** Tamper-resistant wall clock used for all UI countdowns and strict checks. */
    suspend fun now(): Instant = trustedClock.now()

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
            val now = trustedClock.now()
            if (!current.isArmed) {
                return@launch
            }
            if (current.termType == StrictTermType.ONGOING) {
                val unlockSuccess = emergencyUnlockUseCase.confirmUnlock(now)
                if (!unlockSuccess) {
                    _ui.value = _ui.value.copy(
                        error = "Ongoing Strict Mode has no automatic expiration. Use Emergency Unlock in Settings to disarm."
                    )
                    return@launch
                }
                _ui.value = _ui.value.copy(error = null)
                return@launch
            }
            if (current.endInstant != null && now.isBefore(current.endInstant)) {
                val unlockSuccess = emergencyUnlockUseCase.confirmUnlock(now)
                if (!unlockSuccess) {
                    _ui.value = _ui.value.copy(error = "Cannot disarm: Strict Mode term has not expired yet.")
                    return@launch
                }
                _ui.value = _ui.value.copy(error = null)
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

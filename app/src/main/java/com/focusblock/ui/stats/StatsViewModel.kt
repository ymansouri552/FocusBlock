package com.focusblock.ui.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusblock.domain.model.AppSortOrder
import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.StatsPeriod
import com.focusblock.domain.model.UsageStatsOverview
import com.focusblock.domain.usecase.GetUsageStatsUseCase
import com.focusblock.domain.usecase.SystemPermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.DAILY,
    val selectedDate: LocalDate = LocalDate.now(),
    val sortOrder: AppSortOrder = AppSortOrder.MOST_USED,
    val searchQuery: String = "",
    val overview: UsageStatsOverview = UsageStatsOverview(),
    val selectedAppForDetails: AppUsageItem? = null,
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false
) {
    val canGoForward: Boolean
        get() {
            val today = LocalDate.now(ZoneId.systemDefault())
            return when (period) {
                StatsPeriod.DAILY -> selectedDate < today
                StatsPeriod.WEEKLY -> {
                    val currentWeekMonday = today.with(DayOfWeek.MONDAY)
                    selectedDate.with(DayOfWeek.MONDAY) < currentWeekMonday
                }
            }
        }

    val isCurrentPeriod: Boolean
        get() {
            val today = LocalDate.now(ZoneId.systemDefault())
            return when (period) {
                StatsPeriod.DAILY -> selectedDate == today
                StatsPeriod.WEEKLY -> selectedDate.with(DayOfWeek.MONDAY) == today.with(DayOfWeek.MONDAY)
            }
        }
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getUsageStatsUseCase: GetUsageStatsUseCase,
    private val systemPermissionChecker: SystemPermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val perm = systemPermissionChecker.isUsageStatsGranted()
        _uiState.update { it.copy(hasPermission = perm) }
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val current = _uiState.value
            val overview = getUsageStatsUseCase.getStats(
                period = current.period,
                targetDate = current.selectedDate,
                sortOrder = current.sortOrder,
                searchQuery = current.searchQuery
            )
            _uiState.update {
                it.copy(
                    overview = overview,
                    hasPermission = overview.hasPermission,
                    isLoading = false
                )
            }
        }
    }

    fun setPeriod(period: StatsPeriod) {
        if (_uiState.value.period != period) {
            _uiState.update { it.copy(period = period) }
            loadStats()
        }
    }

    fun previousPeriod() {
        _uiState.update { state ->
            val prevDate = when (state.period) {
                StatsPeriod.DAILY -> state.selectedDate.minusDays(1)
                StatsPeriod.WEEKLY -> state.selectedDate.minusWeeks(1)
            }
            state.copy(selectedDate = prevDate)
        }
        loadStats()
    }

    fun nextPeriod() {
        val today = LocalDate.now(ZoneId.systemDefault())
        val current = _uiState.value
        val canAdvance = when (current.period) {
            StatsPeriod.DAILY -> current.selectedDate < today
            StatsPeriod.WEEKLY -> current.selectedDate.with(DayOfWeek.MONDAY) < today.with(DayOfWeek.MONDAY)
        }
        if (canAdvance) {
            _uiState.update { state ->
                val nextDate = when (state.period) {
                    StatsPeriod.DAILY -> state.selectedDate.plusDays(1)
                    StatsPeriod.WEEKLY -> state.selectedDate.plusWeeks(1)
                }
                state.copy(selectedDate = nextDate)
            }
            loadStats()
        }
    }

    fun goToCurrent() {
        _uiState.update { it.copy(selectedDate = LocalDate.now(ZoneId.systemDefault())) }
        loadStats()
    }

    fun selectDay(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                period = StatsPeriod.DAILY
            )
        }
        loadStats()
    }

    fun setSortOrder(order: AppSortOrder) {
        if (_uiState.value.sortOrder != order) {
            _uiState.update { it.copy(sortOrder = order) }
            loadStats()
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadStats()
    }

    fun selectAppForDetails(app: AppUsageItem?) {
        _uiState.update { it.copy(selectedAppForDetails = app) }
    }

    fun launchUsageSettings(context: Context) {
        systemPermissionChecker.launchUsageAccessSettings(context)
    }
}

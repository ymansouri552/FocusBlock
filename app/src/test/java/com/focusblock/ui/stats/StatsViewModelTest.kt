package com.focusblock.ui.stats

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.focusblock.domain.model.AppSortOrder
import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.StatsPeriod
import com.focusblock.domain.model.UsageStatsOverview
import com.focusblock.domain.repository.UsageStatsRepository
import com.focusblock.domain.usecase.GetUsageStatsUseCase
import com.focusblock.domain.usecase.SystemPermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var permissionChecker: SystemPermissionChecker
    private lateinit var fakeRepository: FakeUsageStatsRepository
    private lateinit var useCase: GetUsageStatsUseCase
    private lateinit var viewModel: StatsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        permissionChecker = SystemPermissionChecker(context)
        fakeRepository = FakeUsageStatsRepository()
        useCase = GetUsageStatsUseCase(fakeRepository)
        viewModel = StatsViewModel(useCase, permissionChecker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_defaultsToDailyAndLoads() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(StatsPeriod.DAILY, state.period)
        assertEquals(LocalDate.now(), state.selectedDate)
        assertEquals(AppSortOrder.MOST_USED, state.sortOrder)
        assertEquals("", state.searchQuery)
        assertFalse(state.isLoading)
        assertNull(state.selectedAppForDetails)
    }

    @Test
    fun setPeriod_updatesPeriodAndReloads() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPeriod(StatsPeriod.WEEKLY)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(StatsPeriod.WEEKLY, viewModel.uiState.value.period)
    }

    @Test
    fun previousPeriod_decrementsDate() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val initialDate = viewModel.uiState.value.selectedDate

        viewModel.previousPeriod()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(initialDate.minusDays(1), viewModel.uiState.value.selectedDate)
        assertTrue(viewModel.uiState.value.canGoForward)
    }

    @Test
    fun nextPeriod_incrementsDateWhenAllowed() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.previousPeriod()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextPeriod()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LocalDate.now(), viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.canGoForward)
    }

    @Test
    fun nextPeriod_doesNotExceedToday() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextPeriod() // Already today
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LocalDate.now(), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun selectDay_setsDateAndSwitchesToDaily() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setPeriod(StatsPeriod.WEEKLY)
        testDispatcher.scheduler.advanceUntilIdle()

        val targetDate = LocalDate.now().minusDays(3)
        viewModel.selectDay(targetDate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(StatsPeriod.DAILY, viewModel.uiState.value.period)
        assertEquals(targetDate, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun setSortOrder_updatesSortOrder() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSortOrder(AppSortOrder.NAME)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppSortOrder.NAME, viewModel.uiState.value.sortOrder)
    }

    @Test
    fun setSearchQuery_updatesSearchQuery() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSearchQuery("whatsapp")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("whatsapp", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun selectAppForDetails_opensAndClosesDialog() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val app = AppUsageItem(
            packageName = "com.sample.app",
            appName = "Sample",
            totalForegroundMs = 120_000L
        )

        viewModel.selectAppForDetails(app)
        assertEquals(app, viewModel.uiState.value.selectedAppForDetails)

        viewModel.selectAppForDetails(null)
        assertNull(viewModel.uiState.value.selectedAppForDetails)
    }

    private class FakeUsageStatsRepository : UsageStatsRepository {
        override fun hasUsagePermission(): Boolean = true

        override suspend fun getDailyStats(date: LocalDate): UsageStatsOverview {
            return UsageStatsOverview(
                period = StatsPeriod.DAILY,
                selectedDate = date,
                totalScreenTimeMs = 3600_000L,
                dailyAverageMs = 3600_000L,
                hasPermission = true
            )
        }

        override suspend fun getWeeklyStats(dateInWeek: LocalDate): UsageStatsOverview {
            return UsageStatsOverview(
                period = StatsPeriod.WEEKLY,
                selectedDate = dateInWeek,
                totalScreenTimeMs = 25200_000L,
                dailyAverageMs = 3600_000L,
                hasPermission = true
            )
        }

        override suspend fun getAppUsageForPeriod(
            packageName: String,
            startMs: Long,
            endMs: Long
        ): AppUsageItem? = null

        override suspend fun getUsageDuration(packageName: String, startMs: Long, endMs: Long): Long = 0L

        override suspend fun getCombinedUsageDuration(packages: Set<String>, startMs: Long, endMs: Long): Long = 0L
    }
}

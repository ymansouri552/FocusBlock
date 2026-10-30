package com.focusblock.ui.home

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.StrictTermType
import com.focusblock.domain.repository.ProfileRepository
import com.focusblock.domain.usecase.BlockedAppsUseCase
import com.focusblock.domain.usecase.EmergencyUnlockUseCase
import com.focusblock.domain.usecase.GetProfilesUseCase
import com.focusblock.domain.usecase.QuickBlockUseCase
import com.focusblock.domain.usecase.SaveProfileUseCase
import com.focusblock.domain.usecase.SystemPermissionChecker
import com.focusblock.domain.usecase.ToggleProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var emergencyUnlockUseCase: EmergencyUnlockUseCase
    private lateinit var viewModel: HomeViewModel

    private val fakeRepo = object : ProfileRepository {
        override fun getAllProfiles(): Flow<List<Profile>> = emptyFlow()
        override fun getProfileById(id: Int): Flow<Profile?> = emptyFlow()
        override suspend fun insertProfile(profile: Profile): Long = 1L
        override suspend fun updateProfile(profile: Profile): Int = 1
        override suspend fun deleteProfile(profile: Profile): Int = 1
        override suspend fun editDisabled(profile: Profile): Int = 1
        override suspend fun enableProfile(id: Int, enabled: Boolean): Int = 1
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        settingsDataStore = SettingsDataStore(context)
        emergencyUnlockUseCase = EmergencyUnlockUseCase(settingsDataStore)

        val getProfilesUseCase = GetProfilesUseCase(fakeRepo)
        val blockedAppsUseCase = BlockedAppsUseCase(fakeRepo)
        val toggleProfileUseCase = ToggleProfileUseCase(fakeRepo)
        val quickBlockUseCase = QuickBlockUseCase(fakeRepo)
        val permissionChecker = SystemPermissionChecker(context)

        viewModel = HomeViewModel(
            getProfilesUseCase = getProfilesUseCase,
            blockedAppsUseCase = blockedAppsUseCase,
            toggleProfileUseCase = toggleProfileUseCase,
            quickBlockUseCase = quickBlockUseCase,
            settingsDataStore = settingsDataStore,
            emergencyUnlockUseCase = emergencyUnlockUseCase,
            permissionChecker = permissionChecker
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun disarmStrictMode_whenOngoing_isBlockedWithError() = runTest(testDispatcher) {
        settingsDataStore.armStrictMode(StrictTermType.ONGOING)
        for (i in 1..25) {
            testScheduler.runCurrent()
            if (settingsDataStore.getStrictModeState().isArmed) break
            Thread.sleep(30)
        }

        assertTrue(settingsDataStore.getStrictModeState().isArmed)
        assertEquals(StrictTermType.ONGOING, settingsDataStore.getStrictModeState().termType)

        viewModel.disarmStrictMode()
        for (i in 1..25) {
            testScheduler.runCurrent()
            if (viewModel.ui.value.error != null) break
            Thread.sleep(30)
        }

        // Strict mode must still be armed
        assertTrue(settingsDataStore.getStrictModeState().isArmed)
        assertNotNull(viewModel.ui.value.error)
        assertTrue(viewModel.ui.value.error!!.contains("Ongoing Strict Mode"))
    }

    @Test
    fun disarmStrictMode_whenActiveTimed_isBlockedWithError() = runTest(testDispatcher) {
        val futureEnd = Instant.now().plus(Duration.ofHours(2))
        settingsDataStore.armStrictMode(StrictTermType.HOURS_4, customEndInstant = futureEnd)
        
        for (i in 1..25) {
            testScheduler.runCurrent()
            if (settingsDataStore.getStrictModeState().isArmed) break
            Thread.sleep(30)
        }
        assertTrue(settingsDataStore.getStrictModeState().isArmed)

        viewModel.disarmStrictMode()
        
        for (i in 1..25) {
            testScheduler.runCurrent()
            if (viewModel.ui.value.error != null) break
            Thread.sleep(30)
        }

        assertTrue(settingsDataStore.getStrictModeState().isArmed)
        assertEquals("Cannot disarm: Strict Mode term has not expired yet.", viewModel.ui.value.error)
    }

    @Test
    fun disarmStrictMode_whenExpiredTimed_disarmsSuccessfully() = runTest(testDispatcher) {
        val pastEnd = Instant.now().minus(Duration.ofMinutes(5))
        settingsDataStore.armStrictMode(StrictTermType.HOURS_4, customEndInstant = pastEnd)
        testScheduler.runCurrent()

        viewModel.disarmStrictMode()

        var isArmed = true
        for (i in 1..25) {
            testScheduler.runCurrent()
            if (!settingsDataStore.getStrictModeState().isArmed) {
                isArmed = false
                break
            }
            Thread.sleep(40)
        }

        assertFalse(isArmed)
        assertNull(viewModel.ui.value.error)
    }
}

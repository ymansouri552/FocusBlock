package com.focusblock.ui.settings

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.StrictTermType
import com.focusblock.domain.usecase.EmergencyUnlockStatus
import com.focusblock.domain.usecase.EmergencyUnlockUseCase
import com.focusblock.domain.usecase.TrustedClock
import com.focusblock.testing.createTestSettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var emergencyUnlockUseCase: EmergencyUnlockUseCase
    private lateinit var trustedClock: TrustedClock
    private var viewModel: SettingsViewModel? = null

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(testDispatcher)
        val context: Context = ApplicationProvider.getApplicationContext()
        settingsDataStore = createTestSettingsDataStore(context)
        settingsDataStore.disarmStrictMode()
        settingsDataStore.clearClockAnchor()
        settingsDataStore.setEmergencyUnlockEnabled(false)
        settingsDataStore.setEmergencyUnlockMinutes(15)
        settingsDataStore.setEmergencyUnlockRequestEpochMs(null)

        emergencyUnlockUseCase = EmergencyUnlockUseCase(settingsDataStore)
        trustedClock = TrustedClock(context, settingsDataStore)
    }

    @After
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        viewModel = null
        Dispatchers.resetMain()
    }

    @Test
    fun emergency_settings_are_loaded_from_the_data_store() = runTest(testDispatcher) {
        settingsDataStore.setEmergencyUnlockEnabled(true)
        settingsDataStore.setEmergencyUnlockMinutes(10)

        val viewModel = createViewModel()
        assertTrue(await { viewModel.ui.value.emergencyUnlockEnabled })
        assertEquals(10, viewModel.ui.value.emergencyUnlockMinutes)
        assertEquals(EmergencyUnlockStatus.NotRequested, viewModel.ui.value.emergencyUnlockStatus)
    }

    @Test
    fun emergency_settings_cannot_be_changed_while_strict_mode_is_active() = runTest(testDispatcher) {
        settingsDataStore.armStrictMode(StrictTermType.ONGOING)
        val viewModel = createViewModel()
        assertTrue(await { viewModel.ui.value.isStrictActive })

        viewModel.setEmergencyUnlockEnabled(true)
        viewModel.setEmergencyUnlockMinutes(30)
        Thread.sleep(100)
        testScheduler.runCurrent()

        assertFalse(viewModel.ui.value.emergencyUnlockEnabled)
        assertEquals(15, viewModel.ui.value.emergencyUnlockMinutes)
        assertFalse(settingsDataStore.getEmergencyUnlockEnabled())
        assertEquals(15, settingsDataStore.getEmergencyUnlockMinutes())
    }

    @Test
    fun emergency_unlock_requires_the_full_waiting_period() = runTest(testDispatcher) {
        settingsDataStore.setEmergencyUnlockEnabled(true)
        settingsDataStore.setEmergencyUnlockMinutes(10)
        settingsDataStore.armStrictMode(StrictTermType.ONGOING)

        val viewModel = createViewModel()
        assertTrue(await { viewModel.ui.value.isStrictActive })

        viewModel.requestEmergencyUnlock()
        assertTrue(await { viewModel.ui.value.emergencyUnlockStatus is EmergencyUnlockStatus.Waiting })

        // Confirming before the wait elapses must not disarm strict mode.
        viewModel.confirmEmergencyUnlock()
        Thread.sleep(100)
        testScheduler.runCurrent()
        assertTrue(settingsDataStore.getStrictModeState().isArmed)

        // Simulate the waiting period having elapsed on the monotonic clock.
        val waitMs = Duration.ofMinutes(10).toMillis()
        settingsDataStore.setEmergencyUnlockRequest(
            epochMs = Instant.now().minusMillis(waitMs).toEpochMilli(),
            elapsedRealtimeMs = SystemClock.elapsedRealtime() - waitMs - 1_000L,
            bootCount = settingsDataStore.currentBootCount()
        )

        viewModel.refreshRuntimeState()
        assertTrue(await { viewModel.ui.value.emergencyUnlockStatus == EmergencyUnlockStatus.Available })

        viewModel.confirmEmergencyUnlock()
        assertTrue(await { !settingsDataStore.getStrictModeState().isArmed })
        assertTrue(await { !viewModel.ui.value.isStrictActive })
        assertEquals(EmergencyUnlockStatus.NotRequested, viewModel.ui.value.emergencyUnlockStatus)
    }

    private fun createViewModel(): SettingsViewModel = SettingsViewModel(
        dataStore = settingsDataStore,
        trustedClock = trustedClock,
        emergencyUnlockUseCase = emergencyUnlockUseCase
    ).also { viewModel = it }

    private suspend fun await(
        timeoutMs: Long = 3_000,
        condition: suspend () -> Boolean
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            testDispatcher.scheduler.runCurrent()
            if (condition()) return true
            Thread.sleep(10)
        }
        return condition()
    }
}

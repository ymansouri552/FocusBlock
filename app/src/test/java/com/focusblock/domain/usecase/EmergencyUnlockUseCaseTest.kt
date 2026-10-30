package com.focusblock.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class EmergencyUnlockUseCaseTest {

    private lateinit var context: Context
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var useCase: EmergencyUnlockUseCase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        settingsDataStore = SettingsDataStore(context)
        useCase = EmergencyUnlockUseCase(settingsDataStore)
        StrictModeRules.independentState = StrictModeState()
    }

    @Test
    fun returns_disabled_when_emergency_unlock_setting_is_off(): Unit = runBlocking {
        settingsDataStore.setEmergencyUnlockEnabled(false)
        val status = useCase.getStatus()
        assertIs<EmergencyUnlockStatus.Disabled>(status)
    }

    @Test
    fun returns_not_requested_when_enabled_without_active_request(): Unit = runBlocking {
        settingsDataStore.setEmergencyUnlockEnabled(true)
        settingsDataStore.setEmergencyUnlockRequestEpochMs(null)
        val status = useCase.getStatus()
        assertIs<EmergencyUnlockStatus.NotRequested>(status)
    }

    @Test
    fun request_unlock_initiates_waiting_period_with_countdown(): Unit = runBlocking {
        val requestTime = Instant.parse("2026-10-30T10:00:00Z")
        settingsDataStore.setEmergencyUnlockEnabled(true)
        settingsDataStore.setEmergencyUnlockMinutes(15)

        val initiated = useCase.requestUnlock(requestTime)
        assertTrue(initiated)

        // 5 minutes later, still waiting
        val checkTime = requestTime.plus(Duration.ofMinutes(5))
        val status = useCase.getStatus(checkTime)

        assertIs<EmergencyUnlockStatus.Waiting>(status)
        assertEquals(600L, status.remainingSeconds) // 10 minutes = 600s remaining
    }

    @Test
    fun transitions_to_available_after_waiting_period_expires(): Unit = runBlocking {
        val requestTime = Instant.parse("2026-10-30T10:00:00Z")
        settingsDataStore.setEmergencyUnlockEnabled(true)
        settingsDataStore.setEmergencyUnlockMinutes(15)

        useCase.requestUnlock(requestTime)

        // 15 minutes and 1 second later
        val afterWait = requestTime.plus(Duration.ofMinutes(15)).plusSeconds(1)
        val status = useCase.getStatus(afterWait)

        assertIs<EmergencyUnlockStatus.Available>(status)
    }

    @Test
    fun confirm_unlock_disarms_strict_mode_when_available(): Unit = runBlocking {
        val requestTime = Instant.parse("2026-10-30T10:00:00Z")
        settingsDataStore.setEmergencyUnlockEnabled(true)
        settingsDataStore.setEmergencyUnlockMinutes(15)

        // Arm strict mode
        settingsDataStore.armStrictMode(StrictTermType.HOURS_4, requestTime.plus(Duration.ofHours(4)), requestTime)
        StrictModeRules.independentState = settingsDataStore.getStrictModeState()
        assertTrue(StrictModeRules.independentState.isArmed)

        useCase.requestUnlock(requestTime)

        val afterWait = requestTime.plus(Duration.ofMinutes(15))
        val confirmed = useCase.confirmUnlock(afterWait)

        assertTrue(confirmed)
        assertFalse(settingsDataStore.getStrictModeState().isArmed)
        assertFalse(StrictModeRules.independentState.isArmed)

        // Request time should be cleared
        val finalStatus = useCase.getStatus()
        assertIs<EmergencyUnlockStatus.NotRequested>(finalStatus)
    }
}

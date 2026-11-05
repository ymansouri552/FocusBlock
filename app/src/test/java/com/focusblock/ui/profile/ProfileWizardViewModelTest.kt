package com.focusblock.ui.profile

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.focusblock.domain.model.LimitScope
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.repository.ProfileRepository
import com.focusblock.domain.usecase.GetInstalledAppsUseCase
import com.focusblock.domain.usecase.SaveProfileUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProfileWizardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : ProfileRepository {
        var lastSavedProfile: Profile? = null
        override fun getAllProfiles(): Flow<List<Profile>> = emptyFlow()
        override fun getProfileById(id: Int): Flow<Profile?> = emptyFlow()
        override suspend fun insertProfile(profile: Profile): Long {
            lastSavedProfile = profile
            return 1L
        }
        override suspend fun updateProfile(profile: Profile): Int = 1
        override suspend fun deleteProfile(profile: Profile): Int = 1
        override suspend fun editDisabled(profile: Profile): Int = 1
        override suspend fun enableProfile(id: Int, enabled: Boolean): Int = 1
    }

    private lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase
    private lateinit var saveProfileUseCase: SaveProfileUseCase
    private lateinit var viewModel: ProfileWizardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val app = ApplicationProvider.getApplicationContext<Application>()
        getInstalledAppsUseCase = GetInstalledAppsUseCase(app)
        saveProfileUseCase = SaveProfileUseCase(fakeRepo)
        viewModel = ProfileWizardViewModel(saveProfileUseCase, getInstalledAppsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isStepOneWithDefaultPreset() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(1, state.step)
        assertEquals("deep_work", state.selectedPresetId)
        assertEquals("Deep Work", state.profileName)
    }

    @Test
    fun nextStep_validationFailuresAndSuccessAcross5Steps() = runTest(testDispatcher) {
        advanceUntilIdle()

        // Step 1: blank name fails
        viewModel.setProfileName("")
        assertFalse(viewModel.nextStep())
        assertNotNull(viewModel.uiState.value.error)

        // Step 1 -> 2: valid name succeeds
        viewModel.setProfileName("Focus Time")
        assertTrue(viewModel.nextStep())
        assertEquals(2, viewModel.uiState.value.step)
        assertNull(viewModel.uiState.value.error)

        // Step 2: empty apps fails
        assertFalse(viewModel.nextStep())
        assertNotNull(viewModel.uiState.value.error)

        // Step 2 -> 3: toggle an app succeeds
        viewModel.togglePackage("com.example.social")
        assertTrue(viewModel.nextStep())
        assertEquals(3, viewModel.uiState.value.step)
        assertNull(viewModel.uiState.value.error)

        // Step 3 -> 4: mode selection succeeds
        viewModel.setScheduleMode(ScheduleMode.BLOCK)
        assertTrue(viewModel.nextStep())
        assertEquals(4, viewModel.uiState.value.step)

        // Step 4: block mode requires at least one active day
        viewModel.setSchedule(viewModel.uiState.value.schedule.copy(daysOfWeek = emptySet()))
        assertFalse(viewModel.nextStep())
        assertNotNull(viewModel.uiState.value.error)

        // Step 4 -> 5: setting days succeeds to step 5
        viewModel.setSchedule(viewModel.uiState.value.schedule.copy(daysOfWeek = setOf(2, 3, 4)))
        assertTrue(viewModel.nextStep())
        assertEquals(5, viewModel.uiState.value.step)
        assertNull(viewModel.uiState.value.error)

        // Step 5 is max step: nextStep returns false
        assertFalse(viewModel.nextStep())
        assertEquals(5, viewModel.uiState.value.step)
    }

    @Test
    fun step4_usageLimitValidation_requiresPositiveMinutesAndDays() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.setProfileName("Allowance Profile")
        viewModel.togglePackage("com.example.social")
        viewModel.goToStep(3)

        viewModel.setScheduleMode(ScheduleMode.USAGE_LIMIT)
        assertEquals(ScheduleMode.USAGE_LIMIT, viewModel.uiState.value.schedule.mode)
        assertTrue(viewModel.nextStep())
        assertEquals(4, viewModel.uiState.value.step)

        // Zero minutes fails
        viewModel.setSchedule(viewModel.uiState.value.schedule.copy(usageLimitMinutes = 0))
        assertFalse(viewModel.nextStep())
        assertNotNull(viewModel.uiState.value.error)

        // Positive minutes but empty days fails
        viewModel.setSchedule(viewModel.uiState.value.schedule.copy(usageLimitMinutes = 30, daysOfWeek = emptySet()))
        assertFalse(viewModel.nextStep())
        assertNotNull(viewModel.uiState.value.error)

        // Valid minutes and days succeeds to step 5
        viewModel.setSchedule(viewModel.uiState.value.schedule.copy(usageLimitMinutes = 30, daysOfWeek = setOf(1, 2, 3)))
        assertTrue(viewModel.nextStep())
        assertEquals(5, viewModel.uiState.value.step)
    }

    @Test
    fun prevStep_and_goToStep_navigation() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.goToStep(5)
        assertEquals(5, viewModel.uiState.value.step)

        assertTrue(viewModel.prevStep())
        assertEquals(4, viewModel.uiState.value.step)

        viewModel.goToStep(3)
        assertEquals(3, viewModel.uiState.value.step)

        viewModel.goToStep(10) // out of bounds: should be ignored
        assertEquals(3, viewModel.uiState.value.step)
    }

    @Test
    fun save_persistsProfileToRepository() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.setProfileName("Saved Contract")
        viewModel.togglePackage("com.example.social")
        viewModel.setScheduleMode(ScheduleMode.USAGE_LIMIT)
        viewModel.setSchedule(
            viewModel.uiState.value.schedule.copy(
                type = ScheduleType.HOURLY,
                usageLimitMinutes = 20,
                limitScope = LimitScope.PER_APP,
                daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7)
            )
        )
        viewModel.toggleStrict(true)

        var saved = false
        viewModel.save(onSuccess = { saved = true })
        advanceUntilIdle()

        assertTrue(saved)
        val profile = fakeRepo.lastSavedProfile
        assertNotNull(profile)
        assertEquals("Saved Contract", profile?.name)
        assertTrue(profile?.strict ?: false)
        val savedSchedule = profile?.schedules?.firstOrNull()
        assertNotNull(savedSchedule)
        assertEquals(ScheduleMode.USAGE_LIMIT, savedSchedule?.mode)
        assertEquals(ScheduleType.HOURLY, savedSchedule?.type)
        assertEquals(20, savedSchedule?.usageLimitMinutes)
        assertEquals(LimitScope.PER_APP, savedSchedule?.limitScope)
    }
}

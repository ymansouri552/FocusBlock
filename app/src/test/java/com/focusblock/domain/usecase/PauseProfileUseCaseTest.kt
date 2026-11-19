package com.focusblock.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class PauseProfileUseCaseTest {

    private class FakeProfileRepository : ProfileRepository {
        private val profiles = mutableListOf<Profile>()
        private val flow = MutableStateFlow<List<Profile>>(emptyList())

        override fun getAllProfiles(): Flow<List<Profile>> = flow.asStateFlow()
        override fun getProfileById(id: Int): Flow<Profile?> = flow.map { list -> list.firstOrNull { it.id == id } }
        override suspend fun insertProfile(profile: Profile): Long {
            profiles.add(profile)
            flow.value = profiles.toList()
            return profile.id.toLong()
        }
        override suspend fun updateProfile(profile: Profile): Int {
            val index = profiles.indexOfFirst { it.id == profile.id }
            if (index >= 0) profiles[index] = profile else profiles.add(profile)
            flow.value = profiles.toList()
            return 1
        }
        override suspend fun deleteProfile(profile: Profile): Int = 1
        override suspend fun editDisabled(profile: Profile): Int = 1
        override suspend fun enableProfile(id: Int, enabled: Boolean): Int = 1
    }

    private lateinit var repository: FakeProfileRepository
    private lateinit var pauseUseCase: PauseProfileUseCase
    private lateinit var resumeUseCase: ResumeProfileUseCase

    @Before
    fun setUp() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val settingsDataStore = SettingsDataStore(context)
        settingsDataStore.clearClockAnchor()
        StrictModeRules.independentState = StrictModeState()

        repository = FakeProfileRepository()
        pauseUseCase = PauseProfileUseCase(repository, TrustedClock(context, settingsDataStore))
        resumeUseCase = ResumeProfileUseCase(repository)
    }

    private fun activeProfile(): Profile = Profile(
        id = 1,
        name = "Work",
        blockedApps = setOf("com.distraction.app"),
        enabled = true,
        schedules = listOf(
            Schedule(
                type = ScheduleType.ONE_OFF,
                startDateTime = Instant.parse("2020-01-01T00:00:00Z"),
                endDateTime = Instant.parse("2100-01-01T00:00:00Z")
            )
        )
    )

    @Test
    fun pausing_sets_deadline_and_lifts_blocking(): Unit = runBlocking {
        val profile = activeProfile()
        repository.insertProfile(profile)

        val result = pauseUseCase.invoke(profile.id, 15)
        assertTrue(result is PauseProfileUseCase.PauseResult.Success)

        val stored = repository.getProfileById(profile.id).first()
        assertNotNull(stored?.pausedUntilEpochMs)
        assertTrue(ScheduleEngine.isPaused(stored!!, Instant.now()))
        assertFalse(ScheduleEngine.isActive(stored, Instant.now()))
    }

    @Test
    fun resuming_clears_pause_and_restores_blocking(): Unit = runBlocking {
        val profile = activeProfile()
        repository.insertProfile(profile)
        pauseUseCase.invoke(profile.id, 15)

        val result = resumeUseCase.invoke(profile.id)
        assertNotNull(result as? ResumeProfileUseCase.ResumeResult.Success)

        val stored = repository.getProfileById(profile.id).first()
        assertEquals(null, stored?.pausedUntilEpochMs)
        assertTrue(ScheduleEngine.isActive(stored!!, Instant.now()))
    }

    @Test
    fun strict_mode_blocks_pausing(): Unit = runBlocking {
        val profile = activeProfile()
        repository.insertProfile(profile)
        StrictModeRules.independentState = StrictModeState(
            isArmed = true,
            termType = StrictTermType.ONGOING
        )

        val result = pauseUseCase.invoke(profile.id, 5)
        assertTrue(result is PauseProfileUseCase.PauseResult.Failure)

        val stored = repository.getProfileById(profile.id).first()
        assertEquals(null, stored?.pausedUntilEpochMs)
    }
}

package com.focusblock.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class RestoreBlockStateUseCaseTest {

    private class FakeProfileRepository : ProfileRepository {
        val profiles = mutableListOf<Profile>()
        private val _flow = MutableStateFlow<List<Profile>>(profiles)

        override fun getAllProfiles(): Flow<List<Profile>> = _flow.asStateFlow()
        override fun getProfileById(id: Int): Flow<Profile?> = MutableStateFlow(profiles.find { it.id == id })
        override suspend fun insertProfile(profile: Profile): Long = 1L
        override suspend fun updateProfile(profile: Profile): Int = 1
        override suspend fun deleteProfile(profile: Profile): Int = 1
        override suspend fun editDisabled(profile: Profile): Int = 1
        override suspend fun enableProfile(id: Int, enabled: Boolean): Int = 1
    }

    private lateinit var context: Context
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var trustedClock: TrustedClock
    private lateinit var fakeProfileRepo: FakeProfileRepository
    private lateinit var useCase: RestoreBlockStateUseCase

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        settingsDataStore = SettingsDataStore(context)
        trustedClock = TrustedClock(context, settingsDataStore)
        fakeProfileRepo = FakeProfileRepository()
        useCase = RestoreBlockStateUseCase(settingsDataStore, fakeProfileRepo, trustedClock)
        StrictModeRules.independentState = StrictModeState()
    }

    @Test
    fun restore_when_strict_mode_still_active_keeps_it_armed() = runBlocking {
        val now = Instant.parse("2026-10-30T12:00:00Z")
        val futureEnd = now.plus(Duration.ofHours(4))

        settingsDataStore.armStrictMode(StrictTermType.HOURS_4, futureEnd, now = now)

        val restored = useCase.restore(now)

        assertTrue(restored.isArmed)
        assertEquals(futureEnd, restored.endInstant)
        assertTrue(StrictModeRules.independentState.isArmed)
    }

    @Test
    fun restore_when_strict_term_expired_during_reboot_disarms_cleanly() = runBlocking {
        val now = Instant.parse("2026-10-30T12:00:00Z")
        val pastEnd = now.minus(Duration.ofMinutes(30))

        settingsDataStore.armStrictMode(StrictTermType.HOURS_4, pastEnd)

        val restored = useCase.restore(now)

        assertFalse(restored.isArmed)
        val inDataStore = settingsDataStore.getStrictModeState()
        assertFalse(inDataStore.isArmed)
        assertFalse(StrictModeRules.independentState.isArmed)
    }

    @Test
    fun restore_when_indefinite_ongoing_term_preserves_armed_state() = runBlocking {
        val now = Instant.parse("2026-10-30T12:00:00Z")

        settingsDataStore.armStrictMode(StrictTermType.ONGOING, null)

        val restored = useCase.restore(now)

        assertTrue(restored.isArmed)
        assertEquals(StrictTermType.ONGOING, restored.termType)
        assertTrue(StrictModeRules.independentState.isArmed)
    }

    @Test
    fun reboot_with_clock_rolled_past_end_does_not_disarm_strict_mode() = runBlocking {
        val start = Instant.parse("2026-10-30T10:00:00Z")
        val end = start.plus(Duration.ofHours(4)) // 14:00
        val originalElapsed = 10_000_000L
        val originalBootCount = 1

        settingsDataStore.armStrictMode(
            termType = StrictTermType.HOURS_4,
            customEndInstant = end,
            now = start,
            elapsedRealtimeMs = originalElapsed,
            bootCount = originalBootCount
        )
        // 1 real hour had passed before shutdown.
        settingsDataStore.recordStrictElapsedProgress(
            currentElapsedRealtimeMs = originalElapsed + Duration.ofHours(1).toMillis(),
            bootCount = originalBootCount
        )

        // Rebooted the next day with the wall clock rolled far past the end.
        val rebootedWall = Instant.parse("2026-10-31T10:00:00Z")
        val newElapsed = 5_000_000L
        val newBootCount = 2

        val restored = useCase.restore(rebootedWall, newElapsed, newBootCount)

        assertTrue(restored.isArmed)
        // 3 real hours of the 4-hour term remain after the reboot.
        assertEquals(rebootedWall.plus(Duration.ofHours(3)), restored.endInstant)
        assertTrue(StrictModeRules.independentState.isArmed)
    }
}

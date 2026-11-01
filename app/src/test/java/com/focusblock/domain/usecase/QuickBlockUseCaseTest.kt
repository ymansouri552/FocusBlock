package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class QuickBlockUseCaseTest {

    private val profiles = mutableListOf<Profile>()
    private val profilesFlow = MutableStateFlow<List<Profile>>(emptyList())

    private val fakeRepo = object : ProfileRepository {
        override fun getAllProfiles(): Flow<List<Profile>> = profilesFlow.asStateFlow()
        override fun getProfileById(id: Int): Flow<Profile?> = MutableStateFlow(profiles.find { it.id == id })
        override suspend fun insertProfile(profile: Profile): Long {
            val assigned = profile.copy(id = profiles.size + 1)
            profiles.add(assigned)
            profilesFlow.value = profiles.toList()
            return assigned.id.toLong()
        }
        override suspend fun updateProfile(profile: Profile): Int = 1
        override suspend fun deleteProfile(profile: Profile): Int {
            profiles.removeAll { it.id == profile.id }
            profilesFlow.value = profiles.toList()
            return 1
        }
        override suspend fun editDisabled(profile: Profile): Int = 1
        override suspend fun enableProfile(id: Int, enabled: Boolean): Int = 1
    }

    private lateinit var useCase: QuickBlockUseCase

    @Before
    fun setUp() {
        profiles.clear()
        profilesFlow.value = emptyList()
        useCase = QuickBlockUseCase(fakeRepo)
    }

    @Test
    fun startQuickBlock_inserts_profile_and_prunes_expired_quick_blocks(): Unit = runBlocking {
        // Expired quick block from 2 hours ago (duration was 30 mins)
        val expiredSchedule = Schedule(
            type = ScheduleType.QUICK,
            durationMinutes = 30,
            quickStartInstant = Instant.now().minus(2, ChronoUnit.HOURS)
        )
        val expiredProfile = Profile(
            id = 1,
            name = "Old Quick Block",
            blockedApps = setOf("com.distraction.old"),
            strict = false,
            enabled = true,
            schedules = listOf(expiredSchedule)
        )
        fakeRepo.insertProfile(expiredProfile)

        // Regular profile (should NEVER be pruned)
        val regularProfile = Profile(
            id = 2,
            name = "Work",
            blockedApps = setOf("com.work.app"),
            strict = false,
            enabled = true,
            schedules = emptyList()
        )
        fakeRepo.insertProfile(regularProfile)

        assertEquals(2, profiles.size)

        // Start new quick block
        useCase.startQuickBlock("New Quick Block", setOf("com.distraction.new"), 45)

        // Old quick block should be pruned; regular profile and new quick block remain
        assertEquals(2, profiles.size)
        assertTrue(profiles.any { it.name == "Work" })
        assertTrue(profiles.any { it.name == "New Quick Block" })
        assertFalse(profiles.any { it.name == "Old Quick Block" })
    }
}

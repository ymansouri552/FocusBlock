package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.repository.ProfileRepository
import java.time.Instant
import javax.inject.Inject

class QuickBlockUseCase @Inject constructor(private val repository: ProfileRepository) {
    /**
     * Creates a "quick block" profile blocking [packageNames] for [durationMinutes].
     */
    suspend fun startQuickBlock(profileName: String, packageNames: Set<String>, durationMinutes: Int): Long {
        val now = Instant.now()
        val schedule = Schedule(
            type = ScheduleType.QUICK,
            durationMinutes = durationMinutes,
            quickStartInstant = now
        )
        val profile = Profile(
            name = profileName,
            blockedApps = packageNames,
            strict = false,
            enabled = true,
            schedules = listOf(schedule)
        )
        return repository.insertProfile(profile)
    }
}
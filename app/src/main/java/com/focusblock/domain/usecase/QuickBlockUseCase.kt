package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.ScheduleType
import com.focusblock.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class QuickBlockUseCase @Inject constructor(private val repository: ProfileRepository) {
    /**
     * Creates a "quick block" profile blocking [packageNames] for [durationMinutes].
     * Cleans up any expired quick block profiles first so they do not accumulate indefinitely.
     */
    suspend fun startQuickBlock(profileName: String, packageNames: Set<String>, durationMinutes: Int): Long {
        val now = Instant.now()

        // Prune expired quick blocks
        runCatching {
            val all = repository.getAllProfiles().first()
            all.filter { p ->
                p.schedules.isNotEmpty() &&
                p.schedules.all { it.type == ScheduleType.QUICK } &&
                !ScheduleEngine.isActive(p, now)
            }.forEach { expired ->
                repository.deleteProfile(expired)
            }
        }

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

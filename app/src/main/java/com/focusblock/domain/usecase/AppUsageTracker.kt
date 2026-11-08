package com.focusblock.domain.usecase

import android.os.SystemClock
import com.focusblock.domain.model.ActiveProfileInfo
import com.focusblock.domain.model.LimitScope
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.domain.repository.AppUsageRepository
import com.focusblock.domain.repository.UsageStatsRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class UsageLimitStatus(
    val profile: Profile,
    val schedule: Schedule,
    val limitMs: Long,
    val usedMs: Long,
    val isExceeded: Boolean,
    val resetInstant: Instant
) {
    val remainingQuotaMs: Long
        get() = (limitMs - usedMs).coerceAtLeast(0L)

    val timeUntilResetMs: Long
        get() = (resetInstant.toEpochMilli() - System.currentTimeMillis()).coerceAtLeast(0L)
}

@Singleton
class AppUsageTracker @Inject constructor(
    private val repository: AppUsageRepository,
    private val usageStatsRepository: UsageStatsRepository? = null
) {
    @Volatile
    private var activePackage: String? = null

    @Volatile
    private var activeProfileId: Int = 0

    @Volatile
    private var sessionStartWallMs: Long = 0L

    @Volatile
    private var sessionStartElapsedMs: Long = 0L

    @Volatile
    private var sessionBaselineUsageMs: Long = -1L

    @Volatile
    private var sessionBaselinePkg: String? = null

    val currentPackage: String?
        get() = activePackage

    /**
     * Start or continue tracking the foreground package.
     * If the foreground package changed, commits the previous session to DB.
     */
    suspend fun onPackageForeground(pkg: String, profileId: Int = 0) {
        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()

        val prevPkg = activePackage
        if (prevPkg != null && prevPkg != pkg) {
            commitActiveSession(nowWall, nowElapsed)
        }

        if (activePackage == null || activePackage != pkg) {
            activePackage = pkg
            activeProfileId = profileId
            sessionStartWallMs = nowWall
            sessionStartElapsedMs = nowElapsed
            sessionBaselineUsageMs = -1L
            sessionBaselinePkg = pkg
        }
    }

    /**
     * Called when user leaves the monitored app (e.g. returns home or screen turns off).
     */
    suspend fun onPackageBackground() {
        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        commitActiveSession(nowWall, nowElapsed)
        activePackage = null
        activeProfileId = 0
        sessionStartWallMs = 0L
        sessionStartElapsedMs = 0L
        sessionBaselineUsageMs = -1L
        sessionBaselinePkg = null
    }

    /**
     * Flushes current session progress to database while keeping the session open.
     */
    suspend fun flushSessionProgress() {
        val pkg = activePackage ?: return
        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        if (sessionStartWallMs > 0 && nowWall > sessionStartWallMs) {
            repository.recordUsage(activeProfileId, pkg, sessionStartWallMs, nowWall)
            sessionStartWallMs = nowWall
            sessionStartElapsedMs = nowElapsed
        }
    }

    private suspend fun commitActiveSession(nowWall: Long, nowElapsed: Long) {
        val pkg = activePackage ?: return
        if (sessionStartWallMs > 0 && nowWall > sessionStartWallMs) {
            repository.recordUsage(activeProfileId, pkg, sessionStartWallMs, nowWall)
        }
    }

    /**
     * Computes the live active usage for a package within [windowStart, windowEnd].
     * Connects to actual device app usage via [usageStatsRepository] (ground truth from
     * system UsageStatsManager / UsageEvents) with seamless fallback to [repository].
     */
    suspend fun getUsage(
        pkg: String,
        profile: Profile,
        schedule: Schedule,
        windowStart: Instant,
        windowEnd: Instant
    ): Long {
        val windowStartMs = windowStart.toEpochMilli()
        val windowEndMs = windowEnd.toEpochMilli()

        val currentPkg = activePackage
        val hasActiveSession = currentPkg != null && sessionStartWallMs > 0 &&
                if (schedule.limitScope == LimitScope.COMBINED) profile.blockedApps.contains(currentPkg) else currentPkg == pkg

        val baselineMs: Long
        if (hasActiveSession && sessionBaselineUsageMs >= 0L && sessionBaselinePkg == pkg) {
            baselineMs = sessionBaselineUsageMs
        } else {
            val systemUsageMs = if (usageStatsRepository != null && usageStatsRepository.hasUsagePermission()) {
                if (schedule.limitScope == LimitScope.COMBINED) {
                    usageStatsRepository.getCombinedUsageDuration(profile.blockedApps, windowStartMs, windowEndMs)
                } else {
                    usageStatsRepository.getUsageDuration(pkg, windowStartMs, windowEndMs)
                }
            } else 0L

            val dbUsageMs = if (schedule.limitScope == LimitScope.COMBINED) {
                repository.getUsageForProfile(profile.id, windowStartMs, windowEndMs)
            } else {
                repository.getUsageForPackage(pkg, windowStartMs, windowEndMs)
            }

            baselineMs = maxOf(systemUsageMs, dbUsageMs)

            if (hasActiveSession) {
                sessionBaselineUsageMs = baselineMs
                sessionBaselinePkg = pkg
            }
        }

        // Add currently active uncommitted session in memory if applicable
        var activeAdditionMs = 0L
        if (hasActiveSession) {
            val nowWall = System.currentTimeMillis()
            val sessionStart = sessionStartWallMs.coerceAtLeast(windowStartMs)
            val sessionEnd = nowWall.coerceAtMost(windowEndMs)
            if (sessionEnd > sessionStart) {
                activeAdditionMs = sessionEnd - sessionStart
            }
        }

        return baselineMs + activeAdditionMs
    }

    /**
     * Evaluates whether [pkg] has exceeded its limit in any active usage-limit schedules.
     * Returns the first exceeded status, or the most restrictive active limit status if none exceeded.
     */
    suspend fun evaluateUsageLimit(
        pkg: String,
        activeProfiles: List<ActiveProfileInfo>,
        now: Instant = Instant.now()
    ): UsageLimitStatus? {
        val usageLimitInfos = activeProfiles.filter {
            it.profile.blockedApps.contains(pkg) &&
                it.schedule.mode == ScheduleMode.USAGE_LIMIT &&
                it.schedule.usageLimitMinutes > 0
        }

        if (usageLimitInfos.isEmpty()) return null

        var primaryStatus: UsageLimitStatus? = null

        for (info in usageLimitInfos) {
            val limitMs = info.schedule.usageLimitMinutes * 60 * 1000L
            val usedMs = getUsage(
                pkg = pkg,
                profile = info.profile,
                schedule = info.schedule,
                windowStart = info.startInstant,
                windowEnd = info.endInstant
            )
            val isExceeded = usedMs >= limitMs
            val status = UsageLimitStatus(
                profile = info.profile,
                schedule = info.schedule,
                limitMs = limitMs,
                usedMs = usedMs,
                isExceeded = isExceeded,
                resetInstant = info.endInstant
            )

            // If any schedule is exceeded, that takes top priority (must block)
            if (isExceeded) {
                return status
            }

            if (primaryStatus == null || status.remainingQuotaMs < primaryStatus.remainingQuotaMs) {
                primaryStatus = status
            }
        }

        return primaryStatus
    }
}

package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.StrictModeState
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Rules governing what may be edited while strict mode is active.
 *
 * Strict mode can be activated in two ways:
 * 1. Independently: Armed with its own terms (e.g. 4h, 8h, 12h, 24h, 3 days, 7 days, custom date, ongoing).
 *    While independent strict mode is armed, it stays active 24/7 across all profiles,
 *    even when recurring profile schedules are between active sessions.
 * 2. Per-profile: A profile marked strict during an active blocking session.
 *
 * While strict mode is active:
 *   - NOT editable: deactivate, delete, remove apps, shorten end time, change name
 *   - ALLOWED: add apps, extend end time
 */
object StrictModeRules {
    data class EditResult(val allowed: Boolean, val reason: String?)

    @Volatile
    var independentState: StrictModeState = StrictModeState()

    /**
     * Whether [profile] is currently covered by active strict mode.
     * Evaluates independent strict mode state as well as per-profile active schedule.
     */
    fun isStrictActive(
        profile: Profile,
        now: Instant = Instant.now(),
        state: StrictModeState = independentState
    ): Boolean {
        // 1. If Independent Strict Mode is currently armed and within terms:
        if (state.isEffectivelyActive(now)) {
            // In independent strict mode, any enabled profile or strict profile is locked
            if (profile.enabled || profile.strict) {
                return true
            }
        }

        // 2. Fallback to per-profile schedule strictness
        return profile.strict && ScheduleEngine.isActive(profile, now)
    }

    fun canDeactivate(
        profile: Profile,
        now: Instant = Instant.now(),
        state: StrictModeState = independentState
    ): EditResult {
        if (!isStrictActive(profile, now, state)) return EditResult(true, null)
        val reason = if (state.isEffectivelyActive(now)) {
            val remaining = formatTermRemaining(state, now)
            "Strict Mode is armed ($remaining). Profiles cannot be deactivated."
        } else {
            "Strict profile cannot be deactivated until ${endTime(profile, now)}"
        }
        return EditResult(false, reason)
    }

    fun canDelete(
        profile: Profile,
        now: Instant = Instant.now(),
        state: StrictModeState = independentState
    ): EditResult {
        if (!isStrictActive(profile, now, state)) return EditResult(true, null)
        val reason = if (state.isEffectivelyActive(now)) {
            val remaining = formatTermRemaining(state, now)
            "Strict Mode is armed ($remaining). Profiles cannot be deleted."
        } else {
            "Strict profile cannot be deleted until its end time"
        }
        return EditResult(false, reason)
    }

    fun canEditName(
        profile: Profile,
        now: Instant = Instant.now(),
        state: StrictModeState = independentState
    ): EditResult {
        if (!isStrictActive(profile, now, state)) return EditResult(true, null)
        val reason = if (state.isEffectivelyActive(now)) {
            "Profile name cannot be changed while Strict Mode is armed."
        } else {
            "Strict profile name cannot be changed while active"
        }
        return EditResult(false, reason)
    }

    fun canRemoveApps(
        profile: Profile,
        now: Instant = Instant.now(),
        state: StrictModeState = independentState
    ): EditResult {
        if (!isStrictActive(profile, now, state)) return EditResult(true, null)
        val reason = if (state.isEffectivelyActive(now)) {
            "Apps cannot be removed while Strict Mode is armed."
        } else {
            "Apps cannot be removed from a strict profile while active"
        }
        return EditResult(false, reason)
    }

    fun canAddApps(profile: Profile, now: Instant = Instant.now()): EditResult {
        // Always allowed, even while strict active
        return EditResult(true, null)
    }

    fun canExtendEndTime(profile: Profile, now: Instant = Instant.now()): EditResult {
        // Always allowed, even while strict active
        return EditResult(true, null)
    }

    fun canShortenEndTime(
        profile: Profile,
        now: Instant = Instant.now(),
        state: StrictModeState = independentState
    ): EditResult {
        if (!isStrictActive(profile, now, state)) return EditResult(true, null)
        val reason = if (state.isEffectivelyActive(now)) {
            "Schedules cannot be shortened while Strict Mode is armed."
        } else {
            "End time cannot be shortened while a strict profile is active"
        }
        return EditResult(false, reason)
    }

    private fun formatTermRemaining(state: StrictModeState, now: Instant): String {
        val end = state.endInstant ?: return "Ongoing term"
        val remainingMs = Duration.between(now, end).toMillis()
        if (remainingMs <= 0) return "Term completed"
        val hours = remainingMs / (1000 * 60 * 60)
        val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
        return when {
            hours >= 24 -> "${hours / 24}d ${hours % 24}h remaining"
            hours > 0 -> "${hours}h ${minutes}m remaining"
            else -> "${minutes}m remaining"
        }
    }

    private fun endTime(profile: Profile, now: Instant): String {
        val info = ScheduleEngine.activeProfile(profile, now)
        val endInstant = info?.endInstant ?: return "end time"
        val zoned = endInstant.atZone(ZoneId.systemDefault())
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(zoned)
    }
}

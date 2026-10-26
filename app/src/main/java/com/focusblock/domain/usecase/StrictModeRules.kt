package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleEngine
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Rules governing what may be edited while a strict profile is active.
 *
 * While a strict profile is active:
 *   - NOT editable: deactivate, delete, remove apps, shorten end time, change name
 *   - ALLOWED: add apps, extend end time
 */
object StrictModeRules {
    data class EditResult(val allowed: Boolean, val reason: String?)

    /**
     * Whether [profile] is currently strict and active.
     */
    fun isStrictActive(profile: Profile, now: Instant = Instant.now()): Boolean {
        return profile.strict && ScheduleEngine.isActive(profile, now)
    }

    fun canDeactivate(profile: Profile, now: Instant = Instant.now()): EditResult {
        return if (isStrictActive(profile, now)) EditResult(false, "Strict profile cannot be deactivated until ${endTime(profile, now)}") else EditResult(true, null)
    }

    fun canDelete(profile: Profile, now: Instant = Instant.now()): EditResult {
        return if (isStrictActive(profile, now)) EditResult(false, "Strict profile cannot be deleted until its end time") else EditResult(true, null)
    }

    fun canEditName(profile: Profile, now: Instant = Instant.now()): EditResult {
        return if (isStrictActive(profile, now)) EditResult(false, "Strict profile name cannot be changed while active") else EditResult(true, null)
    }

    fun canRemoveApps(profile: Profile, now: Instant = Instant.now()): EditResult {
        return if (isStrictActive(profile, now)) EditResult(false, "Apps cannot be removed from a strict profile while active") else EditResult(true, null)
    }

    fun canAddApps(profile: Profile, now: Instant = Instant.now()): EditResult {
        // Always allowed, even while strict active
        return EditResult(true, null)
    }

    fun canExtendEndTime(profile: Profile, now: Instant = Instant.now()): EditResult {
        // Always allowed, even while strict active
        return EditResult(true, null)
    }

    fun canShortenEndTime(profile: Profile, now: Instant = Instant.now()): EditResult {
        return if (isStrictActive(profile, now)) EditResult(false, "End time cannot be shortened while a strict profile is active") else EditResult(true, null)
    }

    private fun endTime(profile: Profile, now: Instant): String {
        val info = ScheduleEngine.activeProfile(profile, now)
        val endInstant = info?.endInstant ?: return "end time"
        val zoned = endInstant.atZone(ZoneId.systemDefault())
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(zoned)
    }
}
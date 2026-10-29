package com.focusblock.domain.model

import java.time.Instant

enum class StrictTermType(val label: String, val defaultDurationHours: Long?) {
    HOURS_4("4 Hours", 4L),
    HOURS_8("8 Hours", 8L),
    HOURS_12("12 Hours", 12L),
    HOURS_24("24 Hours (1 Day)", 24L),
    DAYS_3("3 Days", 72L),
    DAYS_7("7 Days (1 Week)", 168L),
    UNTIL_DATE_TIME("Custom Date & Time", null),
    ONGOING("Ongoing / Indefinite", null)
}

data class StrictModeState(
    val isArmed: Boolean = false,
    val termType: StrictTermType = StrictTermType.HOURS_24,
    val endInstant: Instant? = null,
    val startedAt: Instant? = null
) {
    /**
     * Checks if this independent strict mode state is currently actively armed and within its term.
     */
    fun isEffectivelyActive(now: Instant = Instant.now()): Boolean {
        if (!isArmed) return false
        val end = endInstant ?: return true // null endInstant means ONGOING
        return now.isBefore(end)
    }

    /**
     * Remaining milliseconds until strict mode terms expire.
     * Returns null if ongoing, or 0 if expired/inactive.
     */
    fun remainingMs(now: Instant = Instant.now()): Long? {
        if (!isArmed) return 0L
        val end = endInstant ?: return null // Ongoing
        val diff = end.toEpochMilli() - now.toEpochMilli()
        return if (diff > 0) diff else 0L
    }
}

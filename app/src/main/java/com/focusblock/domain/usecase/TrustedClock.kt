package com.focusblock.domain.usecase

import android.content.Context
import android.os.SystemClock
import com.focusblock.data.local.datastore.ClockAnchor
import com.focusblock.data.local.datastore.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * A wall clock that is anchored to [SystemClock.elapsedRealtime], which cannot be changed by
 * the user and only resets on reboot.
 *
 * If the device wall clock jumps by more than [DEFAULT_TOLERANCE_MS] relative to the trusted
 * elapsed-based estimate, the jump is ignored until [reanchor] is called. Callers re-anchor
 * deliberately when no strict session is active (see HandleTimeChangeUseCase), so ordinary
 * clock corrections still take effect once the committed term is over.
 */
@Singleton
class TrustedClock @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {
    companion object {
        /** Wall-clock drift below this is treated as ordinary system time synchronization. */
        const val DEFAULT_TOLERANCE_MS = 10_000L
    }

    private val mutex = Mutex()
    private var cachedAnchor: ClockAnchor? = null

    fun currentBootCount(): Int = runCatching {
        android.provider.Settings.Global.getInt(
            context.contentResolver,
            android.provider.Settings.Global.BOOT_COUNT
        )
    }.getOrDefault(-1)

    suspend fun initialize() {
        mutex.withLock {
            cachedAnchor = settingsDataStore.getClockAnchor()
        }
    }

    /**
     * Returns wall-clock time derived from the elapsedRealtime anchor, ignoring user clock changes.
     * A first call (or a call after a reboot) establishes a fresh anchor from [wallNow].
     */
    suspend fun now(
        wallNow: Instant = Instant.now(),
        elapsedNow: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L),
        bootCount: Int = currentBootCount(),
        toleranceMs: Long = DEFAULT_TOLERANCE_MS
    ): Instant = mutex.withLock {
        val anchor = cachedAnchor ?: settingsDataStore.getClockAnchor().also { cachedAnchor = it }

        if (anchor == null || anchor.bootCount != bootCount || elapsedNow < anchor.elapsedRealtimeMs) {
            val fresh = ClockAnchor(wallNow.toEpochMilli(), elapsedNow, bootCount)
            cachedAnchor = fresh
            settingsDataStore.setClockAnchor(fresh)
            return@withLock wallNow
        }

        val estimatedMs = anchor.wallEpochMs + (elapsedNow - anchor.elapsedRealtimeMs)
        val driftMs = wallNow.toEpochMilli() - estimatedMs
        if (abs(driftMs) <= toleranceMs) {
            // Clock agrees with real elapsed time; keep the in-memory anchor fresh.
            cachedAnchor = ClockAnchor(wallNow.toEpochMilli(), elapsedNow, bootCount)
            wallNow
        } else {
            // Wall clock was moved by the user; continue with true elapsed time.
            Instant.ofEpochMilli(estimatedMs)
        }
    }

    /** Accepts the current system clock as the new trusted baseline (used outside strict sessions). */
    suspend fun reanchor(
        wallNow: Instant = Instant.now(),
        elapsedNow: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L),
        bootCount: Int = currentBootCount()
    ) {
        mutex.withLock {
            val fresh = ClockAnchor(wallNow.toEpochMilli(), elapsedNow, bootCount)
            cachedAnchor = fresh
            settingsDataStore.setClockAnchor(fresh)
        }
    }

    /** Persists how much real time the committed strict term has consumed so far. */
    suspend fun recordStrictProgress(
        elapsedNow: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L),
        bootCount: Int = currentBootCount()
    ) {
        settingsDataStore.recordStrictElapsedProgress(elapsedNow, bootCount)
    }
}

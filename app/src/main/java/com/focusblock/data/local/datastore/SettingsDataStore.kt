package com.focusblock.data.local.datastore

import android.content.Context
import android.os.SystemClock
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.model.StrictTermType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

private val Context.focusBlockDataStore by preferencesDataStore(name = "focusblock_settings")

object SettingsKeys {
    val accessibilityGranted = stringPreferencesKey("accessibility_granted")
    val deviceAdminEnabled = stringPreferencesKey("device_admin_enabled")
    val usageAccessGranted = stringPreferencesKey("usage_access_granted")
    val notificationsGranted = stringPreferencesKey("notifications_granted")
    val exactAlarmsGranted = stringPreferencesKey("exact_alarms_granted")
    val batteryOptimizationExempt = stringPreferencesKey("battery_optimization_exempt")
    val strictLevel = stringPreferencesKey("strict_level")
    val emergencyUnlockMinutes = stringPreferencesKey("emergency_unlock_minutes")
    val emergencyUnlockEnabled = stringPreferencesKey("emergency_unlock_enabled")
    val emergencyUnlockRequestEpochMs = longPreferencesKey("emergency_unlock_request_epoch_ms")
    val theme = stringPreferencesKey("theme")
    val language = stringPreferencesKey("language")
    val debugShortenStrict = stringPreferencesKey("debug_shorten_strict")

    // Independent Strict Mode Keys
    val independentStrictArmed = booleanPreferencesKey("independent_strict_armed")
    val independentStrictTermType = stringPreferencesKey("independent_strict_term_type")
    val independentStrictEndEpochMs = longPreferencesKey("independent_strict_end_epoch_ms")
    val independentStrictStartedEpochMs = longPreferencesKey("independent_strict_started_epoch_ms")
    val independentStrictStartedElapsedRealtimeMs = longPreferencesKey("independent_strict_started_elapsed_realtime_ms")
    val independentStrictExpectedDurationMs = longPreferencesKey("independent_strict_expected_duration_ms")
}

data class StrictElapsedInfo(
    val isArmed: Boolean,
    val startedAt: Instant?,
    val endInstant: Instant?,
    val startedElapsedRealtimeMs: Long?,
    val expectedDurationMs: Long?
)

enum class StrictLevel(val value: String) {
    TARGETED("TARGETED"),
    AGGRESSIVE("AGGRESSIVE")
}

open class SettingsDataStore(context: Context) {
    private val dataStore = context.focusBlockDataStore
    val settings: Flow<Preferences> = dataStore.data

    open val strictModeState: Flow<StrictModeState> = dataStore.data.map { prefs ->
        val armed = prefs[SettingsKeys.independentStrictArmed] ?: false
        val termTypeName = prefs[SettingsKeys.independentStrictTermType] ?: StrictTermType.HOURS_24.name
        val termType = try {
            StrictTermType.valueOf(termTypeName)
        } catch (e: Exception) {
            StrictTermType.HOURS_24
        }
        val endEpochMs = prefs[SettingsKeys.independentStrictEndEpochMs]
        val endInstant = if (endEpochMs != null && endEpochMs > 0) Instant.ofEpochMilli(endEpochMs) else null
        val startedEpochMs = prefs[SettingsKeys.independentStrictStartedEpochMs]
        val startedAt = if (startedEpochMs != null && startedEpochMs > 0) Instant.ofEpochMilli(startedEpochMs) else null

        StrictModeState(
            isArmed = armed,
            termType = termType,
            endInstant = endInstant,
            startedAt = startedAt
        )
    }

    open suspend fun getStrictModeState(): StrictModeState {
        return strictModeState.first()
    }

    open suspend fun getStrictElapsedInfo(): StrictElapsedInfo {
        val prefs = dataStore.data.first()
        val armed = prefs[SettingsKeys.independentStrictArmed] ?: false
        val endEpochMs = prefs[SettingsKeys.independentStrictEndEpochMs]
        val endInstant = if (endEpochMs != null && endEpochMs > 0) Instant.ofEpochMilli(endEpochMs) else null
        val startedEpochMs = prefs[SettingsKeys.independentStrictStartedEpochMs]
        val startedAt = if (startedEpochMs != null && startedEpochMs > 0) Instant.ofEpochMilli(startedEpochMs) else null
        val startedElapsed = prefs[SettingsKeys.independentStrictStartedElapsedRealtimeMs]
        val expectedDuration = prefs[SettingsKeys.independentStrictExpectedDurationMs]

        return StrictElapsedInfo(
            isArmed = armed,
            startedAt = startedAt,
            endInstant = endInstant,
            startedElapsedRealtimeMs = startedElapsed,
            expectedDurationMs = expectedDuration
        )
    }

    open suspend fun updateStrictEndInstant(newEnd: Instant) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.independentStrictEndEpochMs] = newEnd.toEpochMilli()
        }
    }

    open suspend fun armStrictMode(
        termType: StrictTermType,
        customEndInstant: Instant? = null,
        now: Instant = Instant.now(),
        elapsedRealtimeMs: Long = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L)
    ) {
        val calculatedEnd: Instant? = when {
            customEndInstant != null -> customEndInstant
            termType.defaultDurationHours != null -> now.plus(termType.defaultDurationHours, ChronoUnit.HOURS)
            else -> null
        }
        val expectedDurationMs = when {
            customEndInstant != null -> Duration.between(now, customEndInstant).toMillis().coerceAtLeast(0L)
            termType.defaultDurationHours != null -> termType.defaultDurationHours * 3600 * 1000L
            else -> null
        }

        dataStore.edit { prefs ->
            prefs[SettingsKeys.independentStrictArmed] = true
            prefs[SettingsKeys.independentStrictTermType] = termType.name
            if (calculatedEnd != null) {
                prefs[SettingsKeys.independentStrictEndEpochMs] = calculatedEnd.toEpochMilli()
                prefs[SettingsKeys.independentStrictExpectedDurationMs] = expectedDurationMs ?: 0L
            } else {
                prefs.remove(SettingsKeys.independentStrictEndEpochMs)
                prefs.remove(SettingsKeys.independentStrictExpectedDurationMs)
            }
            prefs[SettingsKeys.independentStrictStartedEpochMs] = now.toEpochMilli()
            prefs[SettingsKeys.independentStrictStartedElapsedRealtimeMs] = elapsedRealtimeMs
        }
    }

    open suspend fun disarmStrictMode() {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.independentStrictArmed] = false
            prefs.remove(SettingsKeys.independentStrictEndEpochMs)
            prefs.remove(SettingsKeys.independentStrictStartedEpochMs)
            prefs.remove(SettingsKeys.independentStrictStartedElapsedRealtimeMs)
            prefs.remove(SettingsKeys.independentStrictExpectedDurationMs)
        }
    }

    open suspend fun writeString(key: Preferences.Key<String>, value: String) {
        dataStore.edit { prefs -> prefs[key] = value }
    }

    open suspend fun getEmergencyUnlockEnabled(): Boolean {
        return readString(SettingsKeys.emergencyUnlockEnabled, "false").toBoolean()
    }

    open suspend fun setEmergencyUnlockEnabled(enabled: Boolean) {
        writeString(SettingsKeys.emergencyUnlockEnabled, enabled.toString())
    }

    open suspend fun getEmergencyUnlockMinutes(): Int {
        return readString(SettingsKeys.emergencyUnlockMinutes, "15").toIntOrNull() ?: 15
    }

    open suspend fun setEmergencyUnlockMinutes(minutes: Int) {
        writeString(SettingsKeys.emergencyUnlockMinutes, minutes.toString())
    }

    open suspend fun getEmergencyUnlockRequestEpochMs(): Long? {
        val prefs = dataStore.data.first()
        val ms = prefs[SettingsKeys.emergencyUnlockRequestEpochMs]
        return if (ms != null && ms > 0) ms else null
    }

    open suspend fun setEmergencyUnlockRequestEpochMs(ms: Long?) {
        dataStore.edit { prefs ->
            if (ms != null) {
                prefs[SettingsKeys.emergencyUnlockRequestEpochMs] = ms
            } else {
                prefs.remove(SettingsKeys.emergencyUnlockRequestEpochMs)
            }
        }
    }

    open fun flowString(key: Preferences.Key<String>, default: String): Flow<String> {
        return settings.map { prefs -> prefs[key] ?: default }
    }

    open suspend fun readString(key: Preferences.Key<String>, default: String): String {
        return dataStore.data.first().let { prefs -> prefs[key] ?: default }
    }
}

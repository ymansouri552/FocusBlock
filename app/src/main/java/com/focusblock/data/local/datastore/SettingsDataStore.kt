package com.focusblock.data.local.datastore

import android.content.Context
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
    val theme = stringPreferencesKey("theme")
    val language = stringPreferencesKey("language")
    val debugShortenStrict = stringPreferencesKey("debug_shorten_strict")

    // Independent Strict Mode Keys
    val independentStrictArmed = booleanPreferencesKey("independent_strict_armed")
    val independentStrictTermType = stringPreferencesKey("independent_strict_term_type")
    val independentStrictEndEpochMs = longPreferencesKey("independent_strict_end_epoch_ms")
    val independentStrictStartedEpochMs = longPreferencesKey("independent_strict_started_epoch_ms")
}

enum class StrictLevel(val value: String) {
    TARGETED("TARGETED"),
    AGGRESSIVE("AGGRESSIVE")
}

class SettingsDataStore(context: Context) {
    private val dataStore = context.focusBlockDataStore
    val settings: Flow<Preferences> = dataStore.data

    val strictModeState: Flow<StrictModeState> = dataStore.data.map { prefs ->
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

    suspend fun getStrictModeState(): StrictModeState {
        return strictModeState.first()
    }

    suspend fun armStrictMode(termType: StrictTermType, customEndInstant: Instant? = null) {
        val now = Instant.now()
        val calculatedEnd: Instant? = when {
            customEndInstant != null -> customEndInstant
            termType.defaultDurationHours != null -> now.plus(termType.defaultDurationHours, ChronoUnit.HOURS)
            else -> null
        }
        dataStore.edit { prefs ->
            prefs[SettingsKeys.independentStrictArmed] = true
            prefs[SettingsKeys.independentStrictTermType] = termType.name
            if (calculatedEnd != null) {
                prefs[SettingsKeys.independentStrictEndEpochMs] = calculatedEnd.toEpochMilli()
            } else {
                prefs.remove(SettingsKeys.independentStrictEndEpochMs)
            }
            prefs[SettingsKeys.independentStrictStartedEpochMs] = now.toEpochMilli()
        }
    }

    suspend fun disarmStrictMode() {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.independentStrictArmed] = false
            prefs.remove(SettingsKeys.independentStrictEndEpochMs)
            prefs.remove(SettingsKeys.independentStrictStartedEpochMs)
        }
    }

    suspend fun writeString(key: Preferences.Key<String>, value: String) {
        dataStore.edit { prefs -> prefs[key] = value }
    }

    fun flowString(key: Preferences.Key<String>, default: String): Flow<String> {
        return settings.map { prefs -> prefs[key] ?: default }
    }

    suspend fun readString(key: Preferences.Key<String>, default: String): String {
        return dataStore.data.first().let { prefs -> prefs[key] ?: default }
    }
}

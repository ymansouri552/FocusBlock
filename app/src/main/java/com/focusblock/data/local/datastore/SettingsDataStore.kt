package com.focusblock.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.StringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

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
}

enum class StrictLevel(val value: String) {
    TARGETED("TARGETED"),
    AGGRESSIVE("AGGRESSIVE")
}

class SettingsDataStore(context: Context) {
    private val dataStore = context.focusBlockDataStore
    val settings: Flow<Preferences> = dataStore.data

    suspend fun writeString(key: StringPreferencesKey, value: String) {
        dataStore.edit { prefs -> prefs[key] = value }
    }

    fun flowString(key: StringPreferencesKey, default: String): Flow<String> {
        return settings.map { prefs -> prefs[key] ?: default }
    }

    suspend fun readString(key: StringPreferencesKey, default: String): String {
        return dataStore.data.first().let { prefs -> prefs[key] ?: default }
    }
}

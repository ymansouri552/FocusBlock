package com.focusblock.di

import android.app.Application
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.data.local.room.AppDatabase
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.domain.usecase.TrustedClock
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FocusBlockApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var trustedClock: TrustedClock

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            trustedClock.initialize()
            settingsDataStore.strictModeState.collect { state ->
                StrictModeRules.independentState = state
            }
        }
        // Persist elapsed-based progress so a reboot can freeze the term at the last known point
        // instead of trusting the wall clock.
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            while (isActive) {
                delay(60_000)
                runCatching { trustedClock.recordStrictProgress() }
            }
        }
    }
}

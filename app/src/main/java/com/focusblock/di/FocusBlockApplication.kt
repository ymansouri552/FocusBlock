package com.focusblock.di

import android.app.Application
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.data.local.room.AppDatabase
import com.focusblock.domain.usecase.StrictModeRules
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FocusBlockApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            settingsDataStore.strictModeState.collect { state ->
                StrictModeRules.independentState = state
            }
        }
    }
}

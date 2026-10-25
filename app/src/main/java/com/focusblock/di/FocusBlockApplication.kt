package com.focusblock.di

import android.app.Application
import com.focusblock.data.local.room.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FocusBlockApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
}
package com.focusblock.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusblock.domain.usecase.HandleTimeChangeUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimeChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var handleTimeChangeUseCase: HandleTimeChangeUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_TIME_CHANGED && action != Intent.ACTION_TIMEZONE_CHANGED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleTimeChangeUseCase.handleTimeChanged()
            } catch (e: Exception) {
                // Gracefully ignore
            } finally {
                pendingResult.finish()
            }
        }
    }
}

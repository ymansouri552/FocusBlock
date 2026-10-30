package com.focusblock.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusblock.domain.usecase.RestoreBlockStateUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var restoreBlockStateUseCase: RestoreBlockStateUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                restoreBlockStateUseCase.restore()
            } catch (e: Exception) {
                // Gracefully ignore
            } finally {
                pendingResult.finish()
            }
        }
    }
}

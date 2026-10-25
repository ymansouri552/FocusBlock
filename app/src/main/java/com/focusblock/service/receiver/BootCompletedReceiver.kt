package com.focusblock.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Re-initialize blocking state after boot / app update.
        // No polling; just schedule refresh via WorkManager/alarms.
    }
}
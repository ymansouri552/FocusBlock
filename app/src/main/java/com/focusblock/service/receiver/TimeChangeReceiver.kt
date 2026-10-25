package com.focusblock.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Clock / timezone changed: recompute block state. Use elapsedRealtime alongside
        // wall-clock to prevent clock-rollback from shortening strict blocks.
    }
}
package com.focusblock.service.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.focusblock.di.FocusBlockApplication

class FocusBlockDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Device Administrator enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Device Administrator disabled - strict mode protection lost", Toast.LENGTH_LONG).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling device administration will allow FocusBlock to be uninstalled and strict blocks to be bypassed. " +
            "Keep it enabled while any strict profile is active."
    }

    override fun onUninstallBlocked(context: Context, intent: Intent) {
        super.onUninstallBlocked(context, intent)
    }
}
package com.focusblock.domain.usecase

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import com.focusblock.service.accessibility.FocusBlockAccessibilityService
import com.focusblock.service.admin.FocusBlockDeviceAdminReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class SystemPermissionsState(
    val isAccessibilityGranted: Boolean = false,
    val isDeviceAdminGranted: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false
) {
    val allRequiredGranted: Boolean
        get() = isAccessibilityGranted

    val hasAnyWarning: Boolean
        get() = !isAccessibilityGranted || !isDeviceAdminGranted || !isBatteryOptimizationIgnored
}

@Singleton
class SystemPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun checkPermissions(): SystemPermissionsState {
        return SystemPermissionsState(
            isAccessibilityGranted = isAccessibilityGranted(),
            isDeviceAdminGranted = isDeviceAdminGranted(),
            isBatteryOptimizationIgnored = isBatteryOptimizationIgnored()
        )
    }

    fun isAccessibilityGranted(): Boolean {
        return runCatching {
            val componentName = ComponentName(context, FocusBlockAccessibilityService::class.java)
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            enabledServices.split(':').any {
                ComponentName.unflattenFromString(it) == componentName
            }
        }.getOrDefault(false)
    }

    fun isDeviceAdminGranted(): Boolean {
        return runCatching {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
            val adminComponent = ComponentName(context, FocusBlockDeviceAdminReceiver::class.java)
            dpm.isAdminActive(adminComponent)
        }.getOrDefault(false)
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        return runCatching {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }.getOrDefault(true)
    }

    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getDeviceAdminRequestIntent(): Intent {
        val adminComponent = ComponentName(context, FocusBlockDeviceAdminReceiver::class.java)
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "FocusBlock needs Device Administrator to prevent uninstallation and premature app tampering while Strict Mode is active."
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getBatteryOptimizationIntent(): Intent {
        return try {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } catch (e: Exception) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}

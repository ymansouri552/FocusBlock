package com.focusblock.domain.usecase

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.focusblock.domain.model.Profile
import com.focusblock.service.admin.FocusBlockDeviceAdminReceiver
import com.focusblock.service.accessibility.FocusBlockAccessibilityService
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * v1 strict-mode enforcer: Device Administrator + AccessibilityService.
 *
 * No other code depends on this concrete class; depend on StrictEnforcer only.
 */
@Singleton
class DeviceAdminAccessibilityEnforcer @Inject constructor(
    private val context: Context,
    private val emergencyUnlockUseCase: EmergencyUnlockUseCase
) : StrictEnforcer {

    private val dpm: DevicePolicyManager
        get() = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent: ComponentName =
        ComponentName(context, FocusBlockDeviceAdminReceiver::class.java)

    override fun isEnforcementActive(): Boolean {
        return dpm.isAdminActive(adminComponent) && isAccessibilityServiceEnabled()
    }

    override fun requestActivation() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "FocusBlock needs Device Administrator to prevent uninstallation while Strict Mode is active.")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            ContextCompat.startActivity(context, intent, null)
        } catch (e: ActivityNotFoundException) {
            // Fallback: open Accessibility settings so the user can enable the service
            val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                ContextCompat.startActivity(context, settingsIntent, null)
            } catch (e2: ActivityNotFoundException) {
                // Give up gracefully
            }
        }
    }

    override fun isStrictProfileActive(profile: Profile): Boolean {
        return profile.strict && isEnforcementActive()
    }

    override fun onStrictProfileStateChanged(profile: Profile, isActive: Boolean) {
        if (isActive && profile.strict && !isEnforcementActive()) {
            requestActivation()
        }
    }

    override fun requestEmergencyUnlock(): Boolean {
        return runBlocking {
            emergencyUnlockUseCase.requestUnlock()
        }
    }

    fun requestUninstallBlock() {
        // DevicePolicyManager.setPackagesSuspended / setUninstallBlocked are Device Owner APIs,
        // not available to a regular Device Admin. Documented as a future DeviceOwnerEnforcer path.
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val componentName = ComponentName(context, FocusBlockAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(':').any { ComponentName.unflattenFromString(it) == componentName }
    }

    companion object {
        fun isAdminEnabled(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return dpm.isAdminActive(ComponentName(context, FocusBlockDeviceAdminReceiver::class.java))
        }
    }
}

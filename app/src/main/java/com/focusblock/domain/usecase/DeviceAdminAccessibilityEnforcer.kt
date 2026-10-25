package com.focusblock.domain.usecase

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import com.focusblock.domain.model.Profile
import com.focusblock.service.admin.FocusBlockDeviceAdminReceiver
import javax.inject.Inject
import javax.inject.Singleton

/**
 * v1 strict-mode enforcer: Device Administrator + AccessibilityService.
 *
 * No other code depends on this concrete class; depend on StrictEnforcer only.
 */
@Singleton
class DeviceAdminAccessibilityEnforcer @Inject constructor(
    private val context: Context
) : StrictEnforcer {

    private val dpm: DevicePolicyManager
        get() = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent: ComponentName =
        ComponentName(context, FocusBlockDeviceAdminReceiver::class.java)

    override fun isEnforcementActive(): Boolean {
        return dpm.isAdminActive(adminComponent)
    }

    override fun requestActivation() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "FocusBlock needs Device Administrator to prevent uninstallation while Strict Mode is active.")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
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
        // Emergency unlock is off by default and requires configuration before a strict block starts.
        return false
    }

    fun requestUninstallBlock() {
        // DevicePolicyManager.setPackagesSuspended / setUninstallBlocked are Device Owner APIs,
        // not available to a regular Device Admin. Documented as a future DeviceOwnerEnforcer path.
    }

    companion object {
        fun isAdminEnabled(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return dpm.isAdminActive(ComponentName(context, FocusBlockDeviceAdminReceiver::class.java))
        }
    }
}
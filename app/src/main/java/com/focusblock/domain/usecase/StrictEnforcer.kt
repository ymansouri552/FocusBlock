package com.focusblock.domain.usecase

import com.focusblock.domain.model.Profile

/**
 * Abstracts the strict-mode enforcement mechanism.
 *
 * v1 implementation: DeviceAdminAccessibilityEnforcer (Device Administrator + AccessibilityService).
 * Future: DeviceOwnerEnforcer (setPackagesSuspended, setUninstallBlocked, DISALLOW_APPS_CONTROL, ADB provisioning).
 *
 * No other code depends on the concrete enforcer; depend on this interface only.
 */
interface StrictEnforcer {
    /** Is the enforcement mechanism currently active/enabled? */
    fun isEnforcementActive(): Boolean

    /** Request activation of the enforcement mechanism (e.g. open Device Admin / Accessibility settings). */
    fun requestActivation(): Unit

    /** Whether a strict profile is currently being enforced (admin active + profile active). */
    fun isStrictProfileActive(profile: Profile): Boolean

    /** Notify the enforcer that a strict profile has started/stopped being active. */
    fun onStrictProfileStateChanged(profile: Profile, isActive: Boolean)

    /** Emergency unlock requested; enforcer may enforce a waiting period. */
    fun requestEmergencyUnlock(): Boolean
}
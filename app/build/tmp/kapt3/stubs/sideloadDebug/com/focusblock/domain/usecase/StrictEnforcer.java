package com.focusblock.domain.usecase;

/**
 * Abstracts the strict-mode enforcement mechanism.
 *
 * v1 implementation: DeviceAdminAccessibilityEnforcer (Device Administrator + AccessibilityService).
 * Future: DeviceOwnerEnforcer (setPackagesSuspended, setUninstallBlocked, DISALLOW_APPS_CONTROL, ADB provisioning).
 *
 * No other code depends on the concrete enforcer; depend on this interface only.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\u0010\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H&J\u0018\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0003H&J\b\u0010\n\u001a\u00020\bH&J\b\u0010\u000b\u001a\u00020\u0003H&\u00a8\u0006\f"}, d2 = {"Lcom/focusblock/domain/usecase/StrictEnforcer;", "", "isEnforcementActive", "", "isStrictProfileActive", "profile", "Lcom/focusblock/domain/model/Profile;", "onStrictProfileStateChanged", "", "isActive", "requestActivation", "requestEmergencyUnlock", "app_sideloadDebug"})
public abstract interface StrictEnforcer {
    
    /**
     * Is the enforcement mechanism currently active/enabled?
     */
    public abstract boolean isEnforcementActive();
    
    /**
     * Request activation of the enforcement mechanism (e.g. open Device Admin / Accessibility settings).
     */
    public abstract void requestActivation();
    
    /**
     * Whether a strict profile is currently being enforced (admin active + profile active).
     */
    public abstract boolean isStrictProfileActive(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile);
    
    /**
     * Notify the enforcer that a strict profile has started/stopped being active.
     */
    public abstract void onStrictProfileStateChanged(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, boolean isActive);
    
    /**
     * Emergency unlock requested; enforcer may enforce a waiting period.
     */
    public abstract boolean requestEmergencyUnlock();
}
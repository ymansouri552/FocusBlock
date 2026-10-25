package com.focusblock.domain.usecase;

/**
 * v1 strict-mode enforcer: Device Administrator + AccessibilityService.
 *
 * No other code depends on this concrete class; depend on StrictEnforcer only.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u0007\u0018\u0000 \u00162\u00020\u0001:\u0001\u0016B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u000b\u001a\u00020\fH\u0016J\u0010\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J\u0018\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\fH\u0016J\b\u0010\u0013\u001a\u00020\u0011H\u0016J\b\u0010\u0014\u001a\u00020\fH\u0016J\u0006\u0010\u0015\u001a\u00020\u0011R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\u00020\b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0017"}, d2 = {"Lcom/focusblock/domain/usecase/DeviceAdminAccessibilityEnforcer;", "Lcom/focusblock/domain/usecase/StrictEnforcer;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "adminComponent", "Landroid/content/ComponentName;", "dpm", "Landroid/app/admin/DevicePolicyManager;", "getDpm", "()Landroid/app/admin/DevicePolicyManager;", "isEnforcementActive", "", "isStrictProfileActive", "profile", "Lcom/focusblock/domain/model/Profile;", "onStrictProfileStateChanged", "", "isActive", "requestActivation", "requestEmergencyUnlock", "requestUninstallBlock", "Companion", "app_sideloadDebug"})
public final class DeviceAdminAccessibilityEnforcer implements com.focusblock.domain.usecase.StrictEnforcer {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.ComponentName adminComponent = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.focusblock.domain.usecase.DeviceAdminAccessibilityEnforcer.Companion Companion = null;
    
    @javax.inject.Inject()
    public DeviceAdminAccessibilityEnforcer(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final android.app.admin.DevicePolicyManager getDpm() {
        return null;
    }
    
    @java.lang.Override()
    public boolean isEnforcementActive() {
        return false;
    }
    
    @java.lang.Override()
    public void requestActivation() {
    }
    
    @java.lang.Override()
    public boolean isStrictProfileActive(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile) {
        return false;
    }
    
    @java.lang.Override()
    public void onStrictProfileStateChanged(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, boolean isActive) {
    }
    
    @java.lang.Override()
    public boolean requestEmergencyUnlock() {
        return false;
    }
    
    public final void requestUninstallBlock() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/focusblock/domain/usecase/DeviceAdminAccessibilityEnforcer$Companion;", "", "()V", "isAdminEnabled", "", "context", "Landroid/content/Context;", "app_sideloadDebug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean isAdminEnabled(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return false;
        }
    }
}
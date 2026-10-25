package com.focusblock.domain.usecase;

/**
 * Rules governing what may be edited while a strict profile is active.
 *
 * While a strict profile is active:
 *  - NOT editable: deactivate, delete, remove apps, shorten end time, change name
 *  - ALLOWED: add apps, extend end time
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0013B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\r\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0018\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a8\u0006\u0014"}, d2 = {"Lcom/focusblock/domain/usecase/StrictModeRules;", "", "()V", "canAddApps", "Lcom/focusblock/domain/usecase/StrictModeRules$EditResult;", "profile", "Lcom/focusblock/domain/model/Profile;", "now", "Ljava/time/Instant;", "canDeactivate", "canDelete", "canEditName", "canExtendEndTime", "canRemoveApps", "canShortenEndTime", "endTime", "", "isStrictActive", "", "EditResult", "app_sideloadDebug"})
public final class StrictModeRules {
    @org.jetbrains.annotations.NotNull()
    public static final com.focusblock.domain.usecase.StrictModeRules INSTANCE = null;
    
    private StrictModeRules() {
        super();
    }
    
    /**
     * Whether [profile] is currently strict and active.
     */
    public final boolean isStrictActive(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictModeRules.EditResult canDeactivate(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictModeRules.EditResult canDelete(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictModeRules.EditResult canEditName(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictModeRules.EditResult canRemoveApps(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictModeRules.EditResult canAddApps(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictModeRules.EditResult canExtendEndTime(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictModeRules.EditResult canShortenEndTime(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    private final java.lang.String endTime(com.focusblock.domain.model.Profile profile, java.time.Instant now) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\f\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u001f\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010\u000e\u001a\u00020\u00032\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0010\u001a\u00020\u0011H\u00d6\u0001J\t\u0010\u0012\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0013"}, d2 = {"Lcom/focusblock/domain/usecase/StrictModeRules$EditResult;", "", "allowed", "", "reason", "", "(ZLjava/lang/String;)V", "getAllowed", "()Z", "getReason", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "other", "hashCode", "", "toString", "app_sideloadDebug"})
    public static final class EditResult {
        private final boolean allowed = false;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String reason = null;
        
        public EditResult(boolean allowed, @org.jetbrains.annotations.Nullable()
        java.lang.String reason) {
            super();
        }
        
        public final boolean getAllowed() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getReason() {
            return null;
        }
        
        public final boolean component1() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.focusblock.domain.usecase.StrictModeRules.EditResult copy(boolean allowed, @org.jetbrains.annotations.Nullable()
        java.lang.String reason) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}
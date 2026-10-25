package com.focusblock.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000bJ$\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\r2\b\b\u0002\u0010\n\u001a\u00020\u000bJ$\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\r2\b\b\u0002\u0010\n\u001a\u00020\u000bJ\u001a\u0010\u0012\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u0018\u0010\u0017\u001a\u00020\u00182\u0006\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000bJ\u0012\u0010\u0019\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0012\u0010\u001a\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0012\u0010\u001b\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0012\u0010\u001c\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u001a\u0010\u001d\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u001a\u0010\u001e\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u001a\u0010\u001f\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002R\u0016\u0010\u0003\u001a\n \u0005*\u0004\u0018\u00010\u00040\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/focusblock/domain/model/ScheduleEngine;", "", "()V", "zone", "Ljava/time/ZoneId;", "kotlin.jvm.PlatformType", "activeProfile", "Lcom/focusblock/domain/model/ActiveProfileInfo;", "profile", "Lcom/focusblock/domain/model/Profile;", "now", "Ljava/time/Instant;", "activeProfiles", "", "profiles", "blockedPackages", "", "", "endInstant", "schedule", "Lcom/focusblock/domain/model/Schedule;", "localNow", "Ljava/time/LocalDateTime;", "isActive", "", "oneOffEnd", "oneOffStart", "quickEnd", "quickStart", "recurringEnd", "recurringStart", "startInstant", "app_sideloadDebug"})
public final class ScheduleEngine {
    private static final java.time.ZoneId zone = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.focusblock.domain.model.ScheduleEngine INSTANCE = null;
    
    private ScheduleEngine() {
        super();
    }
    
    public final boolean isActive(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.focusblock.domain.model.ActiveProfileInfo activeProfile(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.focusblock.domain.model.ActiveProfileInfo> activeProfiles(@org.jetbrains.annotations.NotNull()
    java.util.List<com.focusblock.domain.model.Profile> profiles, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.String> blockedPackages(@org.jetbrains.annotations.NotNull()
    java.util.List<com.focusblock.domain.model.Profile> profiles, @org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    private final java.time.Instant startInstant(com.focusblock.domain.model.Schedule schedule, java.time.LocalDateTime localNow) {
        return null;
    }
    
    private final java.time.Instant endInstant(com.focusblock.domain.model.Schedule schedule, java.time.LocalDateTime localNow) {
        return null;
    }
    
    private final java.time.Instant recurringStart(com.focusblock.domain.model.Schedule schedule, java.time.LocalDateTime localNow) {
        return null;
    }
    
    private final java.time.Instant recurringEnd(com.focusblock.domain.model.Schedule schedule, java.time.LocalDateTime localNow) {
        return null;
    }
    
    private final java.time.Instant oneOffStart(com.focusblock.domain.model.Schedule schedule) {
        return null;
    }
    
    private final java.time.Instant oneOffEnd(com.focusblock.domain.model.Schedule schedule) {
        return null;
    }
    
    private final java.time.Instant quickStart(com.focusblock.domain.model.Schedule schedule) {
        return null;
    }
    
    private final java.time.Instant quickEnd(com.focusblock.domain.model.Schedule schedule) {
        return null;
    }
}
package com.focusblock.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u001b\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\b\u0018\u0000 -2\u00020\u0001:\u0001-Be\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\bH\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\bH\u00c6\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\u000b\u0010\"\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\t\u0010#\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003Ji\u0010%\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\r\u001a\u00020\u00062\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000bH\u00c6\u0001J\u0013\u0010&\u001a\u00020\'2\b\u0010(\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010)\u001a\u00020\u0006H\u00d6\u0001J\u0006\u0010*\u001a\u00020+J\t\u0010,\u001a\u00020+H\u00d6\u0001R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\r\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0013\u0010\f\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\t\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0015R\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0015R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0017R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001c\u00a8\u0006."}, d2 = {"Lcom/focusblock/domain/model/Schedule;", "", "type", "Lcom/focusblock/domain/model/ScheduleType;", "daysOfWeek", "", "", "startTime", "Ljava/time/LocalTime;", "endTime", "startDateTime", "Ljava/time/Instant;", "endDateTime", "durationMinutes", "quickStartInstant", "(Lcom/focusblock/domain/model/ScheduleType;Ljava/util/Set;Ljava/time/LocalTime;Ljava/time/LocalTime;Ljava/time/Instant;Ljava/time/Instant;ILjava/time/Instant;)V", "getDaysOfWeek", "()Ljava/util/Set;", "getDurationMinutes", "()I", "getEndDateTime", "()Ljava/time/Instant;", "getEndTime", "()Ljava/time/LocalTime;", "getQuickStartInstant", "getStartDateTime", "getStartTime", "getType", "()Lcom/focusblock/domain/model/ScheduleType;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "", "other", "hashCode", "toStorageString", "", "toString", "Companion", "app_sideloadDebug"})
public final class Schedule {
    @org.jetbrains.annotations.NotNull()
    private final com.focusblock.domain.model.ScheduleType type = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.Integer> daysOfWeek = null;
    @org.jetbrains.annotations.Nullable()
    private final java.time.LocalTime startTime = null;
    @org.jetbrains.annotations.Nullable()
    private final java.time.LocalTime endTime = null;
    @org.jetbrains.annotations.Nullable()
    private final java.time.Instant startDateTime = null;
    @org.jetbrains.annotations.Nullable()
    private final java.time.Instant endDateTime = null;
    private final int durationMinutes = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.time.Instant quickStartInstant = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.focusblock.domain.model.Schedule.Companion Companion = null;
    
    public Schedule(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.ScheduleType type, @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.Integer> daysOfWeek, @org.jetbrains.annotations.Nullable()
    java.time.LocalTime startTime, @org.jetbrains.annotations.Nullable()
    java.time.LocalTime endTime, @org.jetbrains.annotations.Nullable()
    java.time.Instant startDateTime, @org.jetbrains.annotations.Nullable()
    java.time.Instant endDateTime, int durationMinutes, @org.jetbrains.annotations.Nullable()
    java.time.Instant quickStartInstant) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.model.ScheduleType getType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.Integer> getDaysOfWeek() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.LocalTime getStartTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.LocalTime getEndTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.Instant getStartDateTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.Instant getEndDateTime() {
        return null;
    }
    
    public final int getDurationMinutes() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.Instant getQuickStartInstant() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String toStorageString() {
        return null;
    }
    
    public Schedule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.model.ScheduleType component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.Integer> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.LocalTime component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.LocalTime component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.Instant component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.Instant component6() {
        return null;
    }
    
    public final int component7() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.time.Instant component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.model.Schedule copy(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.ScheduleType type, @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.Integer> daysOfWeek, @org.jetbrains.annotations.Nullable()
    java.time.LocalTime startTime, @org.jetbrains.annotations.Nullable()
    java.time.LocalTime endTime, @org.jetbrains.annotations.Nullable()
    java.time.Instant startDateTime, @org.jetbrains.annotations.Nullable()
    java.time.Instant endDateTime, int durationMinutes, @org.jetbrains.annotations.Nullable()
    java.time.Instant quickStartInstant) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/focusblock/domain/model/Schedule$Companion;", "", "()V", "fromStorageString", "Lcom/focusblock/domain/model/Schedule;", "s", "", "app_sideloadDebug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.focusblock.domain.model.Schedule fromStorageString(@org.jetbrains.annotations.NotNull()
        java.lang.String s) {
            return null;
        }
    }
}
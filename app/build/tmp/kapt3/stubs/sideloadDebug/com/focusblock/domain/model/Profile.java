package com.focusblock.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\"\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u001b\b\u0087\b\u0018\u00002\u00020\u0001BW\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\u000e\b\u0003\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\t\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\u000e\b\u0003\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e\u00a2\u0006\u0002\u0010\u0010J\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0005H\u00c6\u0003J\u000f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00050\u0007H\u00c6\u0003J\t\u0010!\u001a\u00020\tH\u00c6\u0003J\t\u0010\"\u001a\u00020\tH\u00c6\u0003J\t\u0010#\u001a\u00020\fH\u00c6\u0003J\u000f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u00c6\u0003J[\u0010%\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u000e\b\u0003\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\f2\u000e\b\u0003\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u00c6\u0001J\u0013\u0010&\u001a\u00020\t2\b\u0010\'\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010(\u001a\u00020\u0003H\u00d6\u0001J\t\u0010)\u001a\u00020\u0005H\u00d6\u0001R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\n\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0016\u00a8\u0006*"}, d2 = {"Lcom/focusblock/domain/model/Profile;", "", "id", "", "name", "", "blockedApps", "", "strict", "", "enabled", "createdAt", "", "schedules", "", "Lcom/focusblock/domain/model/Schedule;", "(ILjava/lang/String;Ljava/util/Set;ZZJLjava/util/List;)V", "getBlockedApps", "()Ljava/util/Set;", "getCreatedAt", "()J", "getEnabled", "()Z", "getId", "()I", "getName", "()Ljava/lang/String;", "getSchedules", "()Ljava/util/List;", "getStrict", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "toString", "app_sideloadDebug"})
@androidx.room.Entity(tableName = "profiles")
public final class Profile {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final int id = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> blockedApps = null;
    private final boolean strict = false;
    private final boolean enabled = false;
    private final long createdAt = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.focusblock.domain.model.Schedule> schedules = null;
    
    public Profile(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @androidx.room.TypeConverters(value = {com.focusblock.domain.model.ProfileConverters.class})
    @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.String> blockedApps, boolean strict, boolean enabled, long createdAt, @androidx.room.TypeConverters(value = {com.focusblock.domain.model.ProfileConverters.class})
    @org.jetbrains.annotations.NotNull()
    java.util.List<com.focusblock.domain.model.Schedule> schedules) {
        super();
    }
    
    public final int getId() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.String> getBlockedApps() {
        return null;
    }
    
    public final boolean getStrict() {
        return false;
    }
    
    public final boolean getEnabled() {
        return false;
    }
    
    public final long getCreatedAt() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.focusblock.domain.model.Schedule> getSchedules() {
        return null;
    }
    
    public Profile() {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.String> component3() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.focusblock.domain.model.Schedule> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.model.Profile copy(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @androidx.room.TypeConverters(value = {com.focusblock.domain.model.ProfileConverters.class})
    @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.String> blockedApps, boolean strict, boolean enabled, long createdAt, @androidx.room.TypeConverters(value = {com.focusblock.domain.model.ProfileConverters.class})
    @org.jetbrains.annotations.NotNull()
    java.util.List<com.focusblock.domain.model.Schedule> schedules) {
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
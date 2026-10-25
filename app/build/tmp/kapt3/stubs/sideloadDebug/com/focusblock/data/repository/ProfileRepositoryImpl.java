package com.focusblock.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\u000fJ\u0014\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00120\u0011H\u0016J\u0018\u0010\u0013\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u00112\u0006\u0010\f\u001a\u00020\u0006H\u0016J\u0016\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/focusblock/data/repository/ProfileRepositoryImpl;", "Lcom/focusblock/domain/repository/ProfileRepository;", "dao", "Lcom/focusblock/data/local/room/ProfileDao;", "(Lcom/focusblock/data/local/room/ProfileDao;)V", "deleteProfile", "", "profile", "Lcom/focusblock/domain/model/Profile;", "(Lcom/focusblock/domain/model/Profile;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "editDisabled", "enableProfile", "id", "enabled", "", "(IZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllProfiles", "Lkotlinx/coroutines/flow/Flow;", "", "getProfileById", "insertProfile", "", "updateProfile", "app_sideloadDebug"})
public final class ProfileRepositoryImpl implements com.focusblock.domain.repository.ProfileRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.focusblock.data.local.room.ProfileDao dao = null;
    
    public ProfileRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.focusblock.data.local.room.ProfileDao dao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.focusblock.domain.model.Profile>> getAllProfiles() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.focusblock.domain.model.Profile> getProfileById(int id) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertProfile(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateProfile(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteProfile(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object editDisabled(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object enableProfile(int id, boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
}
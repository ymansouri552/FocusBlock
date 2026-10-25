package com.focusblock.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0012\u0010\u0007\u001a\u00020\u00062\b\b\u0001\u0010\b\u001a\u00020\tH\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0012\u0010\f\u001a\u00020\r2\b\b\u0001\u0010\b\u001a\u00020\tH\u0007J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/focusblock/di/AppModule;", "", "()V", "provideBlockLogRepository", "Lcom/focusblock/domain/repository/BlockLogRepository;", "database", "Lcom/focusblock/data/local/room/AppDatabase;", "provideDatabase", "context", "Landroid/content/Context;", "provideProfileRepository", "Lcom/focusblock/domain/repository/ProfileRepository;", "provideSettingsDataStore", "Lcom/focusblock/data/local/datastore/SettingsDataStore;", "provideStrictEnforcer", "Lcom/focusblock/domain/usecase/StrictEnforcer;", "enforcer", "Lcom/focusblock/domain/usecase/DeviceAdminAccessibilityEnforcer;", "app_sideloadDebug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.focusblock.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.data.local.room.AppDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.data.local.datastore.SettingsDataStore provideSettingsDataStore(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.repository.ProfileRepository provideProfileRepository(@org.jetbrains.annotations.NotNull()
    com.focusblock.data.local.room.AppDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.repository.BlockLogRepository provideBlockLogRepository(@org.jetbrains.annotations.NotNull()
    com.focusblock.data.local.room.AppDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictEnforcer provideStrictEnforcer(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.usecase.DeviceAdminAccessibilityEnforcer enforcer) {
        return null;
    }
}
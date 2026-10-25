package com.focusblock.data.local.room;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\b"}, d2 = {"Lcom/focusblock/data/local/room/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "blockLogDao", "Lcom/focusblock/data/local/room/BlockLogDao;", "profileDao", "Lcom/focusblock/data/local/room/ProfileDao;", "Companion", "app_sideloadDebug"})
@androidx.room.Database(entities = {com.focusblock.domain.model.Profile.class, com.focusblock.domain.model.BlockLog.class}, version = 2, exportSchema = true)
@androidx.room.TypeConverters(value = {com.focusblock.domain.model.ProfileConverters.class})
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.focusblock.data.local.room.AppDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.focusblock.data.local.room.AppDatabase.Companion Companion = null;
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.focusblock.data.local.room.ProfileDao profileDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.focusblock.data.local.room.BlockLogDao blockLogDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/focusblock/data/local/room/AppDatabase$Companion;", "", "()V", "INSTANCE", "Lcom/focusblock/data/local/room/AppDatabase;", "getInstance", "context", "Landroid/content/Context;", "app_sideloadDebug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.focusblock.data.local.room.AppDatabase getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}
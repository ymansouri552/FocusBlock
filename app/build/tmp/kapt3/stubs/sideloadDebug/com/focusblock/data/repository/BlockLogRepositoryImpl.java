package com.focusblock.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0005\u001a\u00020\u0006H\u0016J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH\u0016J\u001c\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\f\u001a\u00020\rH\u0016J\u001c\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u000f\u001a\u00020\u0010H\u0016J\u0010\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\nH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/focusblock/data/repository/BlockLogRepositoryImpl;", "Lcom/focusblock/domain/repository/BlockLogRepository;", "dao", "Lcom/focusblock/data/local/room/BlockLogDao;", "(Lcom/focusblock/data/local/room/BlockLogDao;)V", "clearAllLogs", "", "getAllLogs", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/focusblock/domain/model/BlockLog;", "getLogsForDay", "day", "", "getLogsForPackage", "packageName", "", "insert", "log", "app_sideloadDebug"})
public final class BlockLogRepositoryImpl implements com.focusblock.domain.repository.BlockLogRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.focusblock.data.local.room.BlockLogDao dao = null;
    
    public BlockLogRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.focusblock.data.local.room.BlockLogDao dao) {
        super();
    }
    
    @java.lang.Override()
    public long insert(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.BlockLog log) {
        return 0L;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.focusblock.domain.model.BlockLog>> getLogsForDay(long day) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.focusblock.domain.model.BlockLog>> getLogsForPackage(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.focusblock.domain.model.BlockLog>> getAllLogs() {
        return null;
    }
    
    @java.lang.Override()
    public int clearAllLogs() {
        return 0;
    }
}
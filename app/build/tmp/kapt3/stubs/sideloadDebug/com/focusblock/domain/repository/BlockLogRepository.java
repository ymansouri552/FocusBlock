package com.focusblock.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\u0014\u0010\u0004\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u0005H&J\u001c\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u00052\u0006\u0010\t\u001a\u00020\nH&J\u001c\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u00052\u0006\u0010\f\u001a\u00020\rH&J\u0010\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\u0007H&\u00a8\u0006\u0010"}, d2 = {"Lcom/focusblock/domain/repository/BlockLogRepository;", "", "clearAllLogs", "", "getAllLogs", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/focusblock/domain/model/BlockLog;", "getLogsForDay", "day", "", "getLogsForPackage", "packageName", "", "insert", "log", "app_sideloadDebug"})
public abstract interface BlockLogRepository {
    
    public abstract long insert(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.BlockLog log);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.focusblock.domain.model.BlockLog>> getLogsForDay(long day);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.focusblock.domain.model.BlockLog>> getLogsForPackage(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.focusblock.domain.model.BlockLog>> getAllLogs();
    
    public abstract int clearAllLogs();
}
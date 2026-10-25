package com.focusblock.domain.usecase;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\"\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J,\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/focusblock/domain/usecase/QuickBlockUseCase;", "", "repository", "Lcom/focusblock/domain/repository/ProfileRepository;", "(Lcom/focusblock/domain/repository/ProfileRepository;)V", "startQuickBlock", "", "profileName", "", "packageNames", "", "durationMinutes", "", "(Ljava/lang/String;Ljava/util/Set;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_sideloadDebug"})
public final class QuickBlockUseCase {
    @org.jetbrains.annotations.NotNull()
    private final com.focusblock.domain.repository.ProfileRepository repository = null;
    
    @javax.inject.Inject()
    public QuickBlockUseCase(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.repository.ProfileRepository repository) {
        super();
    }
    
    /**
     * Creates a "quick block" profile blocking [packageNames] for [durationMinutes].
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object startQuickBlock(@org.jetbrains.annotations.NotNull()
    java.lang.String profileName, @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.String> packageNames, int durationMinutes, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
}
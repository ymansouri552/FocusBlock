package com.focusblock.ui.profile;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001:\u00013B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\u001c\u001a\u00020\u001d2\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020 0\u001fJ\u0010\u0010!\u001a\u00020\"2\b\b\u0002\u0010#\u001a\u00020$J\u0016\u0010%\u001a\u00020\u001d2\u000e\b\u0002\u0010&\u001a\b\u0012\u0004\u0012\u00020\u001d0\'J\u000e\u0010(\u001a\u00020\u001d2\u0006\u0010\u0011\u001a\u00020\u000fJ\u000e\u0010)\u001a\u00020\u001d2\u0006\u0010*\u001a\u00020\rJ\u000e\u0010+\u001a\u00020\u001d2\u0006\u0010,\u001a\u00020 J\u0006\u0010-\u001a\u00020\u001dJ\u000e\u0010.\u001a\u00020\u001d2\u0006\u0010/\u001a\u00020 J\u000e\u00100\u001a\u00020\u001d2\u0006\u00101\u001a\u000202R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R+\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000f8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0016\u0010\u0017\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001b\u00a8\u00064"}, d2 = {"Lcom/focusblock/ui/profile/ProfileEditorViewModel;", "Landroidx/lifecycle/ViewModel;", "saveProfileUseCase", "Lcom/focusblock/domain/usecase/SaveProfileUseCase;", "deleteProfileUseCase", "Lcom/focusblock/domain/usecase/DeleteProfileUseCase;", "profileRepository", "Lcom/focusblock/domain/repository/ProfileRepository;", "(Lcom/focusblock/domain/usecase/SaveProfileUseCase;Lcom/focusblock/domain/usecase/DeleteProfileUseCase;Lcom/focusblock/domain/repository/ProfileRepository;)V", "_ui", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/focusblock/ui/profile/ProfileEditorViewModel$ProfileEditorUiState;", "loadedId", "", "original", "Lcom/focusblock/domain/model/Profile;", "<set-?>", "profile", "getProfile", "()Lcom/focusblock/domain/model/Profile;", "setProfile", "(Lcom/focusblock/domain/model/Profile;)V", "profile$delegate", "Landroidx/compose/runtime/MutableState;", "ui", "Lkotlinx/coroutines/flow/StateFlow;", "getUi", "()Lkotlinx/coroutines/flow/StateFlow;", "addApps", "", "apps", "", "", "canSave", "Lcom/focusblock/domain/usecase/StrictModeRules$EditResult;", "now", "Ljava/time/Instant;", "delete", "onDeleted", "Lkotlin/Function0;", "load", "loadById", "id", "removeApp", "app", "save", "setName", "name", "toggleStrict", "enabled", "", "ProfileEditorUiState", "app_sideloadDebug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ProfileEditorViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.focusblock.domain.usecase.SaveProfileUseCase saveProfileUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.focusblock.domain.usecase.DeleteProfileUseCase deleteProfileUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.focusblock.domain.repository.ProfileRepository profileRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.focusblock.ui.profile.ProfileEditorViewModel.ProfileEditorUiState> _ui = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.focusblock.ui.profile.ProfileEditorViewModel.ProfileEditorUiState> ui = null;
    
    /**
     * Snapshot of the profile as loaded from the database (id == 0 when new).
     */
    @org.jetbrains.annotations.NotNull()
    private com.focusblock.domain.model.Profile original;
    private int loadedId = -2147483648;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState profile$delegate = null;
    
    @javax.inject.Inject()
    public ProfileEditorViewModel(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.usecase.SaveProfileUseCase saveProfileUseCase, @org.jetbrains.annotations.NotNull()
    com.focusblock.domain.usecase.DeleteProfileUseCase deleteProfileUseCase, @org.jetbrains.annotations.NotNull()
    com.focusblock.domain.repository.ProfileRepository profileRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.focusblock.ui.profile.ProfileEditorViewModel.ProfileEditorUiState> getUi() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.model.Profile getProfile() {
        return null;
    }
    
    private final void setProfile(com.focusblock.domain.model.Profile p0) {
    }
    
    public final void load(@org.jetbrains.annotations.NotNull()
    com.focusblock.domain.model.Profile profile) {
    }
    
    public final void loadById(int id) {
    }
    
    public final void setName(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void toggleStrict(boolean enabled) {
    }
    
    public final void addApps(@org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.String> apps) {
    }
    
    public final void removeApp(@org.jetbrains.annotations.NotNull()
    java.lang.String app) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.focusblock.domain.usecase.StrictModeRules.EditResult canSave(@org.jetbrains.annotations.NotNull()
    java.time.Instant now) {
        return null;
    }
    
    public final void save() {
    }
    
    public final void delete(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDeleted) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0011\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B9\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0015\u001a\u0004\u0018\u00010\bH\u00c6\u0003J=\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\bH\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00032\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001J\t\u0010\u001b\u001a\u00020\bH\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000bR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000b\u00a8\u0006\u001c"}, d2 = {"Lcom/focusblock/ui/profile/ProfileEditorViewModel$ProfileEditorUiState;", "", "appPickerOpen", "", "scheduleOpen", "strictConfirmOpen", "saving", "error", "", "(ZZZZLjava/lang/String;)V", "getAppPickerOpen", "()Z", "getError", "()Ljava/lang/String;", "getSaving", "getScheduleOpen", "getStrictConfirmOpen", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "app_sideloadDebug"})
    public static final class ProfileEditorUiState {
        private final boolean appPickerOpen = false;
        private final boolean scheduleOpen = false;
        private final boolean strictConfirmOpen = false;
        private final boolean saving = false;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String error = null;
        
        public ProfileEditorUiState(boolean appPickerOpen, boolean scheduleOpen, boolean strictConfirmOpen, boolean saving, @org.jetbrains.annotations.Nullable()
        java.lang.String error) {
            super();
        }
        
        public final boolean getAppPickerOpen() {
            return false;
        }
        
        public final boolean getScheduleOpen() {
            return false;
        }
        
        public final boolean getStrictConfirmOpen() {
            return false;
        }
        
        public final boolean getSaving() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getError() {
            return null;
        }
        
        public ProfileEditorUiState() {
            super();
        }
        
        public final boolean component1() {
            return false;
        }
        
        public final boolean component2() {
            return false;
        }
        
        public final boolean component3() {
            return false;
        }
        
        public final boolean component4() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.focusblock.ui.profile.ProfileEditorViewModel.ProfileEditorUiState copy(boolean appPickerOpen, boolean scheduleOpen, boolean strictConfirmOpen, boolean saving, @org.jetbrains.annotations.Nullable()
        java.lang.String error) {
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
package com.focusblock.ui.home;

import com.focusblock.domain.usecase.BlockedAppsUseCase;
import com.focusblock.domain.usecase.GetProfilesUseCase;
import com.focusblock.domain.usecase.ToggleProfileUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<GetProfilesUseCase> getProfilesUseCaseProvider;

  private final Provider<BlockedAppsUseCase> blockedAppsUseCaseProvider;

  private final Provider<ToggleProfileUseCase> toggleProfileUseCaseProvider;

  public HomeViewModel_Factory(Provider<GetProfilesUseCase> getProfilesUseCaseProvider,
      Provider<BlockedAppsUseCase> blockedAppsUseCaseProvider,
      Provider<ToggleProfileUseCase> toggleProfileUseCaseProvider) {
    this.getProfilesUseCaseProvider = getProfilesUseCaseProvider;
    this.blockedAppsUseCaseProvider = blockedAppsUseCaseProvider;
    this.toggleProfileUseCaseProvider = toggleProfileUseCaseProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(getProfilesUseCaseProvider.get(), blockedAppsUseCaseProvider.get(), toggleProfileUseCaseProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<GetProfilesUseCase> getProfilesUseCaseProvider,
      Provider<BlockedAppsUseCase> blockedAppsUseCaseProvider,
      Provider<ToggleProfileUseCase> toggleProfileUseCaseProvider) {
    return new HomeViewModel_Factory(getProfilesUseCaseProvider, blockedAppsUseCaseProvider, toggleProfileUseCaseProvider);
  }

  public static HomeViewModel newInstance(GetProfilesUseCase getProfilesUseCase,
      BlockedAppsUseCase blockedAppsUseCase, ToggleProfileUseCase toggleProfileUseCase) {
    return new HomeViewModel(getProfilesUseCase, blockedAppsUseCase, toggleProfileUseCase);
  }
}

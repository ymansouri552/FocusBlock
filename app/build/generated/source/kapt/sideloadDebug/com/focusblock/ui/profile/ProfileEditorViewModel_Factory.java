package com.focusblock.ui.profile;

import com.focusblock.domain.repository.ProfileRepository;
import com.focusblock.domain.usecase.DeleteProfileUseCase;
import com.focusblock.domain.usecase.SaveProfileUseCase;
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
public final class ProfileEditorViewModel_Factory implements Factory<ProfileEditorViewModel> {
  private final Provider<SaveProfileUseCase> saveProfileUseCaseProvider;

  private final Provider<DeleteProfileUseCase> deleteProfileUseCaseProvider;

  private final Provider<ProfileRepository> profileRepositoryProvider;

  public ProfileEditorViewModel_Factory(Provider<SaveProfileUseCase> saveProfileUseCaseProvider,
      Provider<DeleteProfileUseCase> deleteProfileUseCaseProvider,
      Provider<ProfileRepository> profileRepositoryProvider) {
    this.saveProfileUseCaseProvider = saveProfileUseCaseProvider;
    this.deleteProfileUseCaseProvider = deleteProfileUseCaseProvider;
    this.profileRepositoryProvider = profileRepositoryProvider;
  }

  @Override
  public ProfileEditorViewModel get() {
    return newInstance(saveProfileUseCaseProvider.get(), deleteProfileUseCaseProvider.get(), profileRepositoryProvider.get());
  }

  public static ProfileEditorViewModel_Factory create(
      Provider<SaveProfileUseCase> saveProfileUseCaseProvider,
      Provider<DeleteProfileUseCase> deleteProfileUseCaseProvider,
      Provider<ProfileRepository> profileRepositoryProvider) {
    return new ProfileEditorViewModel_Factory(saveProfileUseCaseProvider, deleteProfileUseCaseProvider, profileRepositoryProvider);
  }

  public static ProfileEditorViewModel newInstance(SaveProfileUseCase saveProfileUseCase,
      DeleteProfileUseCase deleteProfileUseCase, ProfileRepository profileRepository) {
    return new ProfileEditorViewModel(saveProfileUseCase, deleteProfileUseCase, profileRepository);
  }
}

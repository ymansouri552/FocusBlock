package com.focusblock.domain.usecase;

import com.focusblock.domain.repository.ProfileRepository;
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
public final class SaveProfileUseCase_Factory implements Factory<SaveProfileUseCase> {
  private final Provider<ProfileRepository> repositoryProvider;

  public SaveProfileUseCase_Factory(Provider<ProfileRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SaveProfileUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static SaveProfileUseCase_Factory create(Provider<ProfileRepository> repositoryProvider) {
    return new SaveProfileUseCase_Factory(repositoryProvider);
  }

  public static SaveProfileUseCase newInstance(ProfileRepository repository) {
    return new SaveProfileUseCase(repository);
  }
}

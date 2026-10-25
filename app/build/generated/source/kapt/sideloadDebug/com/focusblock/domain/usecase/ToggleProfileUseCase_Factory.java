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
public final class ToggleProfileUseCase_Factory implements Factory<ToggleProfileUseCase> {
  private final Provider<ProfileRepository> repositoryProvider;

  public ToggleProfileUseCase_Factory(Provider<ProfileRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ToggleProfileUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ToggleProfileUseCase_Factory create(
      Provider<ProfileRepository> repositoryProvider) {
    return new ToggleProfileUseCase_Factory(repositoryProvider);
  }

  public static ToggleProfileUseCase newInstance(ProfileRepository repository) {
    return new ToggleProfileUseCase(repository);
  }
}

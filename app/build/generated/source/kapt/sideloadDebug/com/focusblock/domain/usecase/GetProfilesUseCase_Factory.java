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
public final class GetProfilesUseCase_Factory implements Factory<GetProfilesUseCase> {
  private final Provider<ProfileRepository> repositoryProvider;

  public GetProfilesUseCase_Factory(Provider<ProfileRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetProfilesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetProfilesUseCase_Factory create(Provider<ProfileRepository> repositoryProvider) {
    return new GetProfilesUseCase_Factory(repositoryProvider);
  }

  public static GetProfilesUseCase newInstance(ProfileRepository repository) {
    return new GetProfilesUseCase(repository);
  }
}

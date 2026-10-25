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
public final class DeleteProfileUseCase_Factory implements Factory<DeleteProfileUseCase> {
  private final Provider<ProfileRepository> repositoryProvider;

  public DeleteProfileUseCase_Factory(Provider<ProfileRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteProfileUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteProfileUseCase_Factory create(
      Provider<ProfileRepository> repositoryProvider) {
    return new DeleteProfileUseCase_Factory(repositoryProvider);
  }

  public static DeleteProfileUseCase newInstance(ProfileRepository repository) {
    return new DeleteProfileUseCase(repository);
  }
}

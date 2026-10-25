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
public final class QuickBlockUseCase_Factory implements Factory<QuickBlockUseCase> {
  private final Provider<ProfileRepository> repositoryProvider;

  public QuickBlockUseCase_Factory(Provider<ProfileRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public QuickBlockUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static QuickBlockUseCase_Factory create(Provider<ProfileRepository> repositoryProvider) {
    return new QuickBlockUseCase_Factory(repositoryProvider);
  }

  public static QuickBlockUseCase newInstance(ProfileRepository repository) {
    return new QuickBlockUseCase(repository);
  }
}

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
public final class BlockedAppsUseCase_Factory implements Factory<BlockedAppsUseCase> {
  private final Provider<ProfileRepository> repositoryProvider;

  public BlockedAppsUseCase_Factory(Provider<ProfileRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BlockedAppsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static BlockedAppsUseCase_Factory create(Provider<ProfileRepository> repositoryProvider) {
    return new BlockedAppsUseCase_Factory(repositoryProvider);
  }

  public static BlockedAppsUseCase newInstance(ProfileRepository repository) {
    return new BlockedAppsUseCase(repository);
  }
}

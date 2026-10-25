package com.focusblock.di;

import com.focusblock.data.local.room.AppDatabase;
import com.focusblock.domain.repository.ProfileRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideProfileRepositoryFactory implements Factory<ProfileRepository> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideProfileRepositoryFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ProfileRepository get() {
    return provideProfileRepository(databaseProvider.get());
  }

  public static AppModule_ProvideProfileRepositoryFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideProfileRepositoryFactory(databaseProvider);
  }

  public static ProfileRepository provideProfileRepository(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideProfileRepository(database));
  }
}

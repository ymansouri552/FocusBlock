package com.focusblock.di;

import com.focusblock.data.local.room.AppDatabase;
import com.focusblock.domain.repository.BlockLogRepository;
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
public final class AppModule_ProvideBlockLogRepositoryFactory implements Factory<BlockLogRepository> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideBlockLogRepositoryFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BlockLogRepository get() {
    return provideBlockLogRepository(databaseProvider.get());
  }

  public static AppModule_ProvideBlockLogRepositoryFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideBlockLogRepositoryFactory(databaseProvider);
  }

  public static BlockLogRepository provideBlockLogRepository(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBlockLogRepository(database));
  }
}

package com.focusblock.di;

import com.focusblock.domain.usecase.DeviceAdminAccessibilityEnforcer;
import com.focusblock.domain.usecase.StrictEnforcer;
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
public final class AppModule_ProvideStrictEnforcerFactory implements Factory<StrictEnforcer> {
  private final Provider<DeviceAdminAccessibilityEnforcer> enforcerProvider;

  public AppModule_ProvideStrictEnforcerFactory(
      Provider<DeviceAdminAccessibilityEnforcer> enforcerProvider) {
    this.enforcerProvider = enforcerProvider;
  }

  @Override
  public StrictEnforcer get() {
    return provideStrictEnforcer(enforcerProvider.get());
  }

  public static AppModule_ProvideStrictEnforcerFactory create(
      Provider<DeviceAdminAccessibilityEnforcer> enforcerProvider) {
    return new AppModule_ProvideStrictEnforcerFactory(enforcerProvider);
  }

  public static StrictEnforcer provideStrictEnforcer(DeviceAdminAccessibilityEnforcer enforcer) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideStrictEnforcer(enforcer));
  }
}

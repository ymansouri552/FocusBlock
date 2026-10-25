package com.focusblock.domain.usecase;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class DeviceAdminAccessibilityEnforcer_Factory implements Factory<DeviceAdminAccessibilityEnforcer> {
  private final Provider<Context> contextProvider;

  public DeviceAdminAccessibilityEnforcer_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DeviceAdminAccessibilityEnforcer get() {
    return newInstance(contextProvider.get());
  }

  public static DeviceAdminAccessibilityEnforcer_Factory create(Provider<Context> contextProvider) {
    return new DeviceAdminAccessibilityEnforcer_Factory(contextProvider);
  }

  public static DeviceAdminAccessibilityEnforcer newInstance(Context context) {
    return new DeviceAdminAccessibilityEnforcer(context);
  }
}

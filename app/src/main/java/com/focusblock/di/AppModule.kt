package com.focusblock.di

import android.content.Context
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.data.local.room.AppDatabase
import com.focusblock.data.repository.AppUsageRepositoryImpl
import com.focusblock.data.repository.BlockLogRepositoryImpl
import com.focusblock.data.repository.ProfileRepositoryImpl
import com.focusblock.data.repository.UsageStatsRepositoryImpl
import com.focusblock.domain.repository.AppUsageRepository
import com.focusblock.domain.repository.BlockLogRepository
import com.focusblock.domain.repository.ProfileRepository
import com.focusblock.domain.repository.UsageStatsRepository
import com.focusblock.domain.usecase.DeviceAdminAccessibilityEnforcer
import com.focusblock.domain.usecase.GetInstalledAppsUseCase
import com.focusblock.domain.usecase.StrictEnforcer
import com.focusblock.domain.usecase.SystemPermissionChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(database: AppDatabase): ProfileRepository {
        return ProfileRepositoryImpl(database.profileDao())
    }

    @Provides
    @Singleton
    fun provideBlockLogRepository(database: AppDatabase): BlockLogRepository {
        return BlockLogRepositoryImpl(database.blockLogDao())
    }

    @Provides
    @Singleton
    fun provideAppUsageRepository(database: AppDatabase): AppUsageRepository {
        return AppUsageRepositoryImpl(database.appUsageDao())
    }

    @Provides
    @Singleton
    fun provideUsageStatsRepository(
        @ApplicationContext context: Context,
        systemPermissionChecker: SystemPermissionChecker,
        getInstalledAppsUseCase: GetInstalledAppsUseCase,
        profileRepository: ProfileRepository
    ): UsageStatsRepository {
        return UsageStatsRepositoryImpl(
            context = context,
            systemPermissionChecker = systemPermissionChecker,
            getInstalledAppsUseCase = getInstalledAppsUseCase,
            profileRepository = profileRepository
        )
    }

    @Provides
    @Singleton
    fun provideStrictEnforcer(enforcer: DeviceAdminAccessibilityEnforcer): StrictEnforcer {
        return enforcer
    }
}

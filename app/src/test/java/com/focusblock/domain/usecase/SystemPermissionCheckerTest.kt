package com.focusblock.domain.usecase

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SystemPermissionCheckerTest {

    private lateinit var context: Context
    private lateinit var permissionChecker: SystemPermissionChecker

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        permissionChecker = SystemPermissionChecker(context)
    }

    @Test
    fun checkPermissions_returnsValidState() {
        val state = permissionChecker.checkPermissions()
        assertNotNull(state)
    }

    @Test
    fun permissionsState_evaluatesAllRequiredGrantedProperly() {
        val stateAllMissing = SystemPermissionsState(
            isAccessibilityGranted = false,
            isDeviceAdminGranted = false,
            isBatteryOptimizationIgnored = false
        )
        assertFalse(stateAllMissing.allRequiredGranted)
        assertTrue(stateAllMissing.hasAnyWarning)

        val stateOnlyAccessibility = SystemPermissionsState(
            isAccessibilityGranted = true,
            isDeviceAdminGranted = false,
            isBatteryOptimizationIgnored = false
        )
        assertTrue(stateOnlyAccessibility.allRequiredGranted)
        assertTrue(stateOnlyAccessibility.hasAnyWarning)

        val stateAllGranted = SystemPermissionsState(
            isAccessibilityGranted = true,
            isDeviceAdminGranted = true,
            isBatteryOptimizationIgnored = true
        )
        assertTrue(stateAllGranted.allRequiredGranted)
        assertFalse(stateAllGranted.hasAnyWarning)
    }

    @Test
    fun getBatteryOptimizationIntent_returnsValidIntent() {
        val intent = permissionChecker.getBatteryOptimizationIntent()
        assertNotNull(intent)
        assertEquals(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, intent.action)
        assertEquals("package:${context.packageName}", intent.dataString)
    }

    @Test
    fun launchBatteryOptimizationSettings_doesNotCrash() {
        val result = permissionChecker.launchBatteryOptimizationSettings()
        assertTrue(result)
    }

    @Test
    fun isUsageStatsGranted_executesWithoutCrash() {
        val result = permissionChecker.isUsageStatsGranted()
        // In Robolectric test context without granted appops, defaults to false
        assertFalse(result)
    }

    @Test
    fun getUsageAccessSettingsIntent_returnsValidIntent() {
        val intent = permissionChecker.getUsageAccessSettingsIntent()
        assertNotNull(intent)
        assertEquals(Settings.ACTION_USAGE_ACCESS_SETTINGS, intent.action)
    }

    @Test
    fun launchUsageAccessSettings_doesNotCrash() {
        val result = permissionChecker.launchUsageAccessSettings()
        assertTrue(result)
    }
}

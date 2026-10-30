package com.focusblock.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
}

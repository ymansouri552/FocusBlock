package com.focusblock.domain.usecase

import com.focusblock.data.local.datastore.StrictLevel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StrictProtectionEvaluatorTest {

    private val evaluator = StrictProtectionEvaluator()

    @Test
    fun inactive_strict_mode_does_not_block_anything() {
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.accessibility.AccessibilitySettings",
                strictLevel = StrictLevel.AGGRESSIVE,
                isStrictActive = false
            )
        )
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.packageinstaller",
                className = "com.android.packageinstaller.UninstallerActivity",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = false
            )
        )
    }

    @Test
    fun aggressive_strict_mode_blocks_entire_settings_app() {
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.DisplaySettings",
                strictLevel = StrictLevel.AGGRESSIVE,
                isStrictActive = true
            )
        )
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.samsung.android.settings",
                className = "com.samsung.android.settings.wifi.WifiSettings",
                strictLevel = StrictLevel.AGGRESSIVE,
                isStrictActive = true
            )
        )
        // Installer is also blocked
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.google.android.packageinstaller",
                className = null,
                strictLevel = StrictLevel.AGGRESSIVE,
                isStrictActive = true
            )
        )
        // Regular non-settings app is not blocked by tamper evaluator
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.example.calculator",
                className = "com.example.calculator.MainActivity",
                strictLevel = StrictLevel.AGGRESSIVE,
                isStrictActive = true
            )
        )
    }

    @Test
    fun aggressive_strict_mode_blocks_browsers_to_prevent_web_bypass() {
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.chrome",
                className = "com.google.android.apps.chrome.Main",
                strictLevel = StrictLevel.AGGRESSIVE,
                isStrictActive = true
            )
        )
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "org.mozilla.firefox",
                className = null,
                strictLevel = StrictLevel.AGGRESSIVE,
                isStrictActive = true
            )
        )
        // Targeted strict mode does not blanket block browsers
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.chrome",
                className = "com.google.android.apps.chrome.Main",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = true
            )
        )
    }

    @Test
    fun targeted_strict_mode_permits_safe_settings_screens() {
        // Safe settings like Display or Sound should be permitted
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.DisplaySettings",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = true
            )
        )
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.SoundSettings",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = true
            )
        )
    }

    @Test
    fun targeted_strict_mode_blocks_admin_accessibility_and_app_info_screens() {
        // Accessibility screen
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.accessibility.AccessibilitySettings",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = true
            )
        )
        // Device Admin screen
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.DeviceAdminAdd",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = true
            )
        )
        // App Info / Installed Details (force stop / clear data / uninstall)
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.applications.InstalledAppDetails",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = true
            )
        )
        // Date & Time settings screen (prevents clock roll-forward tampering)
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.datetime.DateTimeSettings",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = true
            )
        )
        // Package installer / uninstaller
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.packageinstaller",
                className = "com.android.packageinstaller.UninstallerActivity",
                strictLevel = StrictLevel.TARGETED,
                isStrictActive = true
            )
        )
    }
}

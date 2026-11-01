package com.focusblock.domain.usecase

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
                isStrictActive = false,
                blockBrowsers = true
            )
        )
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.packageinstaller",
                className = "com.android.packageinstaller.UninstallerActivity",
                isStrictActive = false,
                blockBrowsers = true
            )
        )
    }

    @Test
    fun active_strict_mode_blocks_entire_settings_and_installers() {
        // AOSP Settings
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.settings",
                className = "com.android.settings.DisplaySettings",
                isStrictActive = true
            )
        )
        // Samsung Settings
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.samsung.android.settings",
                className = "com.samsung.android.settings.wifi.WifiSettings",
                isStrictActive = true
            )
        )
        // Xiaomi Security / Settings
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.miui.securitycenter",
                className = null,
                isStrictActive = true
            )
        )
        // Package installer (uninstallation attempt)
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.google.android.packageinstaller",
                className = null,
                isStrictActive = true
            )
        )
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.packageinstaller",
                className = "com.android.packageinstaller.UninstallerActivity",
                isStrictActive = true
            )
        )
    }

    @Test
    fun active_strict_mode_blocks_browsers_only_when_enabled() {
        // When blockBrowsers = true, browsers are blocked to prevent web bypass
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.chrome",
                className = "com.google.android.apps.chrome.Main",
                isStrictActive = true,
                blockBrowsers = true
            )
        )
        assertTrue(
            evaluator.shouldBlockTamperAttempt(
                pkg = "org.mozilla.firefox",
                className = null,
                isStrictActive = true,
                blockBrowsers = true
            )
        )

        // When blockBrowsers = false, browsers are permitted
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.android.chrome",
                className = "com.google.android.apps.chrome.Main",
                isStrictActive = true,
                blockBrowsers = false
            )
        )
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "org.mozilla.firefox",
                className = null,
                isStrictActive = true,
                blockBrowsers = false
            )
        )
    }

    @Test
    fun active_strict_mode_does_not_block_regular_apps() {
        // Regular non-settings apps are never blocked by tamper protection
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.example.calculator",
                className = "com.example.calculator.MainActivity",
                isStrictActive = true
            )
        )
        assertFalse(
            evaluator.shouldBlockTamperAttempt(
                pkg = "com.google.android.calendar",
                className = null,
                isStrictActive = true
            )
        )
    }
}

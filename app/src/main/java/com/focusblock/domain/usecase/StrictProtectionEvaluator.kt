package com.focusblock.domain.usecase

import com.focusblock.data.local.datastore.StrictLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StrictProtectionEvaluator @Inject constructor() {

    companion object {
        val SETTINGS_PACKAGES = setOf(
            "com.android.settings",
            "com.google.android.settings",
            "com.samsung.android.settings",
            "com.miui.securitycenter",
            "com.miui.securityadd",
            "com.coloros.safecenter",
            "com.oplus.safecenter",
            "com.huawei.systemmanager",
            "com.vivo.permissionmanager"
        )

        val INSTALLER_PACKAGES = setOf(
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.samsung.android.packageinstaller"
        )

        val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "com.google.android.apps.chrome",
            "org.mozilla.firefox",
            "com.sec.android.app.sbrowser",
            "com.microsoft.emmx",
            "com.brave.browser",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.opera.touch",
            "com.duckduckgo.mobile.android",
            "com.kiwibrowser.browser",
            "com.vivaldi.browser",
            "org.torproject.torbrowser",
            "com.UCMobile.intl"
        )

        val TARGETED_CLASS_KEYWORDS = listOf(
            "accessibility",
            "deviceadmin",
            "devicepolicy",
            "installedappdetails",
            "appinfo",
            "appdetails",
            "manageapplications",
            "applicationsdetails",
            "appstoragesettings",
            "uninstall",
            "datetime",
            "dateandtime",
            "timesettings",
            "datesettings"
        )
    }

    fun isSettingsOrSecurityApp(pkg: String): Boolean {
        return SETTINGS_PACKAGES.contains(pkg) || pkg.endsWith(".settings")
    }

    fun isInstallerApp(pkg: String): Boolean {
        return INSTALLER_PACKAGES.contains(pkg) || pkg.endsWith(".packageinstaller")
    }

    fun isBrowserApp(pkg: String): Boolean {
        return BROWSER_PACKAGES.contains(pkg) ||
            pkg.endsWith(".browser") ||
            pkg.split('.').any { it == "browser" }
    }

    fun shouldBlockTamperAttempt(
        pkg: String,
        className: String? = null,
        isStrictActive: Boolean,
        blockBrowsers: Boolean = false
    ): Boolean {
        if (!isStrictActive) return false

        // 1. Any package installer/uninstaller is blocked during active strict mode to prevent uninstalling FocusBlock
        if (isInstallerApp(pkg)) {
            return true
        }

        // 2. Settings & Security app protection: completely block Settings app to prevent disabling
        // Accessibility, removing Device Admin, Force Stopping, clearing data, or altering Date/Time.
        if (isSettingsOrSecurityApp(pkg)) {
            return true
        }

        // 3. Web browsers blocked if enabled to prevent mobile web bypass of blocked apps
        if (blockBrowsers && isBrowserApp(pkg)) {
            return true
        }

        return false
    }

    fun shouldBlockTamperAttempt(
        pkg: String,
        className: String?,
        strictLevel: StrictLevel,
        isStrictActive: Boolean
    ): Boolean {
        return shouldBlockTamperAttempt(
            pkg = pkg,
            className = className,
            isStrictActive = isStrictActive,
            blockBrowsers = (strictLevel == StrictLevel.AGGRESSIVE)
        )
    }
}

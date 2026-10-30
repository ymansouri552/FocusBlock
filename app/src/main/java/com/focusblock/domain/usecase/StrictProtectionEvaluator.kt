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
        return BROWSER_PACKAGES.contains(pkg) || pkg.contains("browser")
    }

    fun shouldBlockTamperAttempt(
        pkg: String,
        className: String?,
        strictLevel: StrictLevel,
        isStrictActive: Boolean
    ): Boolean {
        if (!isStrictActive) return false

        // 1. Any package installer/uninstaller is blocked during active strict mode to prevent uninstalling FocusBlock
        if (isInstallerApp(pkg)) {
            return true
        }

        // 2. In Aggressive Strict Mode, block browsers to prevent mobile web bypass of blocked apps
        if (strictLevel == StrictLevel.AGGRESSIVE && isBrowserApp(pkg)) {
            return true
        }

        // 2. Settings & Security app protection
        if (isSettingsOrSecurityApp(pkg)) {
            if (strictLevel == StrictLevel.AGGRESSIVE) {
                // Aggressive mode: completely block Settings app
                return true
            }

            // Targeted mode: block only sensitive screens (admin, accessibility, app info / force stop)
            if (className != null) {
                val lowerClass = className.lowercase()
                return TARGETED_CLASS_KEYWORDS.any { lowerClass.contains(it) }
            }
        }

        return false
    }
}

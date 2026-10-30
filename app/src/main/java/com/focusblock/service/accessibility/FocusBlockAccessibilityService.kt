package com.focusblock.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.data.local.datastore.SettingsKeys
import com.focusblock.data.local.datastore.StrictLevel
import com.focusblock.domain.model.ActiveProfileInfo
import com.focusblock.domain.model.BlockLog
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.model.StrictModeState
import com.focusblock.domain.repository.BlockLogRepository
import com.focusblock.domain.repository.ProfileRepository
import com.focusblock.domain.usecase.StrictModeRules
import com.focusblock.domain.usecase.StrictProtectionEvaluator
import com.focusblock.ui.block.BlockScreenActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class FocusBlockAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var strictEvaluator: StrictProtectionEvaluator

    @Inject
    lateinit var blockLogRepository: BlockLogRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    @Volatile
    private var cachedProfiles: List<Profile> = emptyList()

    @Volatile
    private var cachedStrictState: StrictModeState = StrictModeState()

    @Volatile
    private var cachedStrictLevel: StrictLevel = StrictLevel.TARGETED

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo
        if (info != null) {
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.notificationTimeout = 100
            serviceInfo = info
        }

        // Cache the profile list so blocking decisions are always up to date.
        serviceScope.launch {
            profileRepository.getAllProfiles().collect { profiles ->
                cachedProfiles = profiles
            }
        }

        // Keep independent strict mode state updated
        serviceScope.launch {
            settingsDataStore.strictModeState.collect { state ->
                cachedStrictState = state
                StrictModeRules.independentState = state
            }
        }

        // Keep strict level (Targeted vs Aggressive) updated
        serviceScope.launch {
            settingsDataStore.flowString(SettingsKeys.strictLevel, StrictLevel.TARGETED.value).collect { levelStr ->
                cachedStrictLevel = runCatching { StrictLevel.valueOf(levelStr) }.getOrDefault(StrictLevel.TARGETED)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

        // Ignore FocusBlock itself and critical phone infrastructure (launcher, keyboard, dialer, systemui)
        if (isCriticalSystemApp(pkg)) return

        val now = Instant.now()
        val activeProfiles = ScheduleEngine.activeProfiles(cachedProfiles, now)

        // 1. Strict Mode Tamper Evaluation (Targeted vs Aggressive Settings / Installer protection)
        val strictRemainingMs = getStrictRemainingMs(now, activeProfiles)
        val isStrictActive = strictRemainingMs != null && strictRemainingMs > 0

        if (isStrictActive && strictEvaluator.shouldBlockTamperAttempt(
                pkg = pkg,
                className = event.className?.toString(),
                strictLevel = cachedStrictLevel,
                isStrictActive = isStrictActive
            )
        ) {
            val appLabel = when {
                strictEvaluator.isInstallerApp(pkg) -> "Package Installer"
                strictEvaluator.isBrowserApp(pkg) -> "Web Browser"
                else -> "System Settings"
            }
            logBlockEvent(pkg = pkg, appName = appLabel, profileId = 0)
            triggerBlockScreen(
                appName = appLabel,
                profileName = "Strict Mode Protection",
                timeRemainingMs = strictRemainingMs,
                isStrict = true
            )
            return
        }

        // 2. Regular blocked app check
        val blockedProfile = activeProfiles.firstOrNull { it.profile.blockedApps.contains(pkg) }
        if (blockedProfile != null) {
            val timeRemainingMs = blockedProfile.endInstant.toEpochMilli() - System.currentTimeMillis()
            if (timeRemainingMs > 0 && serviceScope.isActive) {
                val appLabel = getAppLabel(pkg)
                logBlockEvent(pkg = pkg, appName = appLabel, profileId = blockedProfile.profile.id)
                triggerBlockScreen(
                    appName = appLabel,
                    profileName = blockedProfile.profile.name,
                    timeRemainingMs = timeRemainingMs,
                    isStrict = blockedProfile.profile.strict
                )
            }
        }
    }

    private fun getStrictRemainingMs(now: Instant, activeProfiles: List<ActiveProfileInfo>): Long? {
        val remainingTimes = mutableListOf<Long>()

        // Check independent strict mode
        if (cachedStrictState.isEffectivelyActive(now)) {
            val rem = cachedStrictState.remainingMs(now)
            // If ongoing indefinite term, use 24h placeholder for countdown display
            remainingTimes.add(rem ?: (24 * 3600 * 1000L))
        }

        // Check active strict profiles
        for (active in activeProfiles) {
            if (active.profile.strict) {
                val rem = active.endInstant.toEpochMilli() - now.toEpochMilli()
                if (rem > 0) remainingTimes.add(rem)
            }
        }

        return remainingTimes.maxOrNull()
    }

    @Volatile
    private var lastLogTime: Long = 0
    @Volatile
    private var lastLoggedPkg: String? = null

    private fun logBlockEvent(pkg: String, appName: String, profileId: Int) {
        val nowMs = System.currentTimeMillis()
        if (pkg == lastLoggedPkg && nowMs - lastLogTime < 2500L) {
            return
        }
        lastLogTime = nowMs
        lastLoggedPkg = pkg

        serviceScope.launch(Dispatchers.IO) {
            try {
                blockLogRepository.insert(
                    BlockLog(
                        profileId = profileId,
                        packageName = pkg,
                        appName = appName,
                        timestamp = nowMs,
                        blocked = true
                    )
                )
            } catch (e: Exception) {
                // Ignore DB insertion error in background service
            }
        }
    }

    private fun triggerBlockScreen(
        appName: String,
        profileName: String,
        timeRemainingMs: Long,
        isStrict: Boolean
    ) {
        if (!serviceScope.isActive) return

        val intent = Intent(this, BlockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockScreenActivity.EXTRA_APP_NAME, appName)
            putExtra(BlockScreenActivity.EXTRA_PROFILE_NAME, profileName)
            putExtra(BlockScreenActivity.EXTRA_TIME_REMAINING_MS, timeRemainingMs)
            putExtra(BlockScreenActivity.EXTRA_IS_STRICT, isStrict)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback: evict to home if block activity cannot be displayed directly
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun isCriticalSystemApp(pkg: String): Boolean {
        // Never block FocusBlock itself
        if (pkg == packageName) return true

        // Never block System UI or Google Play Services framework
        if (pkg == "com.android.systemui" || pkg == "com.google.android.gms") return true

        // Never block the user's home screen launcher (prevents infinite blocking loops)
        val defaultLauncher = getDefaultLauncherPackage()
        if (pkg == defaultLauncher) return true

        // Never block active input methods (keyboards)
        if (isDefaultInputMethod(pkg)) return true

        // Never block the emergency phone dialer
        val defaultDialer = getDefaultDialerPackage()
        if (pkg == defaultDialer) return true

        return false
    }

    private fun getDefaultLauncherPackage(): String? {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName
    }

    private fun getDefaultDialerPackage(): String? {
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        return telecomManager?.defaultDialerPackage ?: "com.android.phone"
    }

    private fun isDefaultInputMethod(pkg: String): Boolean {
        val defaultIme = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: return false
        return defaultIme.startsWith(pkg)
    }

    private fun getAppLabel(pkg: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pkg
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return super.onKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

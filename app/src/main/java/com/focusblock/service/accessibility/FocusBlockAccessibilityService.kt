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
import com.focusblock.domain.model.Profile
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.repository.ProfileRepository
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

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    @Volatile
    private var cachedProfiles: List<Profile> = emptyList()

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
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

        // Ignore FocusBlock itself and critical phone infrastructure (launcher, keyboard, dialer, systemui)
        if (isCriticalSystemApp(pkg)) return

        // Check if this package is currently blocked
        val now = Instant.now()
        val activeProfiles = ScheduleEngine.activeProfiles(cachedProfiles, now)
        val blockedProfile = activeProfiles.firstOrNull { it.profile.blockedApps.contains(pkg) }
        if (blockedProfile != null) {
            val timeRemainingMs = blockedProfile.endInstant.toEpochMilli() - System.currentTimeMillis()
            if (timeRemainingMs > 0 && serviceScope.isActive) {
                // Immediately kick back home to close/evict the blocked app
                performGlobalAction(GLOBAL_ACTION_HOME)

                val intent = Intent(this, BlockScreenActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(BlockScreenActivity.EXTRA_APP_NAME, getAppLabel(pkg))
                    putExtra(BlockScreenActivity.EXTRA_PROFILE_NAME, blockedProfile.profile.name)
                    putExtra(BlockScreenActivity.EXTRA_TIME_REMAINING_MS, timeRemainingMs)
                    putExtra(BlockScreenActivity.EXTRA_IS_STRICT, blockedProfile.profile.strict)
                }
                try {
                    startActivity(intent)
                } catch (e: android.content.ActivityNotFoundException) {
                    // Gracefully ignore; no activity to handle the intent
                }
            }
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
        // Consume key events so that, while a block screen may be active,
        // the user cannot navigate back to the blocked app.
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

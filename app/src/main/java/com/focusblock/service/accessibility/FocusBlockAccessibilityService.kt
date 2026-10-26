package com.focusblock.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
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

        // Ignore FocusBlock's own package (prevents re-triggering from BlockScreenActivity)
        if (pkg == packageName) return

        // Ignore system apps (launchers, keyboards, settings, etc.)
        if (isSystemApp(pkg)) return

        // Check if this package is currently blocked
        val now = Instant.now()
        val activeProfiles = ScheduleEngine.activeProfiles(cachedProfiles, now)
        val blockedProfile = activeProfiles.firstOrNull { it.profile.blockedApps.contains(pkg) }
        if (blockedProfile != null) {
            val timeRemainingMs = blockedProfile.endInstant.toEpochMilli() - System.currentTimeMillis()
            if (timeRemainingMs > 0 && serviceScope.isActive) {
                val intent = Intent(this, BlockScreenActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
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

    private fun isSystemApp(pkg: String): Boolean {
        return try {
            val info = packageManager.getApplicationInfo(pkg, 0)
            (info.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (info.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            // Unknown package — treat as non-system (don't block unknown packages)
            false
        }
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

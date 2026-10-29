package com.focusblock.domain.usecase

import android.app.Application
import android.content.Intent
import com.focusblock.domain.model.InstalledApp
import javax.inject.Inject

/**
 * Returns launchable (launcher-category) apps installed on the device,
 * excluding FocusBlock's own package.
 */
class GetInstalledAppsUseCase @Inject constructor(
    private val application: Application
) {
    fun getInstalledApps(): List<InstalledApp> {
        val pm = application.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(intent, 0).mapNotNull { res ->
            val pkg = res.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == application.packageName) return@mapNotNull null
            val label = res.loadLabel(pm).toString()
            InstalledApp(label = label, packageName = pkg)
        }.sortedBy { it.label }
    }
}

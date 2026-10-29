package com.focusblock.domain.model

/**
 * A launchable application installed on the device.
 */
data class InstalledApp(
    val label: String,
    val packageName: String
)

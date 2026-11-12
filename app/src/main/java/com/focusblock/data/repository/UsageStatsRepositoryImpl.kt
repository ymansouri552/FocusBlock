package com.focusblock.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import com.focusblock.domain.model.AppSortOrder
import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.DailyUsageBucket
import com.focusblock.domain.model.StatsPeriod
import com.focusblock.domain.model.UsageStatsOverview
import com.focusblock.domain.model.WeeklyDayBucket
import com.focusblock.domain.repository.ProfileRepository
import com.focusblock.domain.repository.UsageStatsRepository
import com.focusblock.domain.usecase.GetInstalledAppsUseCase
import com.focusblock.domain.usecase.SystemPermissionChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemPermissionChecker: SystemPermissionChecker,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val profileRepository: ProfileRepository
) : UsageStatsRepository {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    override fun hasUsagePermission(): Boolean {
        return systemPermissionChecker.isUsageStatsGranted()
    }

    override suspend fun getDailyStats(date: LocalDate): UsageStatsOverview = withContext(Dispatchers.IO) {
        val hasPermission = hasUsagePermission()
        if (!hasPermission || usageStatsManager == null) {
            return@withContext UsageStatsOverview(
                period = StatsPeriod.DAILY,
                selectedDate = date,
                startDate = date,
                endDate = date,
                hasPermission = false
            )
        }

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfDayMs = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDayMs = if (date == today) {
            System.currentTimeMillis()
        } else {
            date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        }

        if (endOfDayMs <= startOfDayMs) {
            return@withContext UsageStatsOverview(
                period = StatsPeriod.DAILY,
                selectedDate = date,
                startDate = date,
                endDate = date,
                hasPermission = true
            )
        }

        val launchableApps = getLaunchableAppMap()
        val excludedPackages = getExcludedPackages()
        val profileMap = getProfileBlockMap()

        val parsed = extractUsageFromEvents(
            startMs = startOfDayMs,
            endMs = endOfDayMs,
            excludedPackages = excludedPackages,
            launchableApps = launchableApps
        )

        val appItems = mutableListOf<AppUsageItem>()
        for ((pkg, durationMs) in parsed.appDurations) {
            if (durationMs <= 0) continue
            val appLabel = launchableApps[pkg] ?: resolveAppLabel(pkg, excludedPackages) ?: continue
            val launches = parsed.launchCounts[pkg] ?: 0
            val lastUsed = parsed.lastUsedTimestamps[pkg] ?: 0L
            val blockedProfiles = profileMap[pkg] ?: emptyList()

            appItems.add(
                AppUsageItem(
                    packageName = pkg,
                    appName = appLabel,
                    totalForegroundMs = durationMs,
                    launchCount = launches,
                    lastTimeUsedMs = lastUsed,
                    isBlocked = blockedProfiles.isNotEmpty(),
                    profileNames = blockedProfiles
                )
            )
        }

        val totalScreenTimeMs = parsed.totalScreenTimeMs
        val enrichedApps = appItems.map { app ->
            val pct = if (totalScreenTimeMs > 0) {
                app.totalForegroundMs.toFloat() / totalScreenTimeMs.toFloat()
            } else 0f
            app.copy(usagePercentage = pct)
        }.sortedByDescending { it.totalForegroundMs }

        // 8 hourly buckets (3 hours each)
        val slotLabels = listOf("00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00")
        val currentSlotIndex = if (date == today) {
            val nowMs = System.currentTimeMillis()
            (((nowMs - startOfDayMs) / (3 * 3600 * 1000L))).toInt().coerceIn(0, 7)
        } else -1

        val hourlyBuckets = slotLabels.mapIndexed { index, label ->
            DailyUsageBucket(
                slotIndex = index,
                hourLabel = label,
                durationMs = parsed.hourlySlotDurations[index],
                isCurrent = index == currentSlotIndex
            )
        }

        val peakSlot = hourlyBuckets.maxByOrNull { it.durationMs }
        val peakLabel = if (peakSlot != null && peakSlot.durationMs > 0) {
            "${peakSlot.hourLabel} to ${slotLabels.getOrNull(peakSlot.slotIndex + 1) ?: "24:00"}"
        } else {
            "No peak activity"
        }

        UsageStatsOverview(
            period = StatsPeriod.DAILY,
            selectedDate = date,
            startDate = date,
            endDate = date,
            totalScreenTimeMs = totalScreenTimeMs,
            dailyAverageMs = totalScreenTimeMs,
            mostUsedApp = enrichedApps.firstOrNull(),
            peakTimeLabel = peakLabel,
            totalLaunches = parsed.launchCounts.values.sum(),
            appsUsedCount = enrichedApps.size,
            hourlyBuckets = hourlyBuckets,
            weeklyBuckets = emptyList(),
            appUsageList = enrichedApps,
            hasPermission = true
        )
    }

    override suspend fun getWeeklyStats(dateInWeek: LocalDate): UsageStatsOverview = withContext(Dispatchers.IO) {
        val hasPermission = hasUsagePermission()
        if (!hasPermission || usageStatsManager == null) {
            return@withContext UsageStatsOverview(
                period = StatsPeriod.WEEKLY,
                selectedDate = dateInWeek,
                startDate = dateInWeek.with(DayOfWeek.MONDAY),
                endDate = dateInWeek.with(DayOfWeek.SUNDAY),
                hasPermission = false
            )
        }

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfWeek = dateInWeek.with(DayOfWeek.MONDAY)
        val endOfWeek = startOfWeek.plusDays(6)

        val weekStartMs = startOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()
        val weekEndMs = minOf(
            System.currentTimeMillis(),
            endOfWeek.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        )

        val launchableApps = getLaunchableAppMap()
        val excludedPackages = getExcludedPackages()
        val profileMap = getProfileBlockMap()

        // 1. Calculate each day's bucket for the 7-day chart
        val weeklyBuckets = mutableListOf<WeeklyDayBucket>()
        var daysWithData = 0

        for (dayOffset in 0..6) {
            val dayDate = startOfWeek.plusDays(dayOffset.toLong())
            val dayStartMs = dayDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEndMs = minOf(
                System.currentTimeMillis(),
                dayDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            )

            var dayTotalMs = 0L
            if (dayStartMs < System.currentTimeMillis()) {
                daysWithData++
                val dayParsed = extractUsageFromEvents(
                    startMs = dayStartMs,
                    endMs = dayEndMs,
                    excludedPackages = excludedPackages,
                    launchableApps = launchableApps
                )
                dayTotalMs = dayParsed.totalScreenTimeMs
            }

            val shortDayLabel = dayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            weeklyBuckets.add(
                WeeklyDayBucket(
                    date = dayDate,
                    dayLabel = shortDayLabel,
                    durationMs = dayTotalMs,
                    isToday = dayDate == today,
                    isSelected = dayDate == dateInWeek
                )
            )
        }

        // 2. Query full week events
        val weekParsed = extractUsageFromEvents(
            startMs = weekStartMs,
            endMs = weekEndMs,
            excludedPackages = excludedPackages,
            launchableApps = launchableApps
        )

        val appItems = mutableListOf<AppUsageItem>()
        for ((pkg, durationMs) in weekParsed.appDurations) {
            if (durationMs <= 0) continue
            val appLabel = launchableApps[pkg] ?: resolveAppLabel(pkg, excludedPackages) ?: continue
            val launches = weekParsed.launchCounts[pkg] ?: 0
            val lastUsed = weekParsed.lastUsedTimestamps[pkg] ?: 0L
            val blockedProfiles = profileMap[pkg] ?: emptyList()

            appItems.add(
                AppUsageItem(
                    packageName = pkg,
                    appName = appLabel,
                    totalForegroundMs = durationMs,
                    launchCount = launches,
                    lastTimeUsedMs = lastUsed,
                    isBlocked = blockedProfiles.isNotEmpty(),
                    profileNames = blockedProfiles
                )
            )
        }

        val totalScreenTimeMs = weekParsed.totalScreenTimeMs
        val enrichedApps = appItems.map { app ->
            val pct = if (totalScreenTimeMs > 0) {
                app.totalForegroundMs.toFloat() / totalScreenTimeMs.toFloat()
            } else 0f
            app.copy(usagePercentage = pct)
        }.sortedByDescending { it.totalForegroundMs }

        val activeDays = daysWithData.coerceAtLeast(1)
        val dailyAverageMs = totalScreenTimeMs / activeDays

        val peakDay = weeklyBuckets.maxByOrNull { it.durationMs }
        val peakDayLabel = if (peakDay != null && peakDay.durationMs > 0) {
            "${peakDay.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())} (${peakDay.formattedDuration})"
        } else {
            "No peak day"
        }

        UsageStatsOverview(
            period = StatsPeriod.WEEKLY,
            selectedDate = dateInWeek,
            startDate = startOfWeek,
            endDate = endOfWeek,
            totalScreenTimeMs = totalScreenTimeMs,
            dailyAverageMs = dailyAverageMs,
            mostUsedApp = enrichedApps.firstOrNull(),
            peakTimeLabel = peakDayLabel,
            totalLaunches = weekParsed.launchCounts.values.sum(),
            appsUsedCount = enrichedApps.size,
            hourlyBuckets = emptyList(),
            weeklyBuckets = weeklyBuckets,
            appUsageList = enrichedApps,
            hasPermission = true
        )
    }

    override suspend fun getAppUsageForPeriod(
        packageName: String,
        startMs: Long,
        endMs: Long
    ): AppUsageItem? = withContext(Dispatchers.IO) {
        if (!hasUsagePermission() || usageStatsManager == null) return@withContext null

        val excluded = getExcludedPackages()
        val launchable = getLaunchableAppMap()
        val parsed = extractUsageFromEvents(
            startMs = startMs,
            endMs = endMs,
            excludedPackages = excluded,
            launchableApps = launchable
        )

        val duration = parsed.appDurations[packageName] ?: return@withContext null
        val appLabel = launchable[packageName] ?: resolveAppLabel(packageName, excluded) ?: packageName
        val profileMap = getProfileBlockMap()
        val blockedProfiles = profileMap[packageName] ?: emptyList()

        AppUsageItem(
            packageName = packageName,
            appName = appLabel,
            totalForegroundMs = duration,
            launchCount = parsed.launchCounts[packageName] ?: 0,
            lastTimeUsedMs = parsed.lastUsedTimestamps[packageName] ?: 0L,
            isBlocked = blockedProfiles.isNotEmpty(),
            profileNames = blockedProfiles
        )
    }

    override suspend fun getUsageDuration(
        packageName: String,
        startMs: Long,
        endMs: Long
    ): Long = withContext(Dispatchers.IO) {
        if (!hasUsagePermission() || usageStatsManager == null) return@withContext 0L
        val parsed = extractUsageFromEvents(
            startMs = startMs,
            endMs = endMs,
            excludedPackages = emptySet(),
            launchableApps = emptyMap(),
            targetPackages = setOf(packageName)
        )
        parsed.appDurations[packageName] ?: 0L
    }

    override suspend fun getCombinedUsageDuration(
        packages: Set<String>,
        startMs: Long,
        endMs: Long
    ): Long = withContext(Dispatchers.IO) {
        if (!hasUsagePermission() || usageStatsManager == null || packages.isEmpty()) return@withContext 0L
        val parsed = extractUsageFromEvents(
            startMs = startMs,
            endMs = endMs,
            excludedPackages = emptySet(),
            launchableApps = emptyMap(),
            targetPackages = packages
        )
        packages.sumOf { parsed.appDurations[it] ?: 0L }
    }

    private data class ParsedUsage(
        val appDurations: Map<String, Long>,
        val launchCounts: Map<String, Int>,
        val lastUsedTimestamps: Map<String, Long>,
        val hourlySlotDurations: LongArray,
        val totalScreenTimeMs: Long
    )

    private fun extractUsageFromEvents(
        startMs: Long,
        endMs: Long,
        excludedPackages: Set<String>,
        launchableApps: Map<String, String>,
        targetPackages: Set<String>? = null
    ): ParsedUsage {
        val manager = usageStatsManager ?: return ParsedUsage(emptyMap(), emptyMap(), emptyMap(), LongArray(8), 0L)

        // Usage events can't exist in the future. Cap the window at "now" so an
        // in-flight foreground session (no MOVE_TO_BACKGROUND event yet) isn't
        // flushed all the way to the window end; otherwise a quota evaluated over
        // a window [startMs, endMs) would count the currently open app as used
        // until endMs and instantly exhaust the budget at the start of the window.
        var endMs = endMs.coerceAtMost(System.currentTimeMillis())

        // Query events from up to 15 mins prior to catch app sessions starting across boundary
        val queryStart = (startMs - 15 * 60 * 1000L).coerceAtLeast(0L)
        val events = runCatching { manager.queryEvents(queryStart, endMs) }.getOrNull()

        val appDurations = mutableMapOf<String, Long>()
        val launchCounts = mutableMapOf<String, Int>()
        val lastUsed = mutableMapOf<String, Long>()
        val hourlySlots = LongArray(8)
        val slotDurationMs = 3 * 3600 * 1000L

        var hasEvents = false

        if (events != null) {
            var activePkg: String? = null
            var activeStartMs: Long = 0L
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                val time = event.timeStamp
                val eventType = event.eventType
                hasEvents = true

                val shouldTrack = if (targetPackages != null) {
                    targetPackages.contains(pkg)
                } else {
                    isUserApp(pkg, excludedPackages, launchableApps)
                }

                when (eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        if (activePkg != null && activeStartMs > 0 && time > activeStartMs) {
                            commitInterval(
                                pkg = activePkg!!,
                                intervalStart = activeStartMs,
                                intervalEnd = time,
                                windowStart = startMs,
                                windowEnd = endMs,
                                excludedPackages = excludedPackages,
                                launchableApps = launchableApps,
                                targetPackages = targetPackages,
                                appDurations = appDurations,
                                hourlySlots = hourlySlots,
                                slotDurationMs = slotDurationMs
                            )
                        }

                        activePkg = pkg
                        activeStartMs = time

                        if (time in startMs..endMs && shouldTrack) {
                            launchCounts[pkg] = (launchCounts[pkg] ?: 0) + 1
                            lastUsed[pkg] = time
                        }
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.MOVE_TO_BACKGROUND,
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                    UsageEvents.Event.KEYGUARD_SHOWN -> {
                        if (activePkg != null && activeStartMs > 0 && time > activeStartMs) {
                            commitInterval(
                                pkg = activePkg!!,
                                intervalStart = activeStartMs,
                                intervalEnd = time,
                                windowStart = startMs,
                                windowEnd = endMs,
                                excludedPackages = excludedPackages,
                                launchableApps = launchableApps,
                                targetPackages = targetPackages,
                                appDurations = appDurations,
                                hourlySlots = hourlySlots,
                                slotDurationMs = slotDurationMs
                            )
                            if (time in startMs..endMs && shouldTrack) {
                                lastUsed[activePkg!!] = time
                            }
                        }
                        activePkg = null
                        activeStartMs = 0L
                    }
                }
            }

            // Flush in-flight active session at end of window
            if (activePkg != null && activeStartMs > 0 && endMs > activeStartMs) {
                commitInterval(
                    pkg = activePkg!!,
                    intervalStart = activeStartMs,
                    intervalEnd = endMs,
                    windowStart = startMs,
                    windowEnd = endMs,
                    excludedPackages = excludedPackages,
                    launchableApps = launchableApps,
                    targetPackages = targetPackages,
                    appDurations = appDurations,
                    hourlySlots = hourlySlots,
                    slotDurationMs = slotDurationMs
                )
            }
        }

        // Fallback for tests/environments where queryEvents has zero records
        if (!hasEvents || appDurations.isEmpty()) {
            runCatching {
                val statsList = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMs, endMs)
                if (statsList != null) {
                    for (stat in statsList) {
                        val pkg = stat.packageName ?: continue
                        val shouldTrack = if (targetPackages != null) {
                            targetPackages.contains(pkg)
                        } else {
                            isUserApp(pkg, excludedPackages, launchableApps)
                        }
                        if (!shouldTrack) continue
                        if (stat.totalTimeInForeground <= 0) continue
                        // Only include if lastTimeUsed is within the window
                        if (stat.lastTimeUsed in startMs..endMs) {
                            appDurations[pkg] = (appDurations[pkg] ?: 0L) + stat.totalTimeInForeground
                            lastUsed[pkg] = stat.lastTimeUsed
                        }
                    }
                }
            }
        }

        val totalScreenTimeMs = appDurations.values.sum()

        return ParsedUsage(
            appDurations = appDurations,
            launchCounts = launchCounts,
            lastUsedTimestamps = lastUsed,
            hourlySlotDurations = hourlySlots,
            totalScreenTimeMs = totalScreenTimeMs
        )
    }

    private fun commitInterval(
        pkg: String,
        intervalStart: Long,
        intervalEnd: Long,
        windowStart: Long,
        windowEnd: Long,
        excludedPackages: Set<String>,
        launchableApps: Map<String, String>,
        targetPackages: Set<String>?,
        appDurations: MutableMap<String, Long>,
        hourlySlots: LongArray,
        slotDurationMs: Long
    ) {
        if (targetPackages != null) {
            if (!targetPackages.contains(pkg)) return
        } else {
            if (!isUserApp(pkg, excludedPackages, launchableApps)) return
        }

        val segStart = maxOf(intervalStart, windowStart)
        val segEnd = minOf(intervalEnd, windowEnd)
        if (segEnd <= segStart) return

        val duration = segEnd - segStart
        appDurations[pkg] = (appDurations[pkg] ?: 0L) + duration

        val slotIndex = (((segStart - windowStart) / slotDurationMs)).toInt().coerceIn(0, 7)
        hourlySlots[slotIndex] += duration
    }

    private fun isUserApp(
        pkg: String,
        excludedPackages: Set<String>,
        launchableApps: Map<String, String>
    ): Boolean {
        if (excludedPackages.contains(pkg)) return false
        if (launchableApps.containsKey(pkg)) return true
        if (pkg == "com.android.settings") return true
        return resolveAppLabel(pkg, excludedPackages) != null
    }

    private fun getExcludedPackages(): Set<String> {
        val set = mutableSetOf(
            "com.android.systemui",
            "android",
            context.packageName
        )

        // Exclude current default launcher / home app
        runCatching {
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName?.let { set.add(it) }
        }

        // Exclude system keyboards / IMEs
        runCatching {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.inputMethodList?.forEach { set.add(it.packageName) }
        }

        return set
    }

    private fun getLaunchableAppMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val installed = getInstalledAppsUseCase.getInstalledApps()
        for (app in installed) {
            map[app.packageName] = app.label
        }
        map["com.android.settings"] = "Settings"
        return map
    }

    private fun resolveAppLabel(packageName: String, excludedPackages: Set<String>): String? {
        if (excludedPackages.contains(packageName)) return null
        if (packageName == "com.android.settings") return "Settings"
        return runCatching {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            val isUserFacing = (pm.getLaunchIntentForPackage(packageName) != null) ||
                    ((info.flags and ApplicationInfo.FLAG_SYSTEM) == 0)
            if (isUserFacing) {
                pm.getApplicationLabel(info).toString()
            } else null
        }.getOrNull()
    }

    private suspend fun getProfileBlockMap(): Map<String, List<String>> {
        val profiles = profileRepository.getAllProfiles().firstOrNull() ?: emptyList()
        val map = mutableMapOf<String, MutableList<String>>()
        for (profile in profiles) {
            for (pkg in profile.blockedApps) {
                map.getOrPut(pkg) { mutableListOf() }.add(profile.name)
            }
        }
        return map
    }
}

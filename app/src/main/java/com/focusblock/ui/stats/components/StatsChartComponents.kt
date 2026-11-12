package com.focusblock.ui.stats.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.R
import com.focusblock.domain.model.AppUsageItem
import com.focusblock.domain.model.DailyUsageBucket
import com.focusblock.domain.model.WeeklyDayBucket
import com.focusblock.ui.profile.AppIconCache
import com.focusblock.ui.theme.CountdownNumericStyle
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.PledgePalette
import com.focusblock.ui.theme.tabular
import java.time.LocalDate

/**
 * 7-Day Interactive Bar Chart for Weekly Screen Time.
 * Clicking a day bar selects that day.
 */
@Composable
fun WeeklyInteractiveChart(
    buckets: List<WeeklyDayBucket>,
    onSelectDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current
    val maxDuration = buckets.maxOfOrNull { it.durationMs }?.coerceAtLeast(1800_000L) ?: 3600_000L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface)
            .border(BorderStroke(1.dp, palette.hairline), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DAILY BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = palette.inkSecondary,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Tap a bar to inspect day",
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            buckets.forEach { bucket ->
                val fraction = (bucket.durationMs.toFloat() / maxDuration.toFloat()).coerceIn(0.04f, 1f)
                val animatedHeight by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(durationMillis = 400),
                    label = "BarHeight_${bucket.dayLabel}"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectDay(bucket.date) }
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Duration label
                    Text(
                        text = if (bucket.durationMs > 0) formatShortDuration(bucket.durationMs) else "0",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = if (bucket.isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (bucket.isSelected) palette.accent else palette.inkSecondary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(84.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(palette.hairline.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    when {
                                        bucket.isSelected -> palette.accent
                                        bucket.isToday -> palette.accent.copy(alpha = 0.75f)
                                        bucket.durationMs > 0 -> palette.inkSecondary.copy(alpha = 0.55f)
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Day label
                    Text(
                        text = bucket.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = if (bucket.isSelected || bucket.isToday) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            bucket.isSelected -> palette.accent
                            bucket.isToday -> palette.ink
                            else -> palette.inkSecondary
                        }
                    )
                }
            }
        }
    }
}

/**
 * Hourly Distribution Chart for Daily Screen Time.
 */
@Composable
fun DailyHourlyChart(
    buckets: List<DailyUsageBucket>,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current
    val maxDuration = buckets.maxOfOrNull { it.durationMs }?.coerceAtLeast(600_000L) ?: 1800_000L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface)
            .border(BorderStroke(1.dp, palette.hairline), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HOURLY DISTRIBUTION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = palette.inkSecondary,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "3-hour intervals",
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            buckets.forEach { bucket ->
                val fraction = (bucket.durationMs.toFloat() / maxDuration.toFloat()).coerceIn(0.04f, 1f)
                val animatedHeight by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(durationMillis = 400),
                    label = "HourlyHeight_${bucket.slotIndex}"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (bucket.durationMs > 0) formatShortDuration(bucket.durationMs) else "",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (bucket.isCurrent) palette.accent else palette.inkSecondary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(74.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(palette.hairline.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    if (bucket.isCurrent) palette.accent
                                    else if (bucket.durationMs > 0) palette.accent.copy(alpha = 0.6f)
                                    else Color.Transparent
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = bucket.hourLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = if (bucket.isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (bucket.isCurrent) palette.accent else palette.inkSecondary
                    )
                }
            }
        }
    }
}

/**
 * Hero Screen Time Overview Card.
 */
@Composable
fun ScreenTimeHeroCard(
    totalDurationFormatted: String,
    periodLabel: String,
    appsCount: Int,
    launchesCount: Int,
    secondaryMetricLabel: String,
    secondaryMetricValue: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface)
            .border(BorderStroke(1.dp, palette.hairline), RoundedCornerShape(14.dp))
            .padding(18.dp)
    ) {
        Text(
            text = periodLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = palette.inkSecondary,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = totalDurationFormatted,
            style = CountdownNumericStyle.copy(
                fontSize = 38.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Bold,
                color = palette.ink
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeroStatSubPill(
                title = "ACTIVE APPS",
                value = "$appsCount",
                modifier = Modifier.weight(1f)
            )
            HeroStatSubPill(
                title = "APP OPENS",
                value = "$launchesCount",
                modifier = Modifier.weight(1f)
            )
            HeroStatSubPill(
                title = secondaryMetricLabel,
                value = secondaryMetricValue,
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}

@Composable
private fun HeroStatSubPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.background)
            .border(BorderStroke(1.dp, palette.hairline.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = palette.inkSecondary,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = palette.ink
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * App Usage Row Item Card.
 */
@Composable
fun AppUsageRow(
    item: AppUsageItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current
    val context = LocalContext.current
    var iconBitmap by remember(item.packageName) {
        mutableStateOf(AppIconCache.get(item.packageName))
    }

    LaunchedEffect(item.packageName) {
        if (iconBitmap == null) {
            iconBitmap = AppIconCache.loadIcon(context, item.packageName)
        }
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.hairline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap!!,
                            contentDescription = item.appName,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = palette.inkSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // App Label and Package
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.isBlocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(palette.stampSurface)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = palette.stamp,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "BLOCKED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = palette.stamp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.stats_open_count,
                                item.launchCount,
                                item.launchCount
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.inkSecondary
                        )
                        if (item.usagePercentage > 0f) {
                            Text(
                                text = " · ${(item.usagePercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.inkSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Duration
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = item.formattedDuration,
                        style = MaterialTheme.typography.bodyLarge.tabular(),
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = palette.inkSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Visual Progress Bar
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.hairline.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.usagePercentage.coerceIn(0.01f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (item.isBlocked) palette.stamp else palette.accent)
                )
            }
        }
    }
}

private fun formatShortDuration(durationMs: Long): String {
    val minutes = (durationMs / 60_000L).coerceAtLeast(0L)
    val hours = minutes / 60L
    val remMin = minutes % 60L
    return when {
        hours > 0 && remMin > 0 -> "${hours}h ${remMin}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

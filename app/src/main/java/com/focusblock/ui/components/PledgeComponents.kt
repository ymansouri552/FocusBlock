package com.focusblock.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.domain.model.Schedule
import com.focusblock.domain.model.ScheduleMode
import com.focusblock.ui.theme.CountdownNumericStyle
import com.focusblock.ui.theme.HeroNumericStyle
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.PledgePalette
import com.focusblock.ui.theme.PledgeShapes
import com.focusblock.ui.theme.StampTextStyle
import com.focusblock.ui.theme.tabular
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// 1. STATUS RING: OPEN vs SEALED Vault Indicator
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatusRing(
    isSealed: Boolean,
    title: String,
    subtitle: String,
    countdownText: String? = null,
    isStrict: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp
) {
    val palette = LocalPledgePalette.current
    val sweepTarget = if (isSealed) 360f else 0f
    val animatedSweep by animateFloatAsState(
        targetValue = sweepTarget,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "RingSweep"
    )

    val ringColor = if (isStrict) palette.stamp else palette.accent
    val labelColor = if (isStrict) palette.stamp else if (isSealed) palette.accent else palette.inkSecondary

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.toPx() - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background hairline track
            drawArc(
                color = palette.hairline,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = 2.dp.toPx())
            )

            // Outer tick notches for mechanical vault aesthetic
            val radius = diameter / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val totalTicks = 24
            for (i in 0 until totalTicks) {
                val angleRad = Math.toRadians((i * (360f / totalTicks)).toDouble())
                val innerR = radius - (if (i % 6 == 0) 7.dp.toPx() else 4.dp.toPx())
                val outerR = radius - 1.dp.toPx()
                val start = Offset(
                    (center.x + innerR * Math.cos(angleRad)).toFloat(),
                    (center.y + innerR * Math.sin(angleRad)).toFloat()
                )
                val end = Offset(
                    (center.x + outerR * Math.cos(angleRad)).toFloat(),
                    (center.y + outerR * Math.sin(angleRad)).toFloat()
                )
                drawLine(
                    color = palette.hairline,
                    start = start,
                    end = end,
                    strokeWidth = if (i % 6 == 0) 1.5.dp.toPx() else 1.dp.toPx()
                )
            }

            // Foreground animated sealed sweep
            if (animatedSweep > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(20.dp)
        ) {
            // Status Stamp Tag
            Text(
                text = if (isSealed) "SEALED" else "OPEN",
                style = StampTextStyle,
                color = labelColor
            )

            if (countdownText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = countdownText,
                    style = CountdownNumericStyle,
                    color = if (isStrict) palette.stamp else palette.ink,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. HOLD BUTTON: Press & Hold (~800ms) with visible fill & haptic
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HoldButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    holdDurationMs: Long = 800L,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
    progressColor: Color? = null,
    borderStroke: BorderStroke? = null
) {
    val palette = LocalPledgePalette.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var holdJob by remember { mutableStateOf<Job?>(null) }
    val progressAnim = remember { Animatable(0f) }

    val activeFillColor = progressColor ?: if (isDestructive) palette.stamp else palette.accent
    val effectiveBorder = borderStroke ?: BorderStroke(2.dp, if (enabled) palette.border else palette.hairline)

    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(3.dp))
            .border(effectiveBorder, RoundedCornerShape(3.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                        holdJob = scope.launch {
                            progressAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = holdDurationMs.toInt(),
                                    easing = FastOutSlowInEasing
                                )
                            )
                            if (progressAnim.value >= 0.99f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConfirm()
                                delay(150)
                                progressAnim.snapTo(0f)
                            }
                        }

                        val released = tryAwaitRelease()
                        isPressed = false
                        holdJob?.cancel()
                        scope.launch {
                            progressAnim.animateTo(0f, tween(150))
                        }
                    }
                )
            },
        color = palette.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fill wipe from left to right
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressAnim.value)
                    .background(activeFillColor)
            )

            // Text Label
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val textColor = if (progressAnim.value > 0.5f) {
                    if (isDestructive) Color.White else palette.onAccent
                } else {
                    if (enabled) palette.ink else palette.inkSecondary
                }
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                if (isPressed && progressAnim.value < 1f) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOLD",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. DAY BAR 24H: Mini 24-hour horizontal bar showing blocked intervals
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DayBar24h(
    startTime: LocalTime?,
    endTime: LocalTime?,
    isBlockMode: Boolean = true,
    isStrict: Boolean = false,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val palette = LocalPledgePalette.current
    val barColor = if (isStrict) palette.stamp else palette.accent

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(2.dp))
    ) {
        val totalMinutes = 24 * 60f
        // Hairline track
        drawRoundRect(
            color = palette.hairline,
            size = size,
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        if (startTime != null && endTime != null && isBlockMode) {
            val startMin = startTime.hour * 60f + startTime.minute
            val endMin = endTime.hour * 60f + endTime.minute

            if (startMin <= endMin) {
                // Single contiguous block during the day
                val left = (startMin / totalMinutes) * size.width
                val right = (endMin / totalMinutes) * size.width
                val blockWidth = (right - left).coerceAtLeast(2.dp.toPx())
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, 0f),
                    size = Size(blockWidth, size.height),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )
            } else {
                // Overnight block: startMin -> midnight, and midnight -> endMin
                val left1 = (startMin / totalMinutes) * size.width
                val width1 = size.width - left1
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left1, 0f),
                    size = Size(width1, size.height),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )

                val width2 = (endMin / totalMinutes) * size.width
                if (width2 > 0) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(width2, size.height),
                        cornerRadius = CornerRadius(1.dp.toPx())
                    )
                }
            }
        } else if (!isBlockMode) {
            // Allowance mode: render small hash bars
            val segmentCount = 6
            val segWidth = size.width / (segmentCount * 2)
            for (i in 0 until segmentCount) {
                drawRoundRect(
                    color = barColor.copy(alpha = 0.7f),
                    topLeft = Offset(i * segWidth * 2, 0f),
                    size = Size(segWidth, size.height),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. DAY TIMELINE 24H EDITOR: Primary interactive timeline for wizard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DayTimeline24hEditor(
    startTime: LocalTime,
    endTime: LocalTime,
    onRangeChanged: (LocalTime, LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current
    var draggingHandle by remember { mutableStateOf<String?>(null) } // "start", "end", or null

    val startFraction = (startTime.hour * 60 + startTime.minute) / (24 * 60f)
    val endFraction = (endTime.hour * 60 + endTime.minute) / (24 * 60f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Range label display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIMELINE (DRAG TO PAINT)",
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary
            )
            val durationHours = if (endTime.isAfter(startTime)) {
                java.time.Duration.between(startTime, endTime).toMinutes() / 60.0
            } else {
                (24 * 60 - java.time.Duration.between(endTime, startTime).toMinutes()) / 60.0
            }
            Text(
                text = "${String.format("%.1f", durationHours)}h blocked",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = palette.accent
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large 24h interactive slider track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(palette.surface)
                .border(1.5.dp, palette.border, RoundedCornerShape(3.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val touchFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            val distStart = kotlin.math.abs(touchFraction - startFraction)
                            val distEnd = kotlin.math.abs(touchFraction - endFraction)
                            draggingHandle = if (distStart <= distEnd) "start" else "end"
                        },
                        onDragEnd = { draggingHandle = null },
                        onDragCancel = { draggingHandle = null },
                        onDrag = { change, _ ->
                            val touchFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            val totalMinutes = (touchFraction * 24 * 60).roundToInt()
                            // Snap to 15-minute increments
                            val snappedMin = ((totalMinutes + 7) / 15) * 15
                            val clampedMin = snappedMin.coerceIn(0, 24 * 60 - 1)
                            val newTime = LocalTime.of(clampedMin / 60, clampedMin % 60)

                            if (draggingHandle == "start") {
                                onRangeChanged(newTime, endTime)
                            } else if (draggingHandle == "end") {
                                onRangeChanged(startTime, newTime)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalMinutes = 24 * 60f
                val startMin = startTime.hour * 60f + startTime.minute
                val endMin = endTime.hour * 60f + endTime.minute

                // Draw hour division tick marks (every 2 hours)
                for (h in 0..24) {
                    val x = (h / 24f) * size.width
                    val tickHeight = if (h % 6 == 0) size.height else if (h % 2 == 0) 14.dp.toPx() else 8.dp.toPx()
                    drawLine(
                        color = if (h % 6 == 0) palette.hairline.copy(alpha = 0.5f) else palette.hairline,
                        start = Offset(x, size.height - tickHeight),
                        end = Offset(x, size.height),
                        strokeWidth = if (h % 6 == 0) 1.5.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Draw blocked active window fill
                val barTop = 6.dp.toPx()
                val barHeight = size.height - 12.dp.toPx()

                if (startMin <= endMin) {
                    val left = (startMin / totalMinutes) * size.width
                    val width = ((endMin - startMin) / totalMinutes) * size.width
                    drawRect(
                        color = palette.accent,
                        topLeft = Offset(left, barTop),
                        size = Size(width, barHeight)
                    )
                } else {
                    // Overnight window
                    val left1 = (startMin / totalMinutes) * size.width
                    drawRect(
                        color = palette.accent,
                        topLeft = Offset(left1, barTop),
                        size = Size(size.width - left1, barHeight)
                    )
                    val width2 = (endMin / totalMinutes) * size.width
                    if (width2 > 0) {
                        drawRect(
                            color = palette.accent,
                            topLeft = Offset(0f, barTop),
                            size = Size(width2, barHeight)
                        )
                    }
                }

                // Start and End boundary needles
                val startX = (startMin / totalMinutes) * size.width
                val endX = (endMin / totalMinutes) * size.width

                drawLine(
                    color = palette.ink,
                    start = Offset(startX, 0f),
                    end = Offset(startX, size.height),
                    strokeWidth = 3.dp.toPx()
                )
                drawLine(
                    color = palette.ink,
                    start = Offset(endX, 0f),
                    end = Offset(endX, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Time axis markers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.tabular(),
                    color = palette.inkSecondary,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. STAMP LABEL: Tactile vault stamp for LOCKED / STRICT / SEALED states
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StampLabel(
    text: String,
    modifier: Modifier = Modifier,
    rotation: Float = -5f,
    isSealed: Boolean = false
) {
    val palette = LocalPledgePalette.current
    val stampColor = palette.stamp

    Box(
        modifier = modifier
            .rotate(rotation)
            .border(
                width = 1.5.dp,
                color = stampColor,
                shape = RoundedCornerShape(2.dp)
            )
            .background(palette.stampSurface, RoundedCornerShape(2.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = StampTextStyle,
            color = stampColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. LEDGER ROW: Contract / Ledger item in Profiles list
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LedgerRow(
    title: String,
    schedule: Schedule?,
    appCount: Int,
    isStrictLocked: Boolean,
    isActiveNow: Boolean,
    enabled: Boolean,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    val palette = LocalPledgePalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .background(palette.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title.ifBlank { "UNTITLED PROFILE" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isStrictLocked) {
                        StampLabel(text = "LOCKED")
                    } else if (isActiveNow) {
                        Surface(
                            color = palette.accent,
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = StampTextStyle,
                                color = palette.onAccent,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mini 24h schedule bar
                if (schedule != null) {
                    DayBar24h(
                        startTime = schedule.startTime,
                        endTime = schedule.endTime,
                        isBlockMode = schedule.mode == ScheduleMode.BLOCK,
                        isStrict = isStrictLocked,
                        height = 6.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Subtitle metadata in small caps
                val appLabel = if (appCount == 1) "1 APP BLOCKED" else "$appCount APPS BLOCKED"
                val daysSummary = schedule?.daysOfWeek?.size?.let { "$it DAYS/WK" } ?: "ON DEMAND"
                Text(
                    text = "$appLabel · $daysSummary",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary,
                    letterSpacing = 1.0.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (!isStrictLocked) {
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = palette.onAccent,
                        checkedTrackColor = palette.accent,
                        uncheckedThumbColor = palette.inkSecondary,
                        uncheckedTrackColor = palette.surfaceRaised,
                        uncheckedBorderColor = palette.hairline
                    )
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = palette.hairline,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. SECTION HEADER: Ledger uppercase header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val palette = LocalPledgePalette.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = palette.inkSecondary,
                letterSpacing = 1.4.sp
            )
            if (badgeText != null) {
                Surface(
                    color = palette.hairline,
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.ink,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }

        if (actionText != null && onAction != null) {
            Text(
                text = actionText.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = palette.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 8. STAT TILE & STATS STRIP: Focused hours bar chart, streak grid, block count
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    val palette = LocalPledgePalette.current

    Surface(
        modifier = modifier
            .border(BorderStroke(1.dp, palette.hairline), RoundedCornerShape(3.dp)),
        color = palette.surface,
        shape = RoundedCornerShape(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary,
                letterSpacing = 1.0.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.tabular(),
                fontWeight = FontWeight.Bold,
                color = palette.ink
            )
            if (content != null) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}

/**
 * 7-Day Bar Chart: displays Monday-Sunday focus hours.
 */
@Composable
fun SevenDayBarChart(
    dailyHours: List<Float>, // 7 values (0f..24f)
    modifier: Modifier = Modifier,
    height: Dp = 32.dp
) {
    val palette = LocalPledgePalette.current
    val accent = palette.accent

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val count = 7
        val slotWidth = size.width / count
        val barWidth = (slotWidth * 0.55f).coerceAtLeast(3.dp.toPx())
        val maxHour = (dailyHours.maxOrNull() ?: 1f).coerceAtLeast(4f)

        for (i in 0 until count) {
            val h = dailyHours.getOrElse(i) { 0f }
            val barHeight = ((h / maxHour) * size.height).coerceIn(2.dp.toPx(), size.height)
            val x = i * slotWidth + (slotWidth - barWidth) / 2
            val y = size.height - barHeight

            // Base slot hairline
            drawRoundRect(
                color = palette.hairline,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(1.dp.toPx())
            )

            if (h > 0f) {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )
            }
        }
    }
}

/**
 * Streak Calendar Grid: small 7x3 or 7x4 matrix of filled/unfilled squares.
 */
@Composable
fun StreakGrid(
    streakDays: Int,
    totalSlots: Int = 14,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current
    val accent = palette.accent

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val cols = 7
        val rows = (totalSlots + cols - 1) / cols
        val squareSize = 8.dp.toPx()
        val spacing = 3.dp.toPx()

        for (i in 0 until totalSlots) {
            val col = i % cols
            val row = i / cols
            val x = col * (squareSize + spacing)
            val y = row * (squareSize + spacing)

            val isFilled = i < streakDays
            drawRoundRect(
                color = if (isFilled) accent else palette.hairline,
                topLeft = Offset(x, y),
                size = Size(squareSize, squareSize),
                cornerRadius = CornerRadius(1.dp.toPx())
            )
        }
    }
}

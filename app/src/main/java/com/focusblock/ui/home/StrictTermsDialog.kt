package com.focusblock.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focusblock.domain.model.StrictTermType
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.StampTextStyle
import com.focusblock.ui.theme.tabular
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Strict Mode Pledge Ceremony: A dedicated contract / pledge confirmation screen.
 * Reads like a legal/moral commitment covenant with press-and-hold signing,
 * mechanical lock-click feedback, and a physical stamp impact animation.
 */
@Composable
fun StrictTermsDialog(
    onDismiss: () -> Unit,
    onArm: (termType: StrictTermType, customEndInstant: Instant?) -> Unit
) {
    val palette = LocalPledgePalette.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var selectedTerm by remember { mutableStateOf(StrictTermType.DAYS_3) }
    val tomorrow = remember { LocalDate.now().plusDays(1) }
    var customDate by remember { mutableStateOf(tomorrow) }
    var customTime by remember { mutableStateOf(LocalTime.of(8, 0)) }

    var isSealedAnimActive by remember { mutableStateOf(false) }
    var isSigning by remember { mutableStateOf(false) }
    val signProgress = remember { Animatable(0f) }
    var holdJob by remember { mutableStateOf<Job?>(null) }
    val stampScale = remember { Animatable(1.6f) }

    val zone = remember { ZoneId.systemDefault() }
    val now = remember { Instant.now() }

    val commitmentExpiryString = remember(selectedTerm, customDate, customTime) {
        val targetInstant = when (selectedTerm) {
            StrictTermType.HOURS_4 -> now.plus(4, ChronoUnit.HOURS)
            StrictTermType.HOURS_8 -> now.plus(8, ChronoUnit.HOURS)
            StrictTermType.HOURS_12 -> now.plus(12, ChronoUnit.HOURS)
            StrictTermType.HOURS_24 -> now.plus(24, ChronoUnit.HOURS)
            StrictTermType.DAYS_3 -> now.plus(3, ChronoUnit.DAYS)
            StrictTermType.DAYS_7 -> now.plus(7, ChronoUnit.DAYS)
            StrictTermType.ONGOING -> null
            StrictTermType.UNTIL_DATE_TIME -> customDate.atTime(customTime).atZone(zone).toInstant()
        }

        if (targetInstant != null) {
            val dtf = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy 'at' HH:mm")
            targetInstant.atZone(zone).format(dtf)
        } else {
            "MANUAL EMERGENCY UNLOCK IN SETTINGS"
        }
    }

    Dialog(
        onDismissRequest = { if (!isSealedAnimActive) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = palette.surface,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(2.dp, palette.border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "THE STRICT PLEDGE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp,
                            color = palette.stamp
                        )

                        IconButton(
                            onClick = { if (!isSealedAnimActive) onDismiss() },
                            enabled = !isSealedAnimActive
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = palette.inkSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = palette.hairline, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Commitment Term Options
                    Text(
                        text = "1. CHOOSE COMMITMENT TERM",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val termOptions: List<Pair<StrictTermType, String>> = listOf(
                        StrictTermType.HOURS_24 to "24 HOURS",
                        StrictTermType.DAYS_3 to "3 DAYS",
                        StrictTermType.DAYS_7 to "7 DAYS",
                        StrictTermType.HOURS_12 to "12 HOURS",
                        StrictTermType.ONGOING to "ONGOING",
                        StrictTermType.UNTIL_DATE_TIME to "CUSTOM DATE"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        termOptions.chunked(2).forEach { rowTerms ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for ((term, label) in rowTerms) {
                                    val isSelected = selectedTerm == term
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .border(
                                                BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) palette.border else palette.hairline),
                                                RoundedCornerShape(2.dp)
                                            )
                                            .clickable(enabled = !isSealedAnimActive) { selectedTerm = term },
                                        color = if (isSelected) palette.border else palette.surface
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) palette.surface else palette.ink
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Custom Date/Time Pickers (if UNTIL_DATE_TIME selected)
                    if (selectedTerm == StrictTermType.UNTIL_DATE_TIME) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .border(1.dp, palette.hairline, RoundedCornerShape(2.dp))
                                    .clickable {
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d -> customDate = LocalDate.of(y, m + 1, d) },
                                            customDate.year,
                                            customDate.monthValue - 1,
                                            customDate.dayOfMonth
                                        ).show()
                                    },
                                color = palette.surface
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = palette.inkSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(customDate.toString(), style = MaterialTheme.typography.labelSmall.tabular(), color = palette.ink)
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .border(1.dp, palette.hairline, RoundedCornerShape(2.dp))
                                    .clickable {
                                        TimePickerDialog(
                                            context,
                                            { _, h, min -> customTime = LocalTime.of(h, min) },
                                            customTime.hour,
                                            customTime.minute,
                                            DateFormat.is24HourFormat(context)
                                        ).show()
                                    },
                                color = palette.surface
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = palette.inkSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(customTime.toString(), style = MaterialTheme.typography.labelSmall.tabular(), color = palette.ink)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. THE CONTRACT COVENANT (The Solemn Pledge Copy)
                    Text(
                        text = "2. THE COVENANT",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.5.dp, palette.stamp), RoundedCornerShape(3.dp)),
                        color = palette.stampSurface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "SOLEMN COMMITMENT",
                                style = StampTextStyle,
                                color = palette.stamp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "I commit to ${selectedTerm.label}. I understand that I CANNOT undo, pause, or modify blocked profiles until:\n$commitmentExpiryString.\n\nAndroid settings, uninstallers, and package configurations will be mechanically sealed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.ink,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Bottom: Stamp Appearance & Hold-to-Sign Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // STAMP IMPACT ANIMATION
                    if (isSealedAnimActive) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 14.dp)
                                .scale(stampScale.value)
                                .rotate(-7f)
                                .border(2.5.dp, palette.stamp, RoundedCornerShape(3.dp))
                                .background(palette.stampSurface, RoundedCornerShape(3.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "SEALED & ENFORCED",
                                style = StampTextStyle.copy(fontSize = 16.sp, letterSpacing = 2.5.sp),
                                color = palette.stamp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // HOLD TO SIGN BUTTON
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.surface)
                            .border(BorderStroke(2.dp, palette.stamp), RoundedCornerShape(3.dp))
                            .pointerInput(isSealedAnimActive) {
                                if (isSealedAnimActive) return@pointerInput
                                detectTapGestures(
                                    onPress = {
                                        isSigning = true
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                                        holdJob = scope.launch {
                                            signProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(850, easing = FastOutSlowInEasing)
                                            )
                                            if (signProgress.value >= 0.99f) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                isSealedAnimActive = true

                                                stampScale.animateTo(
                                                    targetValue = 1.0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                )

                                                delay(600)
                                                val customInstant = if (selectedTerm == StrictTermType.UNTIL_DATE_TIME) {
                                                    customDate.atTime(customTime).atZone(zone).toInstant()
                                                } else null
                                                onArm(selectedTerm, customInstant)
                                            }
                                        }

                                        val released = tryAwaitRelease()
                                        isSigning = false
                                        holdJob?.cancel()
                                        if (!isSealedAnimActive) {
                                            scope.launch { signProgress.animateTo(0f, tween(150)) }
                                        }
                                    }
                                )
                            }
                    ) {
                        // Fill animation wash
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(signProgress.value)
                                .background(palette.stamp)
                        )

                        // Button Label
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val textColor = if (signProgress.value > 0.45f) Color.White else palette.stamp
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSealedAnimActive) "PLEDGE SEALED"
                                else if (isSigning) "HOLDING TO SIGN..."
                                else "HOLD TO SIGN THE PLEDGE",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                letterSpacing = 1.4.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Press and hold for 800ms to sign covenant",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

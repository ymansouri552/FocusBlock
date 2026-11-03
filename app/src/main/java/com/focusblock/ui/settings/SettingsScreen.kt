package com.focusblock.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.domain.usecase.EmergencyUnlockStatus
import com.focusblock.ui.components.EmergencyDelayButtons
import com.focusblock.ui.components.SectionHeader
import com.focusblock.ui.components.StampLabel
import com.focusblock.ui.theme.CountdownNumericStyle
import com.focusblock.ui.theme.HeroNumericStyle
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.StampTextStyle
import com.focusblock.ui.theme.tabular
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val palette = LocalPledgePalette.current

    var emergencyRowExpanded by remember { mutableStateOf(false) }
    var showFullWaitingScreen by remember { mutableStateOf(false) }

    // Keep strict countdown active
    LaunchedEffect(ui.isStrictActive) {
        if (ui.isStrictActive) {
            while (true) {
                delay(1_000)
                viewModel.refreshRuntimeState()
            }
        }
    }

    // Auto-surface full-screen waiting modal if waiting status is actively running
    LaunchedEffect(ui.emergencyUnlockStatus) {
        if (ui.emergencyUnlockStatus is EmergencyUnlockStatus.Waiting) {
            showFullWaitingScreen = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS & VAULT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = palette.ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.close),
                            tint = palette.ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.background
                )
            )
        },
        containerColor = palette.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 48.dp)
        ) {
            // ─────────────────────────────────────────────────────────────────
            // 1. APPEARANCE SECTION
            // ─────────────────────────────────────────────────────────────────
            SectionHeader(
                title = "APPEARANCE",
                modifier = Modifier.padding(top = 12.dp)
            )

            // Three flat square segments: System, Dark, Light
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val themeModes = listOf(
                    "SYSTEM" to "SYSTEM",
                    "DARK" to "DARK (OBSIDIAN)",
                    "LIGHT" to "LIGHT (PAPER)"
                )

                themeModes.forEach { (mode, label) ->
                    val isSelected = ui.themeMode.equals(mode, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .border(
                                BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) palette.border else palette.hairline),
                                RoundedCornerShape(2.dp)
                            )
                            .clickable { viewModel.setThemeMode(mode) },
                        color = if (isSelected) palette.accent else palette.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) palette.onAccent else palette.ink,
                                letterSpacing = 0.8.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = palette.hairline, thickness = 1.dp)

            // ─────────────────────────────────────────────────────────────────
            // 2. STRICT TAMPER PROTECTION SECTION
            // ─────────────────────────────────────────────────────────────────
            SectionHeader(
                title = "STRICT TAMPER PROTECTION",
                modifier = Modifier.padding(top = 16.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = palette.surface,
                border = BorderStroke(1.dp, palette.hairline),
                shape = RoundedCornerShape(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BLOCK BROWSERS IN STRICT MODE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.ink
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Restricts browser applications while strict mode is actively sealed to prevent web circumvention.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.inkSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = ui.blockBrowsersInStrict,
                            onCheckedChange = { viewModel.setBlockBrowsersInStrict(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.onAccent,
                                checkedTrackColor = palette.accent,
                                uncheckedThumbColor = palette.inkSecondary,
                                uncheckedTrackColor = palette.surface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = palette.hairline, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ENFORCEMENT POLICY",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary,
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Shields Android device settings, system package installers, and accessibility managers during commitment periods to prevent schedule bypassing or uninstallation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = palette.hairline, thickness = 1.dp)

            // ─────────────────────────────────────────────────────────────────
            // 3. EMERGENCY UNLOCK SECTION
            // ─────────────────────────────────────────────────────────────────
            SectionHeader(
                title = "EMERGENCY UNLOCK",
                modifier = Modifier.padding(top = 16.dp)
            )

            if (ui.isStrictActive) {
                // COLLAPSED SINGLE SUMMARY ROW WHILE STRICT MODE IS ACTIVE
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(BorderStroke(1.5.dp, palette.stamp), RoundedCornerShape(2.dp))
                        .clickable { emergencyRowExpanded = !emergencyRowExpanded },
                    color = palette.stampSurface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "EMERGENCY UNLOCK · ${ui.emergencyUnlockMinutes} MIN DELAY · CONFIG LOCKED",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.stamp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap to review emergency bypass action",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.inkSecondary
                                )
                            }
                            Icon(
                                imageVector = if (emergencyRowExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = palette.stamp
                            )
                        }

                        // Expanded contents on tap
                        AnimatedVisibility(
                            visible = emergencyRowExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                HorizontalDivider(color = palette.stamp.copy(alpha = 0.3f), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))

                                when (val status = ui.emergencyUnlockStatus) {
                                    EmergencyUnlockStatus.Disabled -> {
                                        Text(
                                            text = "Emergency unlock was disabled when this pledge was signed. No bypass available.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.ink
                                        )
                                    }
                                    EmergencyUnlockStatus.NotRequested -> {
                                        Text(
                                            text = "Mandatory cooling-off delay: ${ui.emergencyUnlockMinutes} minutes. If initiated, you must wait out the full timer before unlocking.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.ink
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                viewModel.requestEmergencyUnlock()
                                                showFullWaitingScreen = true
                                            },
                                            shape = RoundedCornerShape(2.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = palette.stamp,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("START ${ui.emergencyUnlockMinutes}-MINUTE COOLING TIMER", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    is EmergencyUnlockStatus.Waiting -> {
                                        Text(
                                            text = "Cooling timer running: ${formatUnlockCountdown(status.remainingSeconds)} remaining",
                                            style = MaterialTheme.typography.titleSmall.tabular(),
                                            fontWeight = FontWeight.Bold,
                                            color = palette.stamp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { showFullWaitingScreen = true },
                                                shape = RoundedCornerShape(2.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = palette.border,
                                                    contentColor = palette.surface
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("VIEW COUNTDOWN")
                                            }
                                            Button(
                                                onClick = { viewModel.cancelEmergencyUnlock() },
                                                shape = RoundedCornerShape(2.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = palette.hairline,
                                                    contentColor = palette.ink
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("CANCEL")
                                            }
                                        }
                                    }
                                    EmergencyUnlockStatus.Available -> {
                                        Text(
                                            text = "Delay complete. Unlock is now available.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.stamp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.confirmEmergencyUnlock() },
                                            shape = RoundedCornerShape(2.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = palette.stamp,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("UNLOCK STRICT MODE NOW", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // NORMAL IDLE STATE: FLAT TOGGLE & DELAY SELECTOR
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = palette.surface,
                    border = BorderStroke(1.dp, palette.hairline),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PERMIT EMERGENCY UNLOCK",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.ink
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Allows an intentional delay before disarming strict mode in true emergencies.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.inkSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Switch(
                                checked = ui.emergencyUnlockEnabled,
                                onCheckedChange = { viewModel.setEmergencyUnlockEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = palette.onAccent,
                                    checkedTrackColor = palette.accent,
                                    uncheckedThumbColor = palette.inkSecondary,
                                    uncheckedTrackColor = palette.surface
                                )
                            )
                        }

                        if (ui.emergencyUnlockEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = palette.hairline, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "MANDATORY COOLING-OFF DELAY",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.inkSecondary,
                                letterSpacing = 1.0.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            EmergencyDelayButtons(
                                selectedMinutes = ui.emergencyUnlockMinutes,
                                onSelectMinutes = { viewModel.setEmergencyUnlockMinutes(it) },
                                enabled = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = palette.hairline, thickness = 1.dp)

            // ─────────────────────────────────────────────────────────────────
            // 4. LOCAL ARCHITECTURE & LIMITS
            // ─────────────────────────────────────────────────────────────────
            SectionHeader(
                title = "LOCAL ARCHITECTURE & LIMITS",
                modifier = Modifier.padding(top = 16.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = palette.surface,
                border = BorderStroke(1.dp, palette.hairline),
                shape = RoundedCornerShape(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "100% OFFLINE DATA LEDGER",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "FocusBlock operates entirely on-device. No telemetry, screen data, usage metrics, or logs ever leave your local device storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = palette.hairline, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "TECHNICAL ENFORCEMENT LIMITS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Strict mode closes software tampering vectors. It cannot physically prevent Safe Mode reboots, Android ADB terminal commands, or factory device wipes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "FocusBlock Build v${com.focusblock.BuildConfig.VERSION_NAME} · Native Kotlin & Compose",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FULL-SCREEN DELIBERATE EMERGENCY UNLOCK WAITING SCREEN
    // ─────────────────────────────────────────────────────────────────────────
    if (showFullWaitingScreen && ui.emergencyUnlockStatus is EmergencyUnlockStatus.Waiting) {
        val status = ui.emergencyUnlockStatus as EmergencyUnlockStatus.Waiting
        Dialog(
            onDismissRequest = { showFullWaitingScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = palette.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showFullWaitingScreen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.ink)
                        }
                    }

                    // Body: Sober copy and large deliberate countdown
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        StampLabel(
                            text = "COOLING-OFF PERIOD",
                            rotation = 0f
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large deliberate tabular countdown
                        Text(
                            text = formatUnlockCountdown(status.remainingSeconds),
                            style = HeroNumericStyle,
                            color = palette.stamp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ARE YOU SURE?",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.ink,
                            letterSpacing = 1.0.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "An emergency unlock was initiated. A mandatory waiting delay is being enforced.\n\nTake a breath. Pausing before acting breaks the impulse loop. If this request was impulsive, cancel now to keep your pledge intact.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.inkSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }

                    // Action: Prominent cancel button
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                viewModel.cancelEmergencyUnlock()
                                showFullWaitingScreen = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accent,
                                contentColor = palette.onAccent
                            )
                        ) {
                            Text(
                                text = "CANCEL REQUEST & MAINTAIN PLEDGE",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { showFullWaitingScreen = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LET TIMER RUN IN BACKGROUND",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.inkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatUnlockCountdown(totalSeconds: Long): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0L)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

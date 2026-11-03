package com.focusblock.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.focusblock.R
import com.focusblock.domain.usecase.SystemPermissionsState
import com.focusblock.ui.components.SectionHeader
import com.focusblock.ui.components.StampLabel
import com.focusblock.ui.theme.LocalPledgePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    permissionsState: SystemPermissionsState,
    onRefreshPermissions: () -> Unit,
    onFixAccessibility: () -> Unit,
    onFixDeviceAdmin: () -> Unit,
    onComplete: () -> Unit
) {
    val palette = LocalPledgePalette.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FOCUSBLOCK COVENANT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = palette.ink
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.background
                )
            )
        },
        containerColor = palette.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "VAULT INITIALIZATION",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "FocusBlock requires local Android system permissions to enforce your blocking contracts and tamper seals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.inkSecondary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Single checklist card with hairline separators
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp),
                    color = palette.surface,
                    border = BorderStroke(1.5.dp, palette.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Item 1: Accessibility Service (Required)
                        PermissionChecklistItem(
                            title = "ACCESSIBILITY ENFORCER",
                            tag = "MANDATORY",
                            description = stringResource(id = R.string.perm_accessibility_summary),
                            isGranted = permissionsState.isAccessibilityGranted,
                            actionLabel = "ENABLE",
                            onAction = onFixAccessibility
                        )

                        HorizontalDivider(color = palette.hairline, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                        // Item 2: Device Administrator (Optional / Strict Mode)
                        PermissionChecklistItem(
                            title = "DEVICE ADMINISTRATOR",
                            tag = "STRICT PROTECTION",
                            description = stringResource(id = R.string.perm_device_admin_summary),
                            isGranted = permissionsState.isDeviceAdminGranted,
                            actionLabel = "ACTIVATE",
                            onAction = onFixDeviceAdmin
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quiet local data notice
                Text(
                    text = "All data stays local on your device. FocusBlock does not collect or transmit screen content, usage logs, or personal records.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.inkSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Single primary Finish / Get Started button
            Button(
                onClick = onComplete,
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
                    text = "ENTER VAULT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionChecklistItem(
    title: String,
    tag: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    val palette = LocalPledgePalette.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isGranted) palette.accent else palette.inkSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = palette.inkSecondary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (isGranted) {
            Surface(
                color = palette.accent,
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    text = "READY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.onAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } else {
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.5.dp, palette.border),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

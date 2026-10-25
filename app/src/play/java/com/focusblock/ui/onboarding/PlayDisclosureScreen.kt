package com.focusblock.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusblock.ui.theme.FocusBlockTheme

@Composable
fun PlayDisclosureScreen(
    onAccessibilityConsent: () -> Unit,
    onDeviceAdminConsent: () -> Unit,
    onComplete: () -> Unit
) {
    var accessibilityConsent by remember { mutableStateOf(false) }
    var deviceAdminConsent by remember { mutableStateOf(false) }

    FocusBlockTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Important permissions") },
                    colors = TopAppBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                Text(
                    text = "FocusBlock Accessibility & Device Admin Disclosure",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(id = R.string.play_disclosure_body),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Checkbox(
                    checked = accessibilityConsent,
                    onCheckedChange = { onAccessibilityConsent(); accessibilityConsent = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(id = R.string.play_disclosure_accessibility),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Checkbox(
                    checked = deviceAdminConsent,
                    onCheckedChange = { onDeviceAdminConsent(); deviceAdminConsent = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(id = R.string.play_disclosure_device_admin),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = { if (accessibilityConsent && deviceAdminConsent) onComplete() },
                    enabled = accessibilityConsent && deviceAdminConsent
                ) {
                    Text(text = "I understand and consent")
                }
            }
        }
    }
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)
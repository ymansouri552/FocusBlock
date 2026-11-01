package com.focusblock.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.focusblock.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayDisclosureScreen(
    onComplete: () -> Unit
) {
    var accessibilityConsent by remember { mutableStateOf(false) }
    var deviceAdminConsent by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.play_disclosure_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.play_disclosure_heading),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(id = R.string.play_disclosure_body),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Checkbox(
                checked = accessibilityConsent,
                onCheckedChange = { accessibilityConsent = it },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(id = R.string.play_disclosure_accessibility),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Checkbox(
                checked = deviceAdminConsent,
                onCheckedChange = { deviceAdminConsent = it },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(id = R.string.play_disclosure_device_admin),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = onComplete,
                enabled = accessibilityConsent && deviceAdminConsent
            ) {
                Text(text = stringResource(id = R.string.play_disclosure_consent))
            }
        }
    }
}

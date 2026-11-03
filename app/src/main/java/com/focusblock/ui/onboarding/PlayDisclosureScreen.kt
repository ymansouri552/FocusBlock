package com.focusblock.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.R
import com.focusblock.ui.theme.LocalPledgePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayDisclosureScreen(
    onComplete: () -> Unit
) {
    var accessibilityConsent by remember { mutableStateOf(false) }
    var deviceAdminConsent by remember { mutableStateOf(false) }
    val palette = LocalPledgePalette.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LEGAL DISCLOSURE",
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
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.play_disclosure_heading).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = palette.ink
            )

            Text(
                text = stringResource(id = R.string.play_disclosure_body),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.inkSecondary,
                lineHeight = 22.sp
            )

            // Consent Card 1
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .clickable { accessibilityConsent = !accessibilityConsent },
                shape = RoundedCornerShape(2.dp),
                color = palette.surface,
                border = BorderStroke(
                    if (accessibilityConsent) 2.dp else 1.dp,
                    if (accessibilityConsent) palette.border else palette.hairline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = accessibilityConsent,
                        onCheckedChange = { accessibilityConsent = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = palette.border,
                            checkmarkColor = palette.surface,
                            uncheckedColor = palette.hairline
                        )
                    )
                    Text(
                        text = stringResource(id = R.string.play_disclosure_accessibility),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.ink,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Consent Card 2
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .clickable { deviceAdminConsent = !deviceAdminConsent },
                shape = RoundedCornerShape(2.dp),
                color = palette.surface,
                border = BorderStroke(
                    if (deviceAdminConsent) 2.dp else 1.dp,
                    if (deviceAdminConsent) palette.border else palette.hairline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = deviceAdminConsent,
                        onCheckedChange = { deviceAdminConsent = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = palette.border,
                            checkmarkColor = palette.surface,
                            uncheckedColor = palette.hairline
                        )
                    )
                    Text(
                        text = stringResource(id = R.string.play_disclosure_device_admin),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.ink,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onComplete,
                enabled = accessibilityConsent && deviceAdminConsent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.onAccent,
                    disabledContainerColor = palette.hairline
                )
            ) {
                Text(
                    text = "AGREE & PROCEED",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )
            }
        }
    }
}

package com.focusblock.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.focusblock.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            titleRes = R.string.onboarding_welcome_title,
            descriptionRes = R.string.onboarding_welcome_description,
            permissionRes = null,
            statusGrantedRes = null
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_accessibility,
            descriptionRes = R.string.perm_accessibility_summary,
            permissionRes = R.string.perm_accessibility,
            statusGrantedRes = R.string.onboarding_permission_not_granted
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_device_admin,
            descriptionRes = R.string.perm_device_admin_summary,
            permissionRes = R.string.perm_device_admin,
            statusGrantedRes = R.string.onboarding_permission_not_granted
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_local_data_title,
            descriptionRes = R.string.onboarding_local_data_description,
            permissionRes = null,
            statusGrantedRes = null
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.onboarding_title)) },
                navigationIcon = {
                    if (page > 0) {
                        IconButton(onClick = { page -= 1 }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.onboarding_back)
                            )
                        }
                    }
                },
                actions = {
                    if (page == pages.lastIndex) {
                        TextButton(onClick = onComplete) {
                            Text(text = stringResource(id = R.string.onboarding_done))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            val current = pages[page]
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = current.titleRes),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = current.descriptionRes))
            Spacer(modifier = Modifier.height(32.dp))
            if (current.permissionRes != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = current.permissionRes),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.onboarding_permission_not_granted),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (page > 0) {
                    TextButton(onClick = { page -= 1 }) {
                        Text(text = stringResource(id = R.string.onboarding_back))
                    }
                }
                Button(
                    onClick = { if (page < pages.lastIndex) page += 1 else onComplete() }
                ) {
                    Text(
                        text = if (page < pages.lastIndex) {
                            stringResource(id = R.string.onboarding_next)
                        } else {
                            stringResource(id = R.string.onboarding_done)
                        }
                    )
                }
            }
        }
    }
}

private data class OnboardingPage(
    val titleRes: Int,
    val descriptionRes: Int,
    val permissionRes: Int? = null,
    val statusGrantedRes: Int? = null
)

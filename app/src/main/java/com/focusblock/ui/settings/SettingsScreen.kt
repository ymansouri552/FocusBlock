package com.focusblock.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.data.local.datastore.StrictLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.close)
                        )
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
            Text(
                text = stringResource(id = R.string.settings_strict_level),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.settings_strict_level_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            StrictLevel.entries.forEach { level ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.setStrictLevel(level.value) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = ui.strictLevel == level.value,
                        onClick = { viewModel.setStrictLevel(level.value) }
                    )
                    Column {
                        Text(
                            text = if (level == StrictLevel.TARGETED) {
                                stringResource(id = R.string.settings_strict_targeted)
                            } else {
                                stringResource(id = R.string.settings_strict_aggressive)
                            }
                        )
                        Text(
                            text = if (level == StrictLevel.TARGETED) {
                                stringResource(id = R.string.settings_strict_targeted_summary)
                            } else {
                                stringResource(id = R.string.settings_strict_aggressive_summary)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(id = R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.settings_theme_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.settings_emergency_unlock),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.settings_emergency_unlock_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.settings_about),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.about_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.about_limit_safemode),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(id = R.string.about_limit_adb),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(id = R.string.about_limit_factory_reset),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(id = R.string.about_limit_second_device),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

package com.focusblock.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.focusblock.R
import com.focusblock.domain.model.ScheduleEngine
import com.focusblock.domain.usecase.StrictModeRules
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddProfile: () -> Unit,
    onEditProfile: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val ui by viewModel.ui
    val profiles by viewModel.profiles
    val blockedPackages by viewModel.blockedPackages

    var showQuickBlock by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ui.error?.let { error ->
        LaunchedEffect(key1 = error) {
            scope.launch { snackbarHostState.showSnackbar(error) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.home_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.settings_title)
                        )
                    }
                    IconButton(onClick = onAddProfile) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.home_add_profile)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text = stringResource(id = R.string.home_active_blocks),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (profiles.isEmpty()) {
                Text(text = stringResource(id = R.string.home_no_active_blocks))
            } else {
                LazyColumn {
                    items(profiles) { profile ->
                        val isActive = profile.enabled
                        val now = Instant.now()
                        val isStrictActive = StrictModeRules.isStrictActive(profile, now)
                        val timeRemaining = ScheduleEngine.activeProfile(profile, now)
                            ?.let { Duration.between(now, it.endInstant).toMillis() }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable { onEditProfile(profile.id) },
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isActive) 4.dp else 0.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = profile.name, style = MaterialTheme.typography.titleMedium)
                                    if (isActive && timeRemaining != null && timeRemaining > 0) {
                                        Text(
                                            text = stringResource(id = R.string.home_time_remaining) + ": " +
                                                formatCountdown(timeRemaining),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = profile.blockedApps.take(5).joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                                Switch(
                                    checked = isActive,
                                    enabled = !isStrictActive,
                                    onCheckedChange = { viewModel.toggleProfile(profile.id, it) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (blockedPackages.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.home_blocked_apps) + ": " + blockedPackages.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showQuickBlock = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.home_quick_block))
            }
        }
    }

    if (showQuickBlock) {
        QuickBlockDialog(
            onDismiss = { showQuickBlock = false },
            onBlock = { /* handle quick block */ }
        )
    }
}

@Composable
private fun QuickBlockDialog(onDismiss: () -> Unit, onBlock: (Set<String>) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onBlock(emptySet()) }) { Text(stringResource(id = R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) }
        },
        title = { Text(stringResource(id = R.string.home_quick_block)) }
    ) {
        Text(text = stringResource(id = R.string.quick_block_placeholder))
    }
}

private fun formatCountdown(ms: Long): String {
    val totalMinutes = ms / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "%dh %02dm".format(hours, minutes) else "%dm".format(minutes)
}

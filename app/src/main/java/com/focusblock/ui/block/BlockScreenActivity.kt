package com.focusblock.ui.block

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.focusblock.R
import com.focusblock.ui.theme.FocusBlockTheme

class BlockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: getString(R.string.block_unknown_app)
        val profileName = intent.getStringExtra(EXTRA_PROFILE_NAME) ?: getString(R.string.block_unknown_profile)
        val timeRemaining = intent.getLongExtra(EXTRA_TIME_REMAINING_MS, 0L)
        val isStrict = intent.getBooleanExtra(EXTRA_IS_STRICT, false)

        // Back must not return to the blocked app; send the user home instead.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    moveTaskToBack(true)
                }
            }
        )

        setContent {
            FocusBlockTheme {
                BlockScreen(
                    appName = appName,
                    profileName = profileName,
                    timeRemainingMs = timeRemaining,
                    isStrict = isStrict,
                    onGoHome = { finishAndRemoveTask() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_PROFILE_NAME = "profile_name"
        const val EXTRA_TIME_REMAINING_MS = "time_remaining_ms"
        const val EXTRA_IS_STRICT = "is_strict"
    }
}

@Composable
fun BlockScreen(
    appName: String,
    profileName: String,
    timeRemainingMs: Long,
    isStrict: Boolean,
    onGoHome: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.block_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(id = R.string.block_app_label, appName))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(id = R.string.block_profile_label, profileName))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.block_time_remaining_label) + ": " + formatTime(timeRemainingMs)
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (isStrict) {
                Text(
                    text = stringResource(id = R.string.block_strict_message),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Button(onClick = onGoHome) {
                Text(text = stringResource(id = R.string.block_go_home))
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

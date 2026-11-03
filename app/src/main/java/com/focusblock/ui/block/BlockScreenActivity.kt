package com.focusblock.ui.block

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusblock.R
import com.focusblock.data.local.datastore.SettingsDataStore
import com.focusblock.ui.theme.FocusBlockTheme
import com.focusblock.ui.theme.LocalCustomColors
import com.focusblock.ui.theme.tabular
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class BlockScreenActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: getString(R.string.block_unknown_app)
        val profileName = intent.getStringExtra(EXTRA_PROFILE_NAME) ?: getString(R.string.block_unknown_profile)
        val timeRemaining = intent.getLongExtra(EXTRA_TIME_REMAINING_MS, 0L)
        val isStrict = intent.getBooleanExtra(EXTRA_IS_STRICT, false)
        val blockReason = intent.getStringExtra(EXTRA_BLOCK_REASON)

        // Back sends the user home instead of returning to the blocked app.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    goToHomeScreen()
                }
            }
        )

        setContent {
            val themeMode by settingsDataStore.themeMode.collectAsStateWithLifecycle(initialValue = "DARK")
            FocusBlockTheme(themeMode = themeMode) {
                BlockScreen(
                    appName = appName,
                    profileName = profileName,
                    timeRemainingMs = timeRemaining,
                    isStrict = isStrict,
                    blockReason = blockReason,
                    onGoHome = { goToHomeScreen() }
                )
            }
        }
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(homeIntent)
        } catch (e: Exception) {
            // Gracefully ignore
        }
        finish()
    }

    companion object {
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_PROFILE_NAME = "profile_name"
        const val EXTRA_TIME_REMAINING_MS = "time_remaining_ms"
        const val EXTRA_IS_STRICT = "is_strict"
        const val EXTRA_BLOCK_REASON = "block_reason"
    }
}

@Composable
fun BlockScreen(
    appName: String,
    profileName: String,
    timeRemainingMs: Long,
    isStrict: Boolean,
    blockReason: String? = null,
    onGoHome: () -> Unit
) {
    val customColors = LocalCustomColors.current
    var remainingMs by remember(timeRemainingMs) { mutableLongStateOf(timeRemainingMs) }

    LaunchedEffect(timeRemainingMs) {
        val startWall = System.currentTimeMillis()
        val initialRemaining = timeRemainingMs
        while (remainingMs > 0) {
            delay(500)
            val elapsed = System.currentTimeMillis() - startWall
            remainingMs = (initialRemaining - elapsed).coerceAtLeast(0L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Quiet identification
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.labelMedium,
                color = customColors.subtleText,
                letterSpacing = 1.sp
            )

            // Center: The signature block moment — bold architectural typography & prominent timer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (blockReason != null) {
                        blockReason
                    } else {
                        "Blocked by $profileName"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = customColors.subtleText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Prominent timer as the focal point
                if (remainingMs > 0) {
                    Text(
                        text = formatTime(remainingMs),
                        style = MaterialTheme.typography.displayLarge.tabular(),
                        fontWeight = FontWeight.Bold,
                        color = if (isStrict) customColors.strictAmber else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (blockReason != null) "Quota resets in" else stringResource(id = R.string.block_time_remaining),
                        style = MaterialTheme.typography.bodySmall,
                        color = customColors.subtleText,
                        textAlign = TextAlign.Center
                    )
                }

                if (isStrict) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = customColors.strictContainer,
                        border = BorderStroke(1.dp, customColors.strictAmber.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = customColors.strictAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Strict Mode active · Cannot be unblocked",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = customColors.strictOnContainer
                            )
                        }
                    }
                }
            }

            // Bottom action: single, prominent button
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.block_return_home),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

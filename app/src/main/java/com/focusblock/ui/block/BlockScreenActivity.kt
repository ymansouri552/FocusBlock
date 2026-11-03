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
import com.focusblock.ui.components.StampLabel
import com.focusblock.ui.theme.FocusBlockTheme
import com.focusblock.ui.theme.HeroNumericStyle
import com.focusblock.ui.theme.LocalPledgePalette
import com.focusblock.ui.theme.StampTextStyle
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
    val palette = LocalPledgePalette.current
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
            .background(palette.background)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "FOCUSBLOCK VAULT",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.inkSecondary,
                    letterSpacing = 2.0.sp
                )
            }

            // Center Block Focal Point
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isStrict) {
                    StampLabel(
                        text = "ACCESS SEALED",
                        rotation = -6f
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = appName.uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (blockReason != null) blockReason.uppercase() else "RESTRICTED BY $profileName".uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.inkSecondary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.0.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Prominent Tabular Hero Timer
                if (remainingMs > 0) {
                    Text(
                        text = formatTime(remainingMs),
                        style = HeroNumericStyle,
                        color = if (isStrict) palette.stamp else palette.accent,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (blockReason != null) "QUOTA RESETS AT END OF WINDOW" else "REMAINING UNTIL VAULT REOPENS",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.0.sp
                    )
                }

                if (isStrict) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Strict Mode Pledge Enforced · Config Tamper Proof",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.stamp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bottom action
            Button(
                onClick = onGoHome,
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
                    text = "RETURN TO HOME",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
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

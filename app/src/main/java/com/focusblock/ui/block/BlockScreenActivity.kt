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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.R
import com.focusblock.ui.theme.FocusBlockTheme
import com.focusblock.ui.theme.LocalCustomColors

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
                    goToHomeScreen()
                }
            }
        )

        setContent {
            FocusBlockTheme(darkTheme = true) {
                BlockScreen(
                    appName = appName,
                    profileName = profileName,
                    timeRemainingMs = timeRemaining,
                    isStrict = isStrict,
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
    val customColors = LocalCustomColors.current

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF0B0F19),
            Color(0xFF05070D)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Shield Icon
            Surface(
                shape = CircleShape,
                color = if (isStrict) customColors.strictAmber.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                border = BorderStroke(
                    1.5.dp,
                    if (isStrict) customColors.strictAmber.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isStrict) Icons.Default.Security else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isStrict) customColors.strictAmber else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Take a breath",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "This App is Blocked",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Name & Profile Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Time Remaining Display Card
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TIME REMAINING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatTime(timeRemainingMs),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isStrict) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp),
                    color = customColors.strictContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, customColors.strictAmber.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = customColors.strictAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Strict Mode Enforced · Bypassing is locked until the session finishes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = customColors.strictOnContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Return to Home", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

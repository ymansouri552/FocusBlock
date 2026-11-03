package com.focusblock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.ui.theme.LocalPledgePalette

/**
 * Ledger-style segment selector for emergency unlock delay durations (10m, 15m, 30m).
 * Square-cornered segments with high-contrast active fill.
 */
@Composable
fun EmergencyDelayButtons(
    selectedMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isStrictTheme: Boolean = false
) {
    val palette = LocalPledgePalette.current
    val options = listOf(10, 15, 30)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { mins ->
            val isSelected = selectedMinutes == mins

            val activeBg = if (isStrictTheme) palette.stamp else palette.accent
            val activeText = if (isStrictTheme) palette.surface else palette.onAccent

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .border(
                        BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) palette.border else palette.hairline),
                        RoundedCornerShape(2.dp)
                    )
                    .then(if (!enabled) Modifier.alpha(0.4f) else Modifier)
                    .clickable(enabled = enabled) { onSelectMinutes(mins) },
                color = if (isSelected) activeBg else palette.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${mins}M DELAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) activeText else palette.ink,
                        letterSpacing = 1.0.sp
                    )
                }
            }
        }
    }
}

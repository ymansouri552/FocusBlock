package com.focusblock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.ui.theme.LocalCustomColors

/**
 * Modern, symmetrical 3-button selector for emergency unlock delay durations (10m, 15m, 30m).
 * Ensures all three buttons have identical widths, heights, corner shapes, and typography.
 */
@Composable
fun EmergencyDelayButtons(
    selectedMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isStrictTheme: Boolean = false
) {
    val customColors = LocalCustomColors.current
    val options = listOf(10, 15, 30)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { mins ->
            val isSelected = selectedMinutes == mins

            val activeContainerColor = if (isStrictTheme) {
                customColors.strictContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            }

            val activeBorderColor = if (isStrictTheme) {
                customColors.strictAmber
            } else {
                MaterialTheme.colorScheme.primary
            }

            val activeTextColor = if (isStrictTheme) {
                customColors.strictAmber
            } else {
                MaterialTheme.colorScheme.primary
            }

            OutlinedCard(
                onClick = { if (enabled) onSelectMinutes(mins) },
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .then(if (!enabled) Modifier.alpha(0.5f) else Modifier),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected) activeContainerColor else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) activeBorderColor else customColors.cardBorder
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${mins}m",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) activeTextColor else MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "delay",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) activeTextColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

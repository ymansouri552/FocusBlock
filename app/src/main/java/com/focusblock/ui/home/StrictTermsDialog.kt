package com.focusblock.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.R
import com.focusblock.domain.model.StrictTermType
import com.focusblock.ui.theme.LocalCustomColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun StrictTermsDialog(
    onDismiss: () -> Unit,
    onArm: (termType: StrictTermType, customEndInstant: Instant?) -> Unit
) {
    val context = LocalContext.current
    val customColors = LocalCustomColors.current
    var selectedTerm by remember { mutableStateOf(StrictTermType.HOURS_24) }

    // Custom date & time state
    val tomorrow = remember { LocalDate.now().plusDays(1) }
    var customDate by remember { mutableStateOf(tomorrow) }
    var customTime by remember { mutableStateOf(LocalTime.of(8, 0)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = customColors.strictContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = customColors.strictAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(id = R.string.strict_terms_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Independent Protection",
                        style = MaterialTheme.typography.bodySmall,
                        color = customColors.strictAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.strict_terms_dialog_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ── Presets List ─────────────────────────────────────────────
                val presets = listOf(
                    StrictTermType.HOURS_4 to "Focus Sprint · 4h lock",
                    StrictTermType.HOURS_8 to "Workday · 8h lock",
                    StrictTermType.HOURS_12 to "Half Day · 12h lock",
                    StrictTermType.HOURS_24 to "1 Full Day · 24h discipline",
                    StrictTermType.DAYS_3 to "3 Days · Extended reset",
                    StrictTermType.DAYS_7 to "1 Week · Habit transformation",
                    StrictTermType.UNTIL_DATE_TIME to "Custom Date & Time",
                    StrictTermType.ONGOING to "Ongoing Commitment"
                )

                presets.forEach { (term, subtitle) ->
                    val isSelected = selectedTerm == term
                    OutlinedCard(
                        onClick = { selectedTerm = term },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) customColors.strictContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) customColors.strictAmber else customColors.cardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) customColors.strictAmber else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = term.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) customColors.strictAmber else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ── Custom Date/Time Pickers (shown if UNTIL_DATE_TIME selected) ──
                if (selectedTerm == StrictTermType.UNTIL_DATE_TIME) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Select Unlock Target",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = customColors.strictAmber
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d -> customDate = LocalDate.of(y, m + 1, d) },
                                            customDate.year,
                                            customDate.monthValue - 1,
                                            customDate.dayOfMonth
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = customDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                                        fontSize = 12.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute -> customTime = LocalTime.of(hour, minute) },
                                            customTime.hour,
                                            customTime.minute,
                                            DateFormat.is24HourFormat(context)
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = customTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Strict Rules Commitment Card ──────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = customColors.strictContainer.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, customColors.strictAmber.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "STRICT COMMITMENT RULES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = customColors.strictAmber,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• Recurring profiles stay locked 24/7 even between sessions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Profiles cannot be disabled, shortened, or deleted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Blocked apps cannot be removed (only adding apps allowed).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Device Administrator blocks app uninstall.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Cannot be disarmed until the selected term expires.",
                            style = MaterialTheme.typography.bodySmall,
                            color = customColors.strictAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customEnd = if (selectedTerm == StrictTermType.UNTIL_DATE_TIME) {
                        customDate.atTime(customTime).atZone(ZoneId.systemDefault()).toInstant()
                    } else {
                        null
                    }
                    onArm(selectedTerm, customEnd)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.strictAmber,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(id = R.string.strict_arm_button), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

package com.focusblock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusblock.R
import com.focusblock.ui.theme.LocalPledgePalette

enum class MainTab {
    FOCUS,
    STATISTICS
}

@Composable
fun FocusBlockBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPledgePalette.current

    NavigationBar(
        modifier = modifier
            .height(68.dp)
            .drawBehind {
                // Top hairline border
                drawLine(
                    color = palette.hairline,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
        containerColor = palette.surface,
        tonalElevation = 0.dp
    ) {
        // Tab 1: Focus
        BottomNavItem(
            selected = currentTab == MainTab.FOCUS,
            onClick = { onTabSelected(MainTab.FOCUS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.FOCUS) Icons.Filled.HourglassBottom else Icons.Outlined.HourglassEmpty,
                    contentDescription = stringResource(R.string.tab_focus),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = stringResource(R.string.tab_focus)
        )

        // Tab 2: Statistics
        BottomNavItem(
            selected = currentTab == MainTab.STATISTICS,
            onClick = { onTabSelected(MainTab.STATISTICS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.STATISTICS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = stringResource(R.string.tab_statistics),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = stringResource(R.string.tab_statistics)
        )
    }
}

@Composable
private fun RowScope.BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String
) {
    val palette = LocalPledgePalette.current

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    letterSpacing = 1.0.sp
                ),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = palette.accent,
            selectedTextColor = palette.accent,
            indicatorColor = palette.accent.copy(alpha = 0.16f),
            unselectedIconColor = palette.inkSecondary,
            unselectedTextColor = palette.inkSecondary
        )
    )
}

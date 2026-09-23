package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NavigationTab
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleLightBorder
import com.example.ui.theme.AppleTextSecondary

@Composable
fun SalimBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFC))
            .navigationBarsPadding()
    ) {
        HorizontalDivider(thickness = 0.5.dp, color = AppleLightBorder)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(
                tab = NavigationTab.RECENTS,
                isSelected = currentTab == NavigationTab.RECENTS,
                onClick = { onTabSelected(NavigationTab.RECENTS) },
                activeIcon = Icons.Filled.Schedule,
                inactiveIcon = Icons.Outlined.Schedule
            )

            TabItem(
                tab = NavigationTab.BROWSE,
                isSelected = currentTab == NavigationTab.BROWSE,
                onClick = { onTabSelected(NavigationTab.BROWSE) },
                activeIcon = Icons.Filled.Folder,
                inactiveIcon = Icons.Outlined.Folder
            )

            TabItem(
                tab = NavigationTab.FAVORITES,
                isSelected = currentTab == NavigationTab.FAVORITES,
                onClick = { onTabSelected(NavigationTab.FAVORITES) },
                activeIcon = Icons.Filled.Bookmark,
                inactiveIcon = Icons.Outlined.BookmarkBorder
            )

            TabItem(
                tab = NavigationTab.STORAGE,
                isSelected = currentTab == NavigationTab.STORAGE,
                onClick = { onTabSelected(NavigationTab.STORAGE) },
                activeIcon = Icons.Filled.PieChart,
                inactiveIcon = Icons.Outlined.PieChartOutline
            )
        }
    }
}

@Composable
private fun TabItem(
    tab: NavigationTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector
) {
    val tint = if (isSelected) AppleBlue else AppleTextSecondary

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 28.dp),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) activeIcon else inactiveIcon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = tint,
                    fontSize = 11.sp
                )
            )
        }
    }
}

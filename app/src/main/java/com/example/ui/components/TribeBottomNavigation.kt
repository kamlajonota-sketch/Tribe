package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.TribeLanguageContext

enum class TribeNavTab {
    HOME,
    MY_PLANS,
    EXPLORE,
    PROFILE
}

/**
 * TRIBE Bottom Navigation Bar implementing the strict 4-destination rule (Section 9):
 * 1. Home
 * 2. My Plans
 * 3. Explore
 * 4. Profile
 * Plus the prominent elevated center action for "Create New Plan".
 */
@Composable
fun TribeBottomNavigation(
    selectedTab: TribeNavTab,
    onTabSelected: (TribeNavTab) -> Unit,
    onCreatePlanClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "tribe_bottom_navigation"
) {
    val strings = TribeLanguageContext.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                // Navigation items container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab 1: Home
                    TribeNavItem(
                        label = strings.navHome,
                        selected = selectedTab == TribeNavTab.HOME,
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        onClick = { onTabSelected(TribeNavTab.HOME) },
                        modifier = Modifier.weight(1f),
                        testTag = "nav_tab_home"
                    )

                    // Tab 2: My Plans
                    TribeNavItem(
                        label = strings.navMyPlans,
                        selected = selectedTab == TribeNavTab.MY_PLANS,
                        selectedIcon = Icons.Filled.DateRange,
                        unselectedIcon = Icons.Outlined.DateRange,
                        onClick = { onTabSelected(TribeNavTab.MY_PLANS) },
                        modifier = Modifier.weight(1f),
                        testTag = "nav_tab_my_plans"
                    )

                    // Spacer for Center Action Button
                    Box(modifier = Modifier.weight(1.1f))

                    // Tab 3: Explore
                    TribeNavItem(
                        label = strings.navExplore,
                        selected = selectedTab == TribeNavTab.EXPLORE,
                        selectedIcon = Icons.Filled.Explore,
                        unselectedIcon = Icons.Outlined.Explore,
                        onClick = { onTabSelected(TribeNavTab.EXPLORE) },
                        modifier = Modifier.weight(1f),
                        testTag = "nav_tab_explore"
                    )

                    // Tab 4: Profile
                    TribeNavItem(
                        label = strings.navProfile,
                        selected = selectedTab == TribeNavTab.PROFILE,
                        selectedIcon = Icons.Filled.Person,
                        unselectedIcon = Icons.Outlined.Person,
                        onClick = { onTabSelected(TribeNavTab.PROFILE) },
                        modifier = Modifier.weight(1f),
                        testTag = "nav_tab_profile"
                    )
                }

                // Prominent Global Action: Create New Plan
                FloatingActionButton(
                    onClick = onCreatePlanClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .size(52.dp)
                        .align(Alignment.Center)
                        .offset(y = (-10).dp)
                        .testTag("create_new_plan_global_action")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = strings.navCreatePlan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TribeNavItem(
    label: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 1
        )
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedAccentIceDark
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedDivider
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.viewmodel.TeaGramTab

@Composable
fun TeaGramBottomNav(
    selectedTab: TeaGramTab,
    userAvatarResName: String,
    onTabSelected: (TeaGramTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FrostedCanvas.copy(alpha = 0.92f))
    ) {
        Column {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = FrostedDivider
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                BottomNavIconItem(
                    isSelected = selectedTab == TeaGramTab.HOME,
                    onClick = { onTabSelected(TeaGramTab.HOME) },
                    testTag = "nav_home"
                ) {
                    Icon(
                        imageVector = if (selectedTab == TeaGramTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == TeaGramTab.HOME) FrostedAccentIce else FrostedTextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // 2. Search / Explore
                BottomNavIconItem(
                    isSelected = selectedTab == TeaGramTab.EXPLORE,
                    onClick = { onTabSelected(TeaGramTab.EXPLORE) },
                    testTag = "nav_explore"
                ) {
                    Icon(
                        imageVector = if (selectedTab == TeaGramTab.EXPLORE) Icons.Filled.Search else Icons.Outlined.Search,
                        contentDescription = "Explore",
                        tint = if (selectedTab == TeaGramTab.EXPLORE) FrostedAccentIce else FrostedTextMuted,
                        modifier = Modifier.size(25.dp)
                    )
                }

                // 3. Center Frosted Accent Add Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = FrostedAccentIce, spotColor = FrostedAccentIce)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FrostedAccentIce)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(TeaGramTab.CREATE) }
                        )
                        .testTag("nav_create"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        tint = FrostedAccentIceDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 4. Reels
                BottomNavIconItem(
                    isSelected = selectedTab == TeaGramTab.REELS,
                    onClick = { onTabSelected(TeaGramTab.REELS) },
                    testTag = "nav_reels"
                ) {
                    Icon(
                        imageVector = if (selectedTab == TeaGramTab.REELS) Icons.Filled.Movie else Icons.Outlined.Movie,
                        contentDescription = "Reels",
                        tint = if (selectedTab == TeaGramTab.REELS) FrostedAccentIce else FrostedTextMuted,
                        modifier = Modifier.size(25.dp)
                    )
                }

                // 5. Profile
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(TeaGramTab.PROFILE) }
                        )
                        .testTag("nav_profile"),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarBorder = if (selectedTab == TeaGramTab.PROFILE) {
                        Modifier.border(2.dp, FrostedAccentIce, CircleShape)
                    } else {
                        Modifier.border(1.5.dp, FrostedTextMuted.copy(alpha = 0.5f), CircleShape)
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .then(avatarBorder)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(FrostedTextMuted.copy(alpha = 0.2f))
                    ) {
                        TeaGramImage(
                            model = userAvatarResName,
                            contentDescription = "Profile",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavIconItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        content()
        if (isSelected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(FrostedAccentIce)
            )
        }
    }
}


package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostEntity
import com.example.data.model.UserProfile
import com.example.ui.components.TeaGramImage
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedAccentIceDark
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedCardBorderSubtle
import com.example.ui.theme.FrostedCardSolid
import com.example.ui.theme.FrostedDivider
import com.example.ui.theme.FrostedStoryRingBrush
import com.example.ui.theme.FrostedSurfaceVariantSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import java.text.NumberFormat
import java.util.Locale

data class HighlightItem(val id: Long, val title: String, val imageResName: String)

@Composable
fun ProfileScreen(
    profile: UserProfile,
    userPosts: List<PostEntity>,
    savedPosts: List<PostEntity>,
    onPostClick: (PostEntity) -> Unit,
    onNewPostClick: () -> Unit,
    onUpdateProfile: (username: String, displayName: String, bio: String, avatarResName: String, website: String) -> Unit,
    onFollowToggle: (String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    onMessageClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val highlights = remember {
        listOf(
            HighlightItem(1, "Travel ✈️", "img_feed_travel"),
            HighlightItem(2, "Coffee ☕", "img_feed_coffee"),
            HighlightItem(3, "Spaces 🏛️", "img_feed_arch"),
            HighlightItem(4, "Studio 📸", "img_feed_portrait")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FrostedCanvas)
            .statusBarsPadding()
            .testTag("profile_screen")
    ) {
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FrostedTextPrimary
                        )
                    }
                } else if (profile.isCurrentUser) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Private account",
                        tint = FrostedTextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTextPrimary,
                    modifier = Modifier.testTag("profile_username_header")
                )

                if (profile.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = FrostedAccentIce,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (profile.isCurrentUser && onBackClick == null) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Switch accounts",
                        tint = FrostedTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (profile.isCurrentUser) {
                    IconButton(onClick = onNewPostClick) {
                        Icon(
                            imageVector = Icons.Outlined.AddBox,
                            contentDescription = "New post",
                            tint = FrostedTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = FrostedTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = FrostedTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Profile Main Content
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Stats Row + Avatar
            item(key = "profile_stats") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Profile Avatar with Story Ring
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .border(2.5.dp, FrostedStoryRingBrush, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(FrostedSurfaceVariantSolid)
                            .testTag("profile_avatar_view")
                    ) {
                        TeaGramImage(
                            model = profile.avatarResName,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Stats: Posts, Followers, Following
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatItem(
                            count = userPosts.size.toString(),
                            label = "Posts",
                            testTag = "profile_stat_posts"
                        )
                        ProfileStatItem(
                            count = formatProfileCount(profile.followersCount),
                            label = "Followers",
                            testTag = "profile_stat_followers"
                        )
                        ProfileStatItem(
                            count = formatProfileCount(profile.followingCount),
                            label = "Following",
                            testTag = "profile_stat_following"
                        )
                    }
                }
            }

            // Bio info
            item(key = "profile_bio") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FrostedTextPrimary,
                        modifier = Modifier.testTag("profile_display_name")
                    )
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FrostedTextPrimary.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .testTag("profile_bio_text")
                    )
                    if (profile.websiteUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = "Website",
                                tint = FrostedAccentIce,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = profile.websiteUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = FrostedAccentIce,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Action Buttons: Edit Profile for current user, OR Follow/Following + Message for other users
            item(key = "profile_actions") {
                if (profile.isCurrentUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrostedCardSolid)
                                .border(1.dp, FrostedCardBorder, RoundedCornerShape(12.dp))
                                .clickable { showEditProfileDialog = true }
                                .testTag("edit_profile_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Edit profile",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FrostedTextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrostedCardSolid)
                                .border(1.dp, FrostedCardBorder, RoundedCornerShape(12.dp))
                                .clickable {},
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Share profile",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FrostedTextPrimary
                            )
                        }
                    }
                } else {
                    // Other user's profile: Follow/Following and Message buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onFollowToggle(profile.username) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("profile_follow_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (profile.isFollowing) FrostedCardSolid else FrostedAccentIce,
                                contentColor = if (profile.isFollowing) FrostedTextPrimary else FrostedCanvas
                            ),
                            border = if (profile.isFollowing) androidx.compose.foundation.BorderStroke(1.dp, FrostedCardBorder) else null
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (profile.isFollowing) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (profile.isFollowing) "Following" else "Follow",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrostedCardSolid)
                                .border(1.dp, FrostedCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    onMessageClick?.invoke(profile.username)
                                }
                                .testTag("profile_message_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Message",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FrostedTextPrimary
                            )
                        }
                    }
                }
            }

            // Story Highlights Tray
            item(key = "profile_highlights") {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(highlights, key = { it.id }) { hl ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(64.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .border(1.dp, FrostedCardBorder, CircleShape)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(FrostedCardSolid)
                            ) {
                                TeaGramImage(
                                    model = hl.imageResName,
                                    contentDescription = hl.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Text(
                                text = hl.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                color = FrostedTextMuted,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                    }

                    if (profile.isCurrentUser) {
                        // Add New Highlight button
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(64.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .border(1.dp, FrostedCardBorder, CircleShape)
                                        .clip(CircleShape)
                                    .background(FrostedCardSolid.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New highlight",
                                    tint = FrostedTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = FrostedTextMuted,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                    }
                    }
                }
            }

            // Tab Bar: Grid, Reels, Saved, Tagged
            item(key = "profile_tabs") {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = FrostedCanvas,
                    contentColor = FrostedTextPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = FrostedAccentIce,
                            height = 2.dp
                        )
                    },
                    divider = {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = FrostedDivider
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        icon = {
                            Icon(
                                Icons.Default.GridOn,
                                contentDescription = "Posts Grid",
                                tint = if (selectedTabIndex == 0) FrostedAccentIce else FrostedTextMuted
                            )
                        },
                        modifier = Modifier.testTag("tab_profile_grid")
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        icon = {
                            Icon(
                                Icons.Default.Movie,
                                contentDescription = "Reels",
                                tint = if (selectedTabIndex == 1) FrostedAccentIce else FrostedTextMuted
                            )
                        },
                        modifier = Modifier.testTag("tab_profile_reels")
                    )
                    if (profile.isCurrentUser) {
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            icon = {
                                Icon(
                                    Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Saved",
                                    tint = if (selectedTabIndex == 2) FrostedAccentIce else FrostedTextMuted
                                )
                            },
                            modifier = Modifier.testTag("tab_profile_saved")
                        )
                    }
                    Tab(
                        selected = selectedTabIndex == if (profile.isCurrentUser) 3 else 2,
                        onClick = { selectedTabIndex = if (profile.isCurrentUser) 3 else 2 },
                        icon = {
                            Icon(
                                Icons.Default.PersonPin,
                                contentDescription = "Tagged",
                                tint = if (selectedTabIndex == if (profile.isCurrentUser) 3 else 2) FrostedAccentIce else FrostedTextMuted
                            )
                        },
                        modifier = Modifier.testTag("tab_profile_tagged")
                    )
                }
            }

            // Posts Grid Display based on selected tab
            val displayPosts = if (profile.isCurrentUser && selectedTabIndex == 2) {
                savedPosts
            } else {
                userPosts
            }

            if (displayPosts.isEmpty()) {
                item(key = "empty_posts_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (profile.isCurrentUser && selectedTabIndex == 2) "No saved posts yet." else "No posts yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FrostedTextSecondary
                        )
                    }
                }
            } else {
                // Render 3-column rows
                val chunked = displayPosts.chunked(3)
                items(chunked) { rowPosts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (post in rowPosts) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { onPostClick(post) }
                                    .testTag("profile_post_grid_${post.id}")
                            ) {
                                TeaGramImage(
                                    model = post.imageResName,
                                    contentDescription = post.caption,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        // Fill remaining space if last row has fewer than 3 items
                        val remaining = 3 - rowPosts.size
                        for (i in 0 until remaining) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentProfile = profile,
                onDismiss = { showEditProfileDialog = false },
                onSave = { newUsername, name, bio, avatarResName, site ->
                    onUpdateProfile(newUsername, name, bio, avatarResName, site)
                    showEditProfileDialog = false
                }
            )
        }
    }
}

@Composable
private fun ProfileStatItem(
    count: String,
    label: String,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FrostedTextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = FrostedTextSecondary,
            fontSize = 12.sp
        )
    }
}

private fun formatProfileCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 10_000 -> String.format(Locale.US, "%.1fK", count / 1000.0)
        count >= 1_000 -> NumberFormat.getNumberInstance(Locale.US).format(count)
        else -> count.toString()
    }
}

@Composable
private fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (username: String, name: String, bio: String, avatarResName: String, website: String) -> Unit
) {
    var username by remember { mutableStateOf(currentProfile.username) }
    var name by remember { mutableStateOf(currentProfile.fullName) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var website by remember { mutableStateOf(currentProfile.websiteUrl) }
    var selectedAvatar by remember { mutableStateOf(currentProfile.avatarResName) }

    val availableAvatars = remember {
        listOf(
            "img_feed_portrait" to "Portrait",
            "img_feed_travel" to "Travel",
            "img_feed_arch" to "Arch",
            "img_feed_coffee" to "Coffee"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrostedCardSolid,
        title = {
            Text("Edit profile", fontWeight = FontWeight.Bold, color = FrostedTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Profile Picture Picker Section
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Change profile picture",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FrostedAccentIce
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for ((avatarRes, label) in availableAvatars) {
                            val isSelected = selectedAvatar == avatarRes
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) FrostedAccentIce else FrostedCardBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedAvatar = avatarRes }
                                    .testTag("avatar_option_$avatarRes"),
                                contentAlignment = Alignment.Center
                            ) {
                                TeaGramImage(
                                    model = avatarRes,
                                    contentDescription = label,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(FrostedCanvas.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = FrostedAccentIce,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = FrostedDivider)

                // Username TextField
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = FrostedTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_username"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary,
                        focusedBorderColor = FrostedAccentIce,
                        unfocusedBorderColor = FrostedCardBorder
                    )
                )

                // Display Name TextField
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name", color = FrostedTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary,
                        focusedBorderColor = FrostedAccentIce,
                        unfocusedBorderColor = FrostedCardBorder
                    )
                )

                // Short Bio TextField
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio", color = FrostedTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_bio"),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary,
                        focusedBorderColor = FrostedAccentIce,
                        unfocusedBorderColor = FrostedCardBorder
                    )
                )

                // Website TextField
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website", color = FrostedTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_website"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary,
                        focusedBorderColor = FrostedAccentIce,
                        unfocusedBorderColor = FrostedCardBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(username, name, bio, selectedAvatar, website) },
                colors = ButtonDefaults.buttonColors(containerColor = FrostedAccentIce),
                modifier = Modifier.testTag("edit_profile_save_btn")
            ) {
                Text("Save", color = FrostedAccentIceDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FrostedTextSecondary)
            }
        }
    )
}

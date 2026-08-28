package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostEntity
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedCardBackground
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedCardBorderSubtle
import com.example.ui.theme.FrostedStoryRingBrush
import com.example.ui.theme.FrostedSurfaceVariantSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.InstaHeartRed
import com.example.ui.theme.InstaLinkBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PostCard(
    post: PostEntity,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFollowing: Boolean = true,
    isCurrentUser: Boolean = false,
    onFollowToggle: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var showBigHeart by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isExpandedCaption by remember { mutableStateOf(false) }

    val heartScale = remember { Animatable(1f) }

    // Frosted Glass Card Container (backdrop-blur aesthetic with rounded-[32dp] & subtle border)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(FrostedCardBackground)
            .border(1.dp, FrostedCardBorder, RoundedCornerShape(32.dp))
            .padding(bottom = 16.dp)
            .testTag("post_card_${post.id}")
    ) {
        // 1. Post Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clickable(onClick = onAuthorClick)
                    .testTag("post_author_${post.authorUsername}")
            ) {
                // Author Avatar with gradient ring
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.5.dp, FrostedStoryRingBrush, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(FrostedSurfaceVariantSolid)
                ) {
                    TeaGramImage(
                        model = post.authorAvatarUrl,
                        contentDescription = post.authorUsername,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorUsername,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = FrostedTextPrimary
                        )
                        if (post.authorIsVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified",
                                tint = FrostedAccentIce,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (!isCurrentUser && !isFollowing && onFollowToggle != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                color = FrostedTextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Follow",
                                color = FrostedAccentIce,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable(onClick = onFollowToggle)
                                    .padding(vertical = 2.dp)
                                    .testTag("post_follow_btn_${post.id}")
                            )
                        }
                    }
                    if (post.location != null) {
                        Text(
                            text = post.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = FrostedTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Post options",
                        tint = FrostedTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(FrostedSurfaceVariantSolid)
                ) {
                    if (!isCurrentUser && onFollowToggle != null) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isFollowing) "Unfollow @${post.authorUsername}" else "Follow @${post.authorUsername}",
                                    color = if (isFollowing) InstaHeartRed else FrostedAccentIce
                                )
                            },
                            onClick = {
                                showMenu = false
                                onFollowToggle()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("View profile", color = FrostedTextPrimary) },
                        onClick = {
                            showMenu = false
                            onAuthorClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share to...", color = FrostedTextPrimary) },
                        onClick = {
                            showMenu = false
                            onShareClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (post.isSaved) "Remove from saved" else "Save post", color = FrostedTextPrimary) },
                        onClick = {
                            showMenu = false
                            onSaveClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy link", color = FrostedTextPrimary) },
                        onClick = { showMenu = false }
                    )
                }
            }
        }

        // 2. Post Media with Double-Tap to Like & Frosted Rounded Inner Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(0.5.dp, FrostedCardBorderSubtle, RoundedCornerShape(24.dp))
                .aspectRatio(1f)
                .background(Color.Black)
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (!post.isLiked) {
                                onLikeClick()
                            }
                            coroutineScope.launch {
                                showBigHeart = true
                                delay(900)
                                showBigHeart = false
                            }
                        }
                    )
                }
        ) {
            TeaGramImage(
                model = post.imageResName,
                contentDescription = "Post by ${post.authorUsername}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Frosted Location Tag Overlay Pill
            if (post.location != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.location,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Animated Big Heart Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = showBigHeart,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut(tween(300)) + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(96.dp)
                )
            }
        }

        // 3. Action Buttons Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Like Button
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            heartScale.animateTo(1.3f, tween(100))
                            heartScale.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        onLikeClick()
                    },
                    modifier = Modifier.testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (post.isLiked) "Unlike" else "Like",
                        tint = if (post.isLiked) InstaHeartRed else FrostedTextPrimary,
                        modifier = Modifier
                            .size(26.dp)
                            .scale(heartScale.value)
                    )
                }

                // Comment Button
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = FrostedTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Share Button
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("share_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Share",
                        tint = FrostedTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Save / Bookmark Button
            IconButton(
                onClick = onSaveClick,
                modifier = Modifier.testTag("save_button_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (post.isSaved) "Unsave" else "Save",
                    tint = if (post.isSaved) FrostedAccentIce else FrostedTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 4. Likes count
        val formattedLikes = remember(post.likesCount) {
            NumberFormat.getNumberInstance(Locale.US).format(post.likesCount)
        }
        Text(
            text = "$formattedLikes likes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = FrostedTextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 5. Caption with Username
        val annotatedCaption = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = FrostedTextPrimary)) {
                append("${post.authorUsername} ")
            }
            withStyle(style = SpanStyle(color = FrostedTextPrimary)) {
                append(post.caption)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)) {
            Text(
                text = annotatedCaption,
                style = MaterialTheme.typography.bodyMedium,
                color = FrostedTextPrimary,
                fontSize = 13.sp,
                maxLines = if (isExpandedCaption) Int.MAX_VALUE else 2,
                modifier = Modifier.clickable {
                    isExpandedCaption = !isExpandedCaption
                }
            )
            if (!isExpandedCaption && post.caption.length > 70) {
                Text(
                    text = "more",
                    style = MaterialTheme.typography.bodySmall,
                    color = FrostedTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { isExpandedCaption = true }
                        .padding(top = 1.dp)
                )
            }
        }

        // 6. Comments Link
        if (post.commentsCount > 0) {
            Text(
                text = "View all ${post.commentsCount} comments",
                style = MaterialTheme.typography.bodySmall,
                color = FrostedTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clickable(onClick = onCommentClick)
                    .testTag("view_comments_button_${post.id}")
            )
        }

        // 7. Time elapsed
        val timeAgo = remember(post.timestamp) {
            val diffMs = System.currentTimeMillis() - post.timestamp
            val mins = diffMs / (1000 * 60)
            val hours = mins / 60
            val days = hours / 24
            when {
                mins < 1 -> "Just now"
                mins < 60 -> "$mins minutes ago"
                hours < 24 -> "$hours hours ago"
                else -> "$days days ago"
            }
        }
        Text(
            text = timeAgo.uppercase(Locale.US),
            style = MaterialTheme.typography.labelSmall,
            color = FrostedTextSecondary,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)
        )
    }
}


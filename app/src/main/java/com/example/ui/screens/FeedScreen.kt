package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostEntity
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import com.example.ui.components.TeaGramImage
import com.example.ui.components.PostCard
import com.example.ui.components.StoriesRow
import com.example.ui.theme.FrostedAccentIce
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedCardBackground
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.FrostedDivider
import com.example.ui.theme.FrostedSurfaceVariantSolid
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary

@Composable
fun FeedScreen(
    stories: List<StoryEntity>,
    posts: List<PostEntity>,
    suggestedUsers: List<UserEntity>,
    followingUsernames: Set<String>,
    currentUsername: String,
    isLoadingMore: Boolean,
    isInitialLoading: Boolean,
    errorMessage: String?,
    hasMorePosts: Boolean,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onStoryClick: (StoryEntity) -> Unit,
    onAddStoryClick: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    onShareClick: (PostEntity) -> Unit,
    onSaveClick: (Long) -> Unit,
    onAuthorClick: (String) -> Unit,
    onFollowToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Detect when user scrolls near the bottom to paginate more posts
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value, hasMorePosts, isLoadingMore) {
        if (shouldLoadMore.value && hasMorePosts && !isLoadingMore) {
            onLoadMore()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FrostedCanvas)
    ) {
        if (isInitialLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = FrostedAccentIce,
                strokeWidth = 3.dp
            )
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Something went wrong",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FrostedTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrostedAccentIce,
                        contentColor = FrostedCanvas
                    )
                ) {
                    Text("Retry")
                }
            }
        } else {
            LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("feed_lazy_column")
        ) {
            // Stories header
            item(key = "stories_header") {
                StoriesRow(
                    stories = stories,
                    onStoryClick = onStoryClick,
                    onAddStoryClick = onAddStoryClick
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = FrostedDivider
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // If user has no feed posts (e.g. unfollowed everyone and has no posts)
            if (posts.isEmpty()) {
                item(key = "empty_feed_banner") {
                    EmptyFeedSuggestedSection(
                        suggestedUsers = suggestedUsers,
                        onFollowClick = onFollowToggle,
                        onAuthorClick = onAuthorClick
                    )
                }
            } else {
                // Feed Posts from followed accounts
                items(posts, key = { it.id }) { post ->
                    val isFollowed = followingUsernames.contains(post.authorUsername)
                    val isSelf = post.authorUsername == currentUsername

                    PostCard(
                        post = post,
                        isFollowing = isFollowed,
                        isCurrentUser = isSelf,
                        onFollowToggle = { onFollowToggle(post.authorUsername) },
                        onLikeClick = { onLikeClick(post.id) },
                        onCommentClick = { onCommentClick(post.id) },
                        onShareClick = { onShareClick(post) },
                        onSaveClick = { onSaveClick(post.id) },
                        onAuthorClick = { onAuthorClick(post.authorUsername) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Inline Suggested Creators Tray if there are creators to follow
                if (suggestedUsers.isNotEmpty() && posts.size >= 2) {
                    item(key = "suggested_creators_tray") {
                        SuggestedCreatorsCard(
                            suggestedUsers = suggestedUsers,
                            onFollowClick = onFollowToggle,
                            onAuthorClick = onAuthorClick
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Loading more indicator at the bottom
                if (isLoadingMore) {
                    item(key = "feed_loading_more") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                                .testTag("feed_loading_indicator"),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = FrostedAccentIce,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading older posts...",
                                color = FrostedTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (!hasMorePosts && posts.isNotEmpty()) {
                    // "You're all caught up" footer
                    item(key = "all_caught_up") {
                        AllCaughtUpCard()
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
        }
    }
}

@Composable
fun SuggestedCreatorsCard(
    suggestedUsers: List<UserEntity>,
    onFollowClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(FrostedCardBackground)
            .border(1.dp, FrostedCardBorder, RoundedCornerShape(28.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Suggested for you",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FrostedTextPrimary
            )
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = FrostedAccentIce
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(suggestedUsers, key = { it.username }) { user ->
                CreatorSuggestionItem(
                    user = user,
                    onFollowClick = { onFollowClick(user.username) },
                    onProfileClick = { onAuthorClick(user.username) }
                )
            }
        }
    }
}

@Composable
fun CreatorSuggestionItem(
    user: UserEntity,
    onFollowClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(FrostedSurfaceVariantSolid)
            .border(1.dp, FrostedCardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onProfileClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(FrostedCanvas)
        ) {
            TeaGramImage(
                model = user.avatarResName,
                contentDescription = user.displayName,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = FrostedTextPrimary,
                maxLines = 1
            )
            if (user.isVerified) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Verified",
                    tint = FrostedAccentIce,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Text(
            text = user.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = FrostedTextSecondary,
            maxLines = 1,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onFollowClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .testTag("follow_suggested_${user.username}"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FrostedAccentIce,
                contentColor = FrostedCanvas
            )
        ) {
            Text(
                text = "Follow",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyFeedSuggestedSection(
    suggestedUsers: List<UserEntity>,
    onFollowClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(FrostedCardBackground)
                .border(1.dp, FrostedCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.GroupAdd,
                contentDescription = null,
                tint = FrostedAccentIce,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to your Feed",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = FrostedTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Follow other creators to see their latest photos, architectural studies, and travel stories here in your feed.",
            style = MaterialTheme.typography.bodyMedium,
            color = FrostedTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (suggestedUsers.isNotEmpty()) {
            SuggestedCreatorsCard(
                suggestedUsers = suggestedUsers,
                onFollowClick = onFollowClick,
                onAuthorClick = onAuthorClick
            )
        }
    }
}

@Composable
fun AllCaughtUpCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(FrostedCardBackground)
                .border(1.dp, FrostedCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Caught up",
                tint = FrostedAccentIce,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You're all caught up",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FrostedTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "You've seen all new posts from accounts you follow.",
            style = MaterialTheme.typography.bodySmall,
            color = FrostedTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

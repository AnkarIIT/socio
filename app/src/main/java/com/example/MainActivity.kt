package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.TeaGramBottomNav
import com.example.ui.components.TeaGramTopBar
import com.example.ui.components.ShareSheet
import com.example.ui.components.StoryViewerDialog
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.DirectMessagesScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TeaGramTab
import com.example.ui.viewmodel.TeaGramViewModel
import com.example.data.model.PostEntity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TeaGramApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeaGramApp(
    viewModel: TeaGramViewModel = viewModel()
) {
    val context = LocalContext.current

    val allPosts by viewModel.posts.collectAsStateWithLifecycle()
    val feedPosts by viewModel.feedPosts.collectAsStateWithLifecycle()
    val totalFeedPostsCount by viewModel.totalFeedPostsCount.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val isInitialLoading by viewModel.isInitialLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val followingUsernames by viewModel.followingUsernames.collectAsStateWithLifecycle()
    val currentUserUsername by viewModel.currentUserUsername.collectAsStateWithLifecycle()
    val suggestedUsers by viewModel.suggestedUsers.collectAsStateWithLifecycle()

    val savedPosts by viewModel.savedPosts.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentProfilePosts by viewModel.currentProfilePosts.collectAsStateWithLifecycle()
    val viewedProfileUsername by viewModel.viewedProfileUsername.collectAsStateWithLifecycle()

    val reels by viewModel.reels.collectAsStateWithLifecycle()
    val chatThreads by viewModel.chatThreads.collectAsStateWithLifecycle()

    val selectedStory by viewModel.selectedStory.collectAsStateWithLifecycle()
    val activeCommentsPostId by viewModel.activeCommentsPostId.collectAsStateWithLifecycle()
    val activeComments by viewModel.activeComments.collectAsStateWithLifecycle()
    val isDirectMessagesOpen by viewModel.isDirectMessagesOpen.collectAsStateWithLifecycle()
    val currentConversationId by viewModel.currentConversationId.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val sharePost by viewModel.sharePost.collectAsStateWithLifecycle()
    val selectedPostDetail by viewModel.selectedPostDetail.collectAsStateWithLifecycle()
    val selectedReelId by viewModel.selectedReelId.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedExploreCategory by viewModel.selectedExploreCategory.collectAsStateWithLifecycle()

    // Back handling hierarchy
    BackHandler(enabled = currentConversationId != null) {
        viewModel.closeConversation()
    }
    BackHandler(enabled = currentConversationId == null && isDirectMessagesOpen) {
        viewModel.closeDirectMessages()
    }
    BackHandler(enabled = !isDirectMessagesOpen && (selectedPostDetail != null || selectedReelId != null)) {
        val reelId = selectedReelId
        when {
            selectedPostDetail != null -> viewModel.closePostDetail()
            reelId != null -> viewModel.closeReelComments()
        }
    }
    BackHandler(enabled = !isDirectMessagesOpen && selectedPostDetail == null && viewedProfileUsername != null) {
        viewModel.closeUserProfile()
    }
    BackHandler(enabled = !isDirectMessagesOpen && selectedPostDetail == null && viewedProfileUsername == null && selectedTab != TeaGramTab.HOME) {
        viewModel.selectTab(TeaGramTab.HOME)
    }

    // Modal / Fullscreen overlays
    // 1. Story Viewer
    if (selectedStory != null) {
        StoryViewerDialog(
            story = selectedStory!!,
            onDismiss = { viewModel.closeStory() },
            onNextStory = { viewModel.nextStory() },
            onPrevStory = { viewModel.previousStory() },
            onReply = { reply ->
                Toast.makeText(context, "Replied to story: $reply", Toast.LENGTH_SHORT).show()
                viewModel.closeStory()
            }
        )
    }

    // 2. Comments Bottom Sheet
    if (activeCommentsPostId != null || selectedReelId != null) {
        val commentsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        CommentsBottomSheet(
            comments = activeComments,
            userAvatar = userProfile.avatarResName,
            onDismiss = {
                selectedReelId?.let { viewModel.closeReelComments() } ?: viewModel.closeComments()
            },
            onAddComment = { text ->
                selectedReelId?.let { viewModel.addReelComment(it, text) } ?: viewModel.addComment(text)
            },
            onToggleLike = { comment ->
                selectedReelId?.let { viewModel.toggleReelCommentLike(comment) } ?: viewModel.toggleCommentLike(comment)
            },
            sheetState = commentsSheetState
        )
    }

    // 3. Share Bottom Sheet
    if (sharePost != null) {
        ShareSheet(
            post = sharePost!!,
            friends = chatThreads,
            onDismiss = { viewModel.closeShareModal() },
            onSendToFriend = { friend ->
                Toast.makeText(context, "Sent to $friend", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 4. Conversation / Chat Detail Screen
    if (currentConversationId != null) {
        val partner = chatThreads.firstOrNull { it.conversationId == currentConversationId }
        val partnerName = partner?.partnerFullName ?: currentConversationId!!
        val partnerAvatar = partner?.partnerAvatarResName ?: "img_feed_portrait"

        ChatDetailScreen(
            partnerUsername = partnerName,
            partnerAvatar = partnerAvatar,
            messages = activeMessages,
            onBackClick = { viewModel.closeConversation() },
            onSendMessage = { text -> viewModel.sendMessage(text) }
        )
        return
    }

    // 5. Direct Messages Screen
    if (isDirectMessagesOpen) {
        DirectMessagesScreen(
            currentUsername = userProfile.username,
            threads = chatThreads,
            onBackClick = { viewModel.closeDirectMessages() },
            onThreadClick = { thread -> viewModel.openConversation(thread.conversationId) }
        )
        return
    }

    // 6. Post Detail Screen
    if (selectedPostDetail != null) {
        PostDetailScreen(
            post = selectedPostDetail!!,
            onBackClick = { viewModel.closePostDetail() },
            onLikeClick = { viewModel.toggleLike(selectedPostDetail!!.id) },
            onCommentClick = { viewModel.openComments(selectedPostDetail!!.id) },
            onShareClick = { viewModel.openShareModal(selectedPostDetail!!) },
            onSaveClick = { viewModel.toggleSave(selectedPostDetail!!.id) },
            onAuthorClick = {
                val author = selectedPostDetail!!.authorUsername
                viewModel.closePostDetail()
                viewModel.openUserProfile(author)
            }
        )
        return
    }

    // 7. Viewing Another User's Profile (Pushed on stack)
    if (viewedProfileUsername != null) {
        ProfileScreen(
            profile = userProfile,
            userPosts = currentProfilePosts,
            savedPosts = emptyList(),
            onPostClick = { post -> viewModel.openPostDetail(post) },
            onNewPostClick = { viewModel.selectTab(TeaGramTab.CREATE) },
            onUpdateProfile = { username, name, bio, avatar, site ->
                viewModel.updateProfile(username, name, bio, avatar, site)
            },
            onFollowToggle = { target ->
                viewModel.toggleFollow(target)
            },
            onBackClick = {
                viewModel.closeUserProfile()
            },
            onMessageClick = { target ->
                viewModel.openConversation(target)
            }
        )
        return
    }

    // Main App Shell with Top Bar & Bottom Navigation
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FrostedCanvas,
        topBar = {
            if (selectedTab == TeaGramTab.HOME) {
                TeaGramTopBar(
                    onNewPostClick = { viewModel.selectTab(TeaGramTab.CREATE) },
                    onDirectMessagesClick = { viewModel.openDirectMessages() },
                    onNotificationsClick = {
                        Toast.makeText(context, "You are all caught up!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        },
        bottomBar = {
            if (selectedTab != TeaGramTab.CREATE) {
                TeaGramBottomNav(
                    selectedTab = selectedTab,
                    userAvatarResName = userProfile.avatarResName,
                    onTabSelected = { tab -> viewModel.selectTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FrostedCanvas)
        ) {
            when (selectedTab) {
                TeaGramTab.HOME -> {
                    FeedScreen(
                        stories = stories,
                        posts = feedPosts,
                        suggestedUsers = suggestedUsers,
                        followingUsernames = followingUsernames,
                        currentUsername = currentUserUsername,
                        isLoadingMore = isLoadingMore,
                        isInitialLoading = isInitialLoading,
                        errorMessage = errorMessage,
                        hasMorePosts = feedPosts.size < totalFeedPostsCount,
                        onLoadMore = { viewModel.loadMorePosts() },
                        onRetry = {
                            viewModel.clearError()
                            viewModel.refreshFeed()
                        },
                        onStoryClick = { story -> viewModel.openStory(story) },
                        onAddStoryClick = { viewModel.selectTab(TeaGramTab.CREATE) },
                        onLikeClick = { postId -> viewModel.toggleLike(postId) },
                        onCommentClick = { postId -> viewModel.openComments(postId) },
                        onShareClick = { post -> viewModel.openShareModal(post) },
                        onSaveClick = { postId -> viewModel.toggleSave(postId) },
                        onAuthorClick = { username ->
                            viewModel.openUserProfile(username)
                        },
                        onFollowToggle = { target ->
                            viewModel.toggleFollow(target)
                        }
                    )
                }

                TeaGramTab.EXPLORE -> {
                    ExploreScreen(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
                        selectedCategory = selectedExploreCategory,
                        onSelectCategory = { c -> viewModel.setExploreCategory(c) },
                        posts = allPosts,
                        onPostClick = { post -> viewModel.openPostDetail(post) }
                    )
                }

                TeaGramTab.CREATE -> {
                    CreatePostScreen(
                        onDismiss = { viewModel.selectTab(TeaGramTab.HOME) },
                        onSharePost = { imageRes, caption, location ->
                            viewModel.createPost(imageRes, caption, location)
                            Toast.makeText(context, "Post shared to your feed!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                TeaGramTab.REELS -> {
                    val currentReel = if (selectedReelId != null) reels.firstOrNull { it.id == selectedReelId } else null
                    ReelsScreen(
                        reels = reels,
                        onToggleLike = { reelId -> viewModel.toggleReelLike(reelId) },
                        onCommentClick = { reelId ->
                            viewModel.openReelComments(reelId)
                        },
                        onShareClick = { reel ->
                            viewModel.openShareModal(
                                PostEntity(
                                    id = reel.id,
                                    authorUsername = reel.authorUsername,
                                    authorAvatarUrl = reel.authorAvatarResName,
                                    authorIsVerified = false,
                                    imageResName = reel.imageResName,
                                    caption = reel.caption,
                                    location = null,
                                    likesCount = reel.likesCount,
                                    isLiked = reel.isLiked,
                                    isSaved = false,
                                    commentsCount = 0,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    )
                }

                TeaGramTab.PROFILE -> {
                    ProfileScreen(
                        profile = userProfile,
                        userPosts = currentProfilePosts,
                        savedPosts = savedPosts,
                        onPostClick = { post -> viewModel.openPostDetail(post) },
                        onNewPostClick = { viewModel.selectTab(TeaGramTab.CREATE) },
                        onUpdateProfile = { username, name, bio, avatar, site ->
                            viewModel.updateProfile(username, name, bio, avatar, site)
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        },
                        onFollowToggle = { target ->
                            viewModel.toggleFollow(target)
                        }
                    )
                }
            }
        }
    }
}

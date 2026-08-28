package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ChatThread
import com.example.data.model.CommentEntity
import com.example.data.model.DirectMessageEntity
import com.example.data.model.PostEntity
import com.example.data.model.ReelItem
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserProfile
import com.example.data.repository.TeaGramRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TeaGramTab {
    HOME,
    EXPLORE,
    CREATE,
    REELS,
    PROFILE
}

@OptIn(ExperimentalCoroutinesApi::class)
class TeaGramViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TeaGramRepository

    val posts: StateFlow<List<PostEntity>>
    val savedPosts: StateFlow<List<PostEntity>>
    val stories: StateFlow<List<StoryEntity>>

    private val _currentUserUsername = MutableStateFlow("sarah.creatives")
    val currentUserUsername: StateFlow<String> = _currentUserUsername.asStateFlow()

    private val _selectedTab = MutableStateFlow(TeaGramTab.HOME)
    val selectedTab: StateFlow<TeaGramTab> = _selectedTab.asStateFlow()

    private val _viewedProfileUsername = MutableStateFlow<String?>(null)
    val viewedProfileUsername: StateFlow<String?> = _viewedProfileUsername.asStateFlow()

    // Pagination limit for main feed
    private val _feedLimit = MutableStateFlow(4)
    val feedLimit: StateFlow<Int> = _feedLimit.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Following tracking
    val followingUsernames: StateFlow<Set<String>>
    val followingCount: StateFlow<Int>

    // Feed posts from followed users + self (paged)
    val feedPosts: StateFlow<List<PostEntity>>
    val totalFeedPostsCount: StateFlow<Int>

    // Profile & profile posts
    val userProfile: StateFlow<UserProfile>
    val currentProfilePosts: StateFlow<List<PostEntity>>

    // Suggested users to follow
    val suggestedUsers: StateFlow<List<UserEntity>>

    private val _selectedStory = MutableStateFlow<StoryEntity?>(null)
    val selectedStory: StateFlow<StoryEntity?> = _selectedStory.asStateFlow()

    private val _activeCommentsPostId = MutableStateFlow<Long?>(null)
    val activeCommentsPostId: StateFlow<Long?> = _activeCommentsPostId.asStateFlow()

    private val _activeComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val activeComments: StateFlow<List<CommentEntity>> = _activeComments.asStateFlow()

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<DirectMessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<DirectMessageEntity>> = _activeMessages.asStateFlow()

    private val _chatThreads = MutableStateFlow<List<ChatThread>>(emptyList())
    val chatThreads: StateFlow<List<ChatThread>> = _chatThreads.asStateFlow()

    private val _reels = MutableStateFlow<List<ReelItem>>(emptyList())
    val reels: StateFlow<List<ReelItem>> = _reels.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedExploreCategory = MutableStateFlow("For you")
    val selectedExploreCategory: StateFlow<String> = _selectedExploreCategory.asStateFlow()

    private val _isDirectMessagesOpen = MutableStateFlow(false)
    val isDirectMessagesOpen: StateFlow<Boolean> = _isDirectMessagesOpen.asStateFlow()

    private val _sharePost = MutableStateFlow<PostEntity?>(null)
    val sharePost: StateFlow<PostEntity?> = _sharePost.asStateFlow()

    private val _selectedPostDetail = MutableStateFlow<PostEntity?>(null)
    val selectedPostDetail: StateFlow<PostEntity?> = _selectedPostDetail.asStateFlow()

    private val _selectedReelId = MutableStateFlow<Long?>(null)
    val selectedReelId: StateFlow<Long?> = _selectedReelId.asStateFlow()

    private val _reelComments = MutableStateFlow<Map<Long, List<CommentEntity>>>(emptyMap())
    val reelComments: StateFlow<Map<Long, List<CommentEntity>>> = _reelComments.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = TeaGramRepository(db, application)

        posts = repository.allPosts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedPosts = repository.savedPosts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        stories = repository.allStories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        followingUsernames = _currentUserUsername.flatMapLatest { username ->
            repository.getFollowingUsernames(username).map { it.toSet() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

        followingCount = _currentUserUsername.flatMapLatest { username ->
            repository.getFollowingCount(username)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2
        )

        feedPosts = combine(_currentUserUsername, _feedLimit) { username, limit ->
            Pair(username, limit)
        }.flatMapLatest { (username, limit) ->
            repository.getFeedPosts(username, limit)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        totalFeedPostsCount = _currentUserUsername.flatMapLatest { username ->
            repository.getFeedPostsCount(username)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        suggestedUsers = combine(
            repository.allUsers,
            followingUsernames,
            _currentUserUsername
        ) { users, following, currentUsername ->
            users.filter { it.username != currentUsername && !following.contains(it.username) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userProfile = combine(
            _viewedProfileUsername,
            repository.currentUser,
            repository.allUsers,
            posts,
            followingUsernames
        ) { viewedName, currentUserEntity, allUsersList, allPostsList, followingSet ->
            val isSelf = viewedName == null || viewedName == currentUserEntity?.username
            val targetUsername = if (isSelf) (currentUserEntity?.username ?: "sarah.creatives") else viewedName

            val targetUser = if (isSelf) {
                currentUserEntity ?: allUsersList.find { it.username == targetUsername }
            } else {
                allUsersList.find { it.username == targetUsername }
            }

            val isFollowing = followingSet.contains(targetUsername)
            val userPostsCount = allPostsList.count { it.authorUsername == targetUsername }

            val followers = if (isSelf) {
                targetUser?.baseFollowersCount ?: 18420
            } else {
                (targetUser?.baseFollowersCount ?: 1200) + (if (isFollowing) 1 else 0)
            }

            val following = if (isSelf) {
                followingSet.size
            } else {
                targetUser?.baseFollowingCount ?: 250
            }

            UserProfile(
                username = targetUsername,
                fullName = targetUser?.displayName ?: targetUsername,
                bio = targetUser?.bio ?: "Visual creator",
                websiteUrl = targetUser?.websiteUrl ?: "",
                avatarResName = targetUser?.avatarResName ?: "img_feed_portrait",
                postsCount = userPostsCount,
                followersCount = followers,
                followingCount = following,
                isCurrentUser = isSelf,
                isFollowing = isFollowing,
                isVerified = targetUser?.isVerified ?: false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

        currentProfilePosts = combine(
            _viewedProfileUsername,
            _currentUserUsername,
            posts
        ) { viewedName, currentName, allPostsList ->
            val target = viewedName ?: currentName
            allPostsList.filter { it.authorUsername == target }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            try {
                repository.seedInitialDataIfNeeded()
                _chatThreads.value = repository.getChatThreads()
                _reels.value = repository.getReels()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to load data"
            } finally {
                _isInitialLoading.value = false
            }
        }
    }

    fun selectTab(tab: TeaGramTab) {
        _selectedTab.value = tab
        if (tab == TeaGramTab.PROFILE) {
            _viewedProfileUsername.value = null
        }
    }

    fun openUserProfile(username: String) {
        if (username == _currentUserUsername.value) {
            _viewedProfileUsername.value = null
            _selectedTab.value = TeaGramTab.PROFILE
        } else {
            _viewedProfileUsername.value = username
        }
    }

    fun closeUserProfile() {
        _viewedProfileUsername.value = null
    }

    fun toggleFollow(targetUsername: String) {
        viewModelScope.launch {
            repository.toggleFollow(_currentUserUsername.value, targetUsername)
        }
    }

    fun loadMorePosts() {
        if (_isLoadingMore.value) return
        if (feedPosts.value.size >= totalFeedPostsCount.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            delay(500)
            _feedLimit.value = _feedLimit.value + 4
            _isLoadingMore.value = false
        }
    }

    fun refreshFeed() {
        _feedLimit.value = 4
    }

    fun toggleLike(postId: Long) {
        viewModelScope.launch {
            repository.toggleLike(postId)
            if (_selectedPostDetail.value?.id == postId) {
                _selectedPostDetail.value = _selectedPostDetail.value?.let {
                    val newLiked = !it.isLiked
                    val newCount = if (newLiked) it.likesCount + 1 else maxOf(0, it.likesCount - 1)
                    it.copy(isLiked = newLiked, likesCount = newCount)
                }
            }
        }
    }

    fun toggleSave(postId: Long) {
        viewModelScope.launch {
            repository.toggleSave(postId)
            if (_selectedPostDetail.value?.id == postId) {
                _selectedPostDetail.value = _selectedPostDetail.value?.let {
                    it.copy(isSaved = !it.isSaved)
                }
            }
        }
    }

    fun openComments(postId: Long) {
        _activeCommentsPostId.value = postId
        viewModelScope.launch {
            repository.getCommentsForPost(postId).collect { comments ->
                _activeComments.value = comments
            }
        }
    }

    fun closeComments() {
        _activeCommentsPostId.value = null
        _activeComments.value = emptyList()
    }

    fun addComment(text: String) {
        val postId = _activeCommentsPostId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(postId, text.trim(), _currentUserUsername.value)
        }
    }

    fun toggleCommentLike(comment: CommentEntity) {
        viewModelScope.launch {
            repository.toggleCommentLike(comment.id, comment.isLiked, comment.likesCount)
        }
    }

    fun openReelComments(reelId: Long) {
        _selectedReelId.value = reelId
        _activeCommentsPostId.value = null
        val comments = _reelComments.value[reelId] ?: emptyList()
        _activeComments.value = comments
    }

    fun addReelComment(reelId: Long, text: String) {
        if (text.isBlank()) return
        val comment = CommentEntity(
            postId = -reelId,
            authorUsername = _currentUserUsername.value,
            authorAvatarUrl = "img_feed_portrait",
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        _reelComments.value = _reelComments.value.toMutableMap().apply {
            put(reelId, (get(reelId) ?: emptyList()) + comment)
        }
        _activeComments.value = _reelComments.value[reelId] ?: emptyList()
    }

    fun toggleReelCommentLike(comment: CommentEntity) {
        val reelId = _selectedReelId.value ?: return
        val current = _reelComments.value[reelId] ?: return
        val updated = current.map {
            if (it.id == comment.id) {
                val newLiked = !it.isLiked
                val newCount = if (newLiked) it.likesCount + 1 else maxOf(0, it.likesCount - 1)
                it.copy(isLiked = newLiked, likesCount = newCount)
            } else it
        }
        _reelComments.value = _reelComments.value.toMutableMap().apply {
            put(reelId, updated)
        }
        _activeComments.value = updated
    }

    fun closeReelComments() {
        _selectedReelId.value = null
        _activeComments.value = emptyList()
    }

    fun openStory(story: StoryEntity) {
        _selectedStory.value = story
        viewModelScope.launch {
            repository.markStorySeen(story.id)
        }
    }

    fun closeStory() {
        _selectedStory.value = null
    }

    fun nextStory() {
        val current = _selectedStory.value ?: return
        val list = stories.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex in 0 until list.size - 1) {
            openStory(list[currentIndex + 1])
        } else {
            closeStory()
        }
    }

    fun previousStory() {
        val current = _selectedStory.value ?: return
        val list = stories.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            openStory(list[currentIndex - 1])
        }
    }

    fun openDirectMessages() {
        _isDirectMessagesOpen.value = true
    }

    fun closeDirectMessages() {
        _isDirectMessagesOpen.value = false
        _currentConversationId.value = null
    }

    fun openConversation(conversationId: String) {
        _currentConversationId.value = conversationId
        viewModelScope.launch {
            repository.getMessagesForConversation(conversationId).collect { msgs ->
                _activeMessages.value = msgs
            }
        }
    }

    fun closeConversation() {
        _currentConversationId.value = null
        _activeMessages.value = emptyList()
    }

    fun sendMessage(text: String) {
        val convId = _currentConversationId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(convId, text.trim())
        }
    }

    fun createPost(imageResName: String, caption: String, location: String?) {
        viewModelScope.launch {
            repository.createPost(imageResName, caption, location, _currentUserUsername.value)
            _selectedTab.value = TeaGramTab.HOME
        }
    }

    fun toggleReelLike(reelId: Long) {
        repository.toggleReelLike(reelId)
        _reels.value = _reels.value.map { reel ->
            if (reel.id == reelId) {
                val newLiked = !reel.isLiked
                val newLikes = if (newLiked) reel.likesCount + 1 else reel.likesCount - 1
                reel.copy(isLiked = newLiked, likesCount = newLikes)
            } else reel
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setExploreCategory(category: String) {
        _selectedExploreCategory.value = category
    }

    fun openShareModal(post: PostEntity) {
        _sharePost.value = post
    }

    fun closeShareModal() {
        _sharePost.value = null
    }

    fun openPostDetail(post: PostEntity) {
        _selectedPostDetail.value = post
    }

    fun closePostDetail() {
        _selectedPostDetail.value = null
    }

    fun selectReel(reelId: Long) {
        _selectedReelId.value = reelId
    }

    fun closeReelOverlays() {
        _selectedReelId.value = null
    }

    fun updateProfile(
        username: String,
        displayName: String,
        bio: String,
        avatarResName: String,
        websiteUrl: String
    ) {
        viewModelScope.launch {
            val oldUsername = _currentUserUsername.value
            val cleanUsername = username.trim().removePrefix("@").ifBlank { oldUsername }
            repository.updateUserProfile(
                oldUsername = oldUsername,
                newUsername = cleanUsername,
                displayName = displayName.trim(),
                bio = bio.trim(),
                avatarResName = avatarResName,
                websiteUrl = websiteUrl.trim()
            )
            _currentUserUsername.value = cleanUsername
        }
    }
}

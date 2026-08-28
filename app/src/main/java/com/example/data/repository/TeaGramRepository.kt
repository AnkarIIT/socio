package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.ChatThread
import com.example.data.model.CommentEntity
import com.example.data.model.DirectMessageEntity
import com.example.data.model.FollowEntity
import com.example.data.model.PostEntity
import com.example.data.model.ReelItem
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import android.content.Context
import android.content.SharedPreferences

class TeaGramRepository(private val db: AppDatabase, private val context: Context) {
    private val likedReelsPrefs: SharedPreferences = context.getSharedPreferences("teagram_likes", Context.MODE_PRIVATE)
    private val likedReelIds: Set<String>
        get() = likedReelsPrefs.getStringSet("liked_reels", emptySet()) ?: emptySet()

    fun isReelLiked(reelId: Long): Boolean = likedReelIds.contains(reelId.toString())

    val allPosts: Flow<List<PostEntity>> = db.postDao().getAllPosts()
    val savedPosts: Flow<List<PostEntity>> = db.postDao().getSavedPosts()
    val allStories: Flow<List<StoryEntity>> = db.storyDao().getAllStories()
    val currentUser: Flow<UserEntity?> = db.userDao().getCurrentUser()
    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()

    fun getUserProfile(username: String): Flow<UserEntity?> {
        return db.userDao().getUserByUsername(username)
    }

    suspend fun getUserProfileDirect(username: String): UserEntity? {
        return db.userDao().getUserDirect(username)
    }

    fun isFollowing(follower: String, target: String): Flow<Boolean> {
        return db.followDao().isFollowing(follower, target)
    }

    fun getFollowingCount(follower: String): Flow<Int> {
        return db.followDao().getFollowingCount(follower)
    }

    fun getFollowersCount(target: String): Flow<Int> {
        return db.followDao().getFollowersCount(target)
    }

    fun getFollowingUsernames(follower: String): Flow<List<String>> {
        return db.followDao().getFollowingUsernames(follower)
    }

    suspend fun toggleFollow(follower: String, target: String) = withContext(Dispatchers.IO) {
        val isAlready = db.followDao().isFollowingDirect(follower, target)
        if (isAlready) {
            db.followDao().deleteFollow(follower, target)
        } else {
            db.followDao().insertFollow(FollowEntity(followerUsername = follower, followingUsername = target))
        }
    }

    fun getFeedPosts(currentUsername: String, limit: Int = 50): Flow<List<PostEntity>> {
        return db.postDao().getFeedPostsForUserPaged(currentUsername, limit)
    }

    fun getFeedPostsCount(currentUsername: String): Flow<Int> {
        return db.postDao().getFeedPostsCount(currentUsername)
    }

    fun getPostsByAuthor(username: String): Flow<List<PostEntity>> {
        return db.postDao().getPostsByAuthor(username)
    }

    fun getPostsCountByAuthor(username: String): Flow<Int> {
        return db.postDao().getPostsCountByAuthor(username)
    }

    suspend fun updateUserProfile(
        oldUsername: String,
        newUsername: String,
        displayName: String,
        bio: String,
        avatarResName: String,
        websiteUrl: String
    ) = withContext(Dispatchers.IO) {
        val existing = db.userDao().getUserDirect(oldUsername)
        val isCurrent = existing?.isCurrentUser ?: true
        val baseFollowers = existing?.baseFollowersCount ?: 18420
        val baseFollowing = existing?.baseFollowingCount ?: 2

        if (oldUsername != newUsername) {
            db.userDao().deleteUser(oldUsername)
            db.userDao().insertUser(
                UserEntity(
                    username = newUsername,
                    displayName = displayName,
                    bio = bio,
                    avatarResName = avatarResName,
                    websiteUrl = websiteUrl,
                    baseFollowersCount = baseFollowers,
                    baseFollowingCount = baseFollowing,
                    isCurrentUser = isCurrent,
                    isVerified = true
                )
            )
            db.postDao().updateAuthorInfo(oldUsername, newUsername, avatarResName)
            db.followDao().updateFollowerUsername(oldUsername, newUsername)
            db.followDao().updateFollowingUsername(oldUsername, newUsername)
        } else {
            db.userDao().updateUserData(
                oldUsername = oldUsername,
                newUsername = newUsername,
                displayName = displayName,
                bio = bio,
                avatarResName = avatarResName,
                websiteUrl = websiteUrl
            )
            db.postDao().updateAuthorInfo(oldUsername, newUsername, avatarResName)
        }
    }

    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> {
        return db.commentDao().getCommentsForPost(postId)
    }

    fun getMessagesForConversation(convId: String): Flow<List<DirectMessageEntity>> {
        return db.messageDao().getMessages(convId)
    }

    suspend fun toggleLike(postId: Long) = withContext(Dispatchers.IO) {
        val post = db.postDao().getPostById(postId) ?: return@withContext
        val newLiked = !post.isLiked
        val newCount = if (newLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
        db.postDao().updateLikeStatus(postId, newLiked, newCount)
    }

    suspend fun toggleSave(postId: Long) = withContext(Dispatchers.IO) {
        val post = db.postDao().getPostById(postId) ?: return@withContext
        db.postDao().updateSavedStatus(postId, !post.isSaved)
    }

    suspend fun addComment(postId: Long, text: String, username: String = "sarah.creatives") = withContext(Dispatchers.IO) {
        val comment = CommentEntity(
            postId = postId,
            authorUsername = username,
            authorAvatarUrl = "img_feed_portrait",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        db.commentDao().insertComment(comment)
        db.postDao().incrementCommentsCount(postId)
    }

    suspend fun toggleCommentLike(commentId: Long, isCurrentlyLiked: Boolean, currentLikes: Int) = withContext(Dispatchers.IO) {
        val newLiked = !isCurrentlyLiked
        val newCount = if (newLiked) currentLikes + 1 else maxOf(0, currentLikes - 1)
        db.commentDao().updateCommentLike(commentId, newLiked, newCount)
    }

    suspend fun createPost(
        imageResName: String,
        caption: String,
        location: String?,
        username: String = "sarah.creatives"
    ) = withContext(Dispatchers.IO) {
        val post = PostEntity(
            authorUsername = username,
            authorAvatarUrl = "img_feed_portrait",
            authorIsVerified = true,
            imageResName = imageResName,
            caption = caption,
            location = location,
            likesCount = 1,
            isLiked = true,
            isSaved = false,
            commentsCount = 0,
            timestamp = System.currentTimeMillis()
        )
        db.postDao().insertPost(post)
    }

    suspend fun markStorySeen(storyId: Long) = withContext(Dispatchers.IO) {
        db.storyDao().markStorySeen(storyId)
    }

    suspend fun sendMessage(conversationId: String, text: String) = withContext(Dispatchers.IO) {
        val message = DirectMessageEntity(
            conversationId = conversationId,
            senderUsername = "sarah.creatives",
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        db.messageDao().insertMessage(message)
    }

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        if (db.userDao().getCount() == 0) {
            val users = listOf(
                UserEntity(
                    username = "sarah.creatives",
                    displayName = "Sarah Jenkins",
                    bio = "Visual artist & photographer 📸\nCapturing light & architecture across the world ✨\n📍 San Francisco, CA",
                    avatarResName = "img_feed_portrait",
                    websiteUrl = "sarahjenkins.studio",
                    baseFollowersCount = 18420,
                    baseFollowingCount = 2,
                    isCurrentUser = true,
                    isVerified = true
                ),
                UserEntity(
                    username = "elena.voyages",
                    displayName = "Elena Rostova",
                    bio = "Travel photographer & visual storyteller 🌍\nSantorini • Amalfi • Kyoto ✨",
                    avatarResName = "img_feed_travel",
                    websiteUrl = "elenavoyages.com",
                    baseFollowersCount = 42500,
                    baseFollowingCount = 320,
                    isCurrentUser = false,
                    isVerified = true
                ),
                UserEntity(
                    username = "nordic.spaces",
                    displayName = "Nordic Architecture",
                    bio = "Modern architectural curves, concrete & natural light 🏛️📐 Copenhagen",
                    avatarResName = "img_feed_arch",
                    websiteUrl = "nordicspaces.dk",
                    baseFollowersCount = 28900,
                    baseFollowingCount = 150,
                    isCurrentUser = false,
                    isVerified = true
                ),
                UserEntity(
                    username = "artisan_brew",
                    displayName = "Artisan Roasters",
                    bio = "Slow mornings, single origin roasts and latte art craft ☕ Kinfolk Cafe",
                    avatarResName = "img_feed_coffee",
                    websiteUrl = "artisanbrew.coffee",
                    baseFollowersCount = 15300,
                    baseFollowingCount = 210,
                    isCurrentUser = false,
                    isVerified = false
                ),
                UserEntity(
                    username = "marcus_lens",
                    displayName = "Marcus Vance",
                    bio = "Analogue 35mm & 120 medium format portraiture 🎞️ Soho, NYC",
                    avatarResName = "img_feed_portrait",
                    websiteUrl = "marcusvance.photo",
                    baseFollowersCount = 36800,
                    baseFollowingCount = 480,
                    isCurrentUser = false,
                    isVerified = true
                ),
                UserEntity(
                    username = "sophia_wander",
                    displayName = "Sophia Lin",
                    bio = "Nomadic moments, culinary journeys and design essays 🌿",
                    avatarResName = "img_feed_travel",
                    websiteUrl = "sophiawander.me",
                    baseFollowersCount = 9200,
                    baseFollowingCount = 190,
                    isCurrentUser = false,
                    isVerified = false
                )
            )
            db.userDao().insertUsers(users)
        }

        if (db.followDao().getCount() == 0) {
            val initialFollows = listOf(
                FollowEntity(
                    followerUsername = "sarah.creatives",
                    followingUsername = "elena.voyages"
                ),
                FollowEntity(
                    followerUsername = "sarah.creatives",
                    followingUsername = "nordic.spaces"
                )
            )
            db.followDao().insertFollows(initialFollows)
        }

        if (db.postDao().getCount() == 0) {
            val initialPosts = listOf(
                PostEntity(
                    id = 1,
                    authorUsername = "elena.voyages",
                    authorAvatarUrl = "img_feed_travel",
                    authorIsVerified = true,
                    imageResName = "img_feed_travel",
                    caption = "Lost in the blue and white labyrinths of Santorini 🇬🇷✨ Sunset here feels like a painted dream. Which island is on your bucket list this year?",
                    location = "Oia, Santorini, Greece",
                    likesCount = 3842,
                    isLiked = false,
                    isSaved = false,
                    commentsCount = 4,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30 // 30 mins ago
                ),
                PostEntity(
                    id = 2,
                    authorUsername = "nordic.spaces",
                    authorAvatarUrl = "img_feed_arch",
                    authorIsVerified = true,
                    imageResName = "img_feed_arch",
                    caption = "Form follows light. Minimal concrete curves bathed in the late afternoon glow. Architecture is the learned game of magnificent volumes played in light 🏛️📐",
                    location = "Copenhagen Architectural Center",
                    likesCount = 2190,
                    isLiked = true,
                    isSaved = true,
                    commentsCount = 3,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2 // 2 hours ago
                ),
                PostEntity(
                    id = 3,
                    authorUsername = "sarah.creatives",
                    authorAvatarUrl = "img_feed_portrait",
                    authorIsVerified = true,
                    imageResName = "img_feed_portrait",
                    caption = "Reflections and golden horizons over the bay 🌅 Studio quiet time after a long week of gallery curation.",
                    location = "San Francisco, CA",
                    likesCount = 4120,
                    isLiked = true,
                    isSaved = false,
                    commentsCount = 8,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 4 // 4 hours ago
                ),
                PostEntity(
                    id = 4,
                    authorUsername = "elena.voyages",
                    authorAvatarUrl = "img_feed_travel",
                    authorIsVerified = true,
                    imageResName = "img_feed_travel",
                    caption = "Morning light spilling over the Aegean Sea. Nothing beats that first breath of sea breeze. 🌊💙",
                    location = "Amorgos Island, Cyclades",
                    likesCount = 2890,
                    isLiked = false,
                    isSaved = false,
                    commentsCount = 2,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 6 // 6 hours ago
                ),
                PostEntity(
                    id = 5,
                    authorUsername = "nordic.spaces",
                    authorAvatarUrl = "img_feed_arch",
                    authorIsVerified = true,
                    imageResName = "img_feed_arch",
                    caption = "Spatial rhythm and acoustic wood slats. A study in texture and restrained balance 🌿",
                    location = "Aarhus Design Studio",
                    likesCount = 1780,
                    isLiked = false,
                    isSaved = false,
                    commentsCount = 1,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 9 // 9 hours ago
                ),
                PostEntity(
                    id = 6,
                    authorUsername = "artisan_brew",
                    authorAvatarUrl = "img_feed_coffee",
                    authorIsVerified = false,
                    imageResName = "img_feed_coffee",
                    caption = "Slow mornings, silky oat cortado and notebook reflections ☕📖 The aroma of single-origin beans makes everything better.",
                    location = "Kinfolk Cafe & Roastery",
                    likesCount = 1450,
                    isLiked = false,
                    isSaved = false,
                    commentsCount = 2,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 12 // 12 hours ago
                ),
                PostEntity(
                    id = 7,
                    authorUsername = "marcus_lens",
                    authorAvatarUrl = "img_feed_portrait",
                    authorIsVerified = true,
                    imageResName = "img_feed_portrait",
                    caption = "Street portrait series with vintage 35mm glass. Golden hour magic hits differently through analogue lenses 🎞️💫",
                    location = "SoHo, New York",
                    likesCount = 5219,
                    isLiked = false,
                    isSaved = false,
                    commentsCount = 5,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 18 // 18 hours ago
                ),
                PostEntity(
                    id = 8,
                    authorUsername = "sarah.creatives",
                    authorAvatarUrl = "img_feed_portrait",
                    authorIsVerified = true,
                    imageResName = "img_feed_arch",
                    caption = "New architectural photo essay printed on Hahnemühle cotton paper. Exhibition opens this Friday! 🎨✨",
                    location = "Modern Art Pavilion SF",
                    likesCount = 3650,
                    isLiked = false,
                    isSaved = false,
                    commentsCount = 12,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 // 1 day ago
                ),
                PostEntity(
                    id = 9,
                    authorUsername = "artisan_brew",
                    authorAvatarUrl = "img_feed_coffee",
                    authorIsVerified = false,
                    imageResName = "img_feed_coffee",
                    caption = "Dialing in the washed Geisha roast. Jasmine floral notes and sweet citrus acidity 🌸☕",
                    location = "Kinfolk Roasting Lab",
                    likesCount = 980,
                    isLiked = false,
                    isSaved = false,
                    commentsCount = 4,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 30 // 1.2 days ago
                ),
                PostEntity(
                    id = 10,
                    authorUsername = "marcus_lens",
                    authorAvatarUrl = "img_feed_portrait",
                    authorIsVerified = true,
                    imageResName = "img_feed_travel",
                    caption = "Rooftop views as the rain cleared over Manhattan. Always carry your camera with you 🌆🌧️",
                    location = "Lower East Side, NYC",
                    likesCount = 4310,
                    isLiked = false,
                    isSaved = false,
                    commentsCount = 6,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 36 // 1.5 days ago
                )
            )
            db.postDao().insertPosts(initialPosts)

            // Seed initial comments
            val initialComments = listOf(
                CommentEntity(postId = 1, authorUsername = "marcus_lens", authorAvatarUrl = "img_feed_portrait", text = "The color palette is breathtaking! Did you use a circular polarizer? 👏", timestamp = System.currentTimeMillis() - 1000 * 60 * 30, likesCount = 12),
                CommentEntity(postId = 1, authorUsername = "nordic.spaces", authorAvatarUrl = "img_feed_arch", text = "Those Cycladic domes are pure architectural perfection.", timestamp = System.currentTimeMillis() - 1000 * 60 * 20, likesCount = 5),
                CommentEntity(postId = 1, authorUsername = "sophia_wander", authorAvatarUrl = "img_feed_coffee", text = "Taking notes for my summer itinerary! 😍", timestamp = System.currentTimeMillis() - 1000 * 60 * 15, likesCount = 2),
                CommentEntity(postId = 1, authorUsername = "artisan_brew", authorAvatarUrl = "img_feed_coffee", text = "Need that Greek iced frappe right about now!", timestamp = System.currentTimeMillis() - 1000 * 60 * 8, likesCount = 1),

                CommentEntity(postId = 2, authorUsername = "sarah.creatives", authorAvatarUrl = "img_feed_portrait", text = "Those shadows create such poetic rhythm!", timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2, likesCount = 8),
                CommentEntity(postId = 2, authorUsername = "elena.voyages", authorAvatarUrl = "img_feed_travel", text = "The geometry here is incredible 🙌", timestamp = System.currentTimeMillis() - 1000 * 60 * 60, likesCount = 4),

                CommentEntity(postId = 3, authorUsername = "marcus_lens", authorAvatarUrl = "img_feed_portrait", text = "That latte art is crisp! What blend are they pulling?", timestamp = System.currentTimeMillis() - 1000 * 60 * 120, likesCount = 3),
                CommentEntity(postId = 3, authorUsername = "sarah.creatives", authorAvatarUrl = "img_feed_portrait", text = "My favorite Sunday ritual ☕", timestamp = System.currentTimeMillis() - 1000 * 60 * 60, likesCount = 2),

                CommentEntity(postId = 4, authorUsername = "elena.voyages", authorAvatarUrl = "img_feed_travel", text = "The analog grain adds so much character!", timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 10, likesCount = 14)
            )
            db.commentDao().insertComments(initialComments)
        }

        if (db.storyDao().getCount() == 0) {
            val initialStories = listOf(
                StoryEntity(
                    id = 1,
                    username = "Your story",
                    avatarUrl = "img_feed_portrait",
                    storyImageResName = "img_feed_travel",
                    caption = "Golden moments 🌅",
                    hasUnseen = false,
                    isUserStory = true,
                    timestamp = System.currentTimeMillis()
                ),
                StoryEntity(
                    id = 2,
                    username = "elena.voyages",
                    avatarUrl = "img_feed_travel",
                    storyImageResName = "img_feed_travel",
                    caption = "Santorini waking up early morning 🌊",
                    hasUnseen = true,
                    isUserStory = false,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 40
                ),
                StoryEntity(
                    id = 3,
                    username = "artisan_brew",
                    avatarUrl = "img_feed_coffee",
                    storyImageResName = "img_feed_coffee",
                    caption = "First pour of Ethiopian Yirgacheffe ☕✨",
                    hasUnseen = true,
                    isUserStory = false,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 90
                ),
                StoryEntity(
                    id = 4,
                    username = "nordic.spaces",
                    avatarUrl = "img_feed_arch",
                    storyImageResName = "img_feed_arch",
                    caption = "Light play at 4 PM ☀️📐",
                    hasUnseen = true,
                    isUserStory = false,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 180
                ),
                StoryEntity(
                    id = 5,
                    username = "marcus_lens",
                    avatarUrl = "img_feed_portrait",
                    storyImageResName = "img_feed_portrait",
                    caption = "Shooting street portraits on Kodak Portra 400 🎞️",
                    hasUnseen = true,
                    isUserStory = false,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 240
                )
            )
            db.storyDao().insertStories(initialStories)
        }

        if (db.messageDao().getCount() == 0) {
            val initialDMs = listOf(
                DirectMessageEntity(
                    conversationId = "elena.voyages",
                    senderUsername = "elena.voyages",
                    text = "Hey Sarah! Loved your recent shots from the coast!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                    isFromMe = false
                ),
                DirectMessageEntity(
                    conversationId = "elena.voyages",
                    senderUsername = "sarah.creatives",
                    text = "Thank you so much Elena! The lighting was unreal that evening.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60,
                    isFromMe = true
                ),
                DirectMessageEntity(
                    conversationId = "elena.voyages",
                    senderUsername = "elena.voyages",
                    text = "Are you joining the photography workshop in Lisbon next month?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 15,
                    isFromMe = false
                ),

                DirectMessageEntity(
                    conversationId = "marcus_lens",
                    senderUsername = "marcus_lens",
                    text = "Which lens adapter did you end up getting for the Leica?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                    isFromMe = false
                ),
                DirectMessageEntity(
                    conversationId = "marcus_lens",
                    senderUsername = "sarah.creatives",
                    text = "Got the Novoflex M to E-mount. Build quality is exceptional.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 4,
                    isFromMe = true
                )
            )
            db.messageDao().insertMessages(initialDMs)
        }
    }

    fun getChatThreads(): List<ChatThread> {
        return listOf(
            ChatThread(
                conversationId = "elena.voyages",
                partnerUsername = "elena.voyages",
                partnerFullName = "Elena Rostova",
                partnerAvatarResName = "img_feed_travel",
                lastMessage = "Are you joining the photography workshop in Lisbon next month?",
                timestampFormatted = "15m",
                isOnline = true,
                unreadCount = 1
            ),
            ChatThread(
                conversationId = "marcus_lens",
                partnerUsername = "marcus_lens",
                partnerFullName = "Marcus Vance",
                partnerAvatarResName = "img_feed_portrait",
                lastMessage = "Got the Novoflex M to E-mount. Build quality is exceptional.",
                timestampFormatted = "4h",
                isOnline = false,
                unreadCount = 0
            ),
            ChatThread(
                conversationId = "artisan_brew",
                partnerUsername = "artisan_brew",
                partnerFullName = "Artisan Roasters",
                partnerAvatarResName = "img_feed_coffee",
                lastMessage = "Thanks for featuring our cafe on your story!",
                timestampFormatted = "1d",
                isOnline = false,
                unreadCount = 0
            ),
            ChatThread(
                conversationId = "nordic.spaces",
                partnerUsername = "nordic.spaces",
                partnerFullName = "Nordic Architecture",
                partnerAvatarResName = "img_feed_arch",
                lastMessage = "Shared a photo with you",
                timestampFormatted = "2d",
                isOnline = true,
                unreadCount = 0
            )
        )
    }

    fun getReels(): List<ReelItem> {
        val liked = likedReelIds
        return listOf(
            ReelItem(
                id = 101,
                authorUsername = "elena.voyages",
                authorAvatarResName = "img_feed_travel",
                imageResName = "img_feed_travel",
                caption = "Sunset symphony in Oia. The moment the sun dips below the Aegean horizon 🌅🇬🇷 #santorini #travelreels #aesthetic",
                audioTitle = "Original Audio - elena.voyages • Golden Hour Vibes",
                likesCount = 84200,
                commentsCount = 1240,
                sharesCount = 6300,
                isLiked = liked.contains("101")
            ),
            ReelItem(
                id = 102,
                authorUsername = "artisan_brew",
                authorAvatarResName = "img_feed_coffee",
                imageResName = "img_feed_coffee",
                caption = "The perfect swan pour in 4K ☕🦢 Patience and silky microfoam is all you need. #latteart #barista #coffeelover",
                audioTitle = "Morning Chill Beats • Lofi Coffeehouse",
                likesCount = 39100,
                commentsCount = 680,
                sharesCount = 3100,
                isLiked = liked.contains("102")
            ),
            ReelItem(
                id = 103,
                authorUsername = "nordic.spaces",
                authorAvatarResName = "img_feed_arch",
                imageResName = "img_feed_arch",
                caption = "When modern architectural geometry meets the golden sun shadows 🏛️✨ #minimalism #architecturelovers",
                audioTitle = "Ambient Electronic Waves • Hans Zimmer tribute",
                likesCount = 52800,
                commentsCount = 890,
                sharesCount = 4200,
                isLiked = liked.contains("103")
            ),
            ReelItem(
                id = 104,
                authorUsername = "marcus_lens",
                authorAvatarResName = "img_feed_portrait",
                imageResName = "img_feed_portrait",
                caption = "Asking strangers for street portraits in NYC Soho! 📸 Their reactions are pure joy. #streetphotography #portraits",
                audioTitle = "Retro Grooves • Marcus Vance",
                likesCount = 112000,
                commentsCount = 2840,
                sharesCount = 9800,
                isLiked = liked.contains("104")
            )
        )
    }

    fun toggleReelLike(reelId: Long) {
        val current = likedReelIds.toMutableSet()
        val key = reelId.toString()
        if (current.contains(key)) {
            current.remove(key)
        } else {
            current.add(key)
        }
        likedReelsPrefs.edit().putStringSet("liked_reels", current).apply()
    }
}

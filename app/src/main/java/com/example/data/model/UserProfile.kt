package com.example.data.model

data class UserProfile(
    val username: String = "sarah.creatives",
    val fullName: String = "Sarah Jenkins",
    val bio: String = "Visual artist & photographer 📸\nCapturing light & architecture across the world ✨\n📍 San Francisco, CA",
    val websiteUrl: String = "sarahjenkins.studio",
    val avatarResName: String = "img_feed_portrait",
    val postsCount: Int = 42,
    val followersCount: Int = 18420,
    val followingCount: Int = 432,
    val isFollowing: Boolean = false,
    val isCurrentUser: Boolean = false,
    val isVerified: Boolean = false
)

data class ChatThread(
    val conversationId: String,
    val partnerUsername: String,
    val partnerFullName: String,
    val partnerAvatarResName: String,
    val lastMessage: String,
    val timestampFormatted: String,
    val isOnline: Boolean = false,
    val unreadCount: Int = 0
)

data class ReelItem(
    val id: Long,
    val authorUsername: String,
    val authorAvatarResName: String,
    val imageResName: String,
    val caption: String,
    val audioTitle: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean = false
)

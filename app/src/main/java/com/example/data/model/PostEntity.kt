package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorUsername: String,
    val authorAvatarUrl: String,
    val authorIsVerified: Boolean = false,
    val imageResName: String, // e.g. "img_feed_travel" or custom URI / URL
    val caption: String,
    val location: String? = null,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isSponsored: Boolean = false
)

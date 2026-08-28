package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reels",
    indices = [
        Index(value = ["authorUsername"])
    ]
)
data class ReelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorUsername: String,
    val authorAvatarResName: String,
    val imageResName: String,
    val caption: String,
    val audioTitle: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false
)

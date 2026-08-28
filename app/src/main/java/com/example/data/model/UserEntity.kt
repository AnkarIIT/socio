package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarResName: String,
    val websiteUrl: String = "",
    val baseFollowersCount: Int = 0,
    val baseFollowingCount: Int = 0,
    val isCurrentUser: Boolean = false,
    val isVerified: Boolean = false
)

package com.example.data.model

import androidx.room.Entity

@Entity(
    tableName = "follows",
    primaryKeys = ["followerUsername", "followingUsername"]
)
data class FollowEntity(
    val followerUsername: String,
    val followingUsername: String,
    val timestamp: Long = System.currentTimeMillis()
)

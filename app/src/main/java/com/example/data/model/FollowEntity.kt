package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follows",
    primaryKeys = ["followerUsername", "followingUsername"],
    indices = [
        Index(value = ["followerUsername"]),
        Index(value = ["followingUsername"])
    ]
)
data class FollowEntity(
    val followerUsername: String,
    val followingUsername: String,
    val timestamp: Long = System.currentTimeMillis()
)

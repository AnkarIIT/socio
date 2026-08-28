package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stories",
    indices = [
        Index(value = ["username"]),
        Index(value = ["timestamp"])
    ]
)
data class StoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val avatarUrl: String,
    val storyImageResName: String,
    val caption: String = "",
    val hasUnseen: Boolean = true,
    val isUserStory: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

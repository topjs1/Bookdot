package com.bookdot.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mediaUrl: String,
    val mediaType: String,
    val viewCount: Int = 0,
    val isViewed: Boolean = false,
    val expiresAt: Long,
    val createdAt: Long
)
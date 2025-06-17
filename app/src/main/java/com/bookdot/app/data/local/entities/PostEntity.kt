package com.bookdot.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val content: String,
    val imageUrls: List<String>,
    val videoUrl: String?,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Long
)
package com.bookdot.app.domain.model

data class Story(
    val id: String,
    val userId: String,
    val user: User,
    val mediaUrl: String,
    val mediaType: MediaType,
    val viewCount: Int = 0,
    val isViewed: Boolean = false,
    val expiresAt: Long,
    val createdAt: Long
)

enum class MediaType { IMAGE, VIDEO }
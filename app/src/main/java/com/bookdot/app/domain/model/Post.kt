package com.bookdot.app.domain.model

data class Post(
    val id: String,
    val userId: String,
    val user: User,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long
)
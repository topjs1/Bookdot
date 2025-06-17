package com.bookdot.app.domain.model

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val user: User,
    val content: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long
)
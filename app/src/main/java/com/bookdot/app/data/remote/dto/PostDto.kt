package com.bookdot.app.data.remote.dto

import com.bookdot.app.data.local.entities.PostEntity
import com.bookdot.app.domain.model.Post
import com.bookdot.app.domain.model.User

data class PostDto(
    val id: String,
    val userId: String,
    val user: UserDto,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long
)

data class CreatePostRequest(
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null
)

data class LikeResponse(
    val liked: Boolean,
    val likeCount: Int
)

fun PostDto.toDomainModel(): Post {
    return Post(
        id = id,
        userId = userId,
        user = user.toDomainModel(),
        content = content,
        imageUrls = imageUrls,
        videoUrl = videoUrl,
        likeCount = likeCount,
        commentCount = commentCount,
        isLiked = false, // API에서는 기본값으로 설정
        createdAt = createdAt
    )
}

fun PostDto.toEntity(): PostEntity {
    return PostEntity(
        id = id,
        userId = userId,
        content = content,
        imageUrls = imageUrls,
        videoUrl = videoUrl,
        likeCount = likeCount,
        commentCount = commentCount,
        createdAt = createdAt
    )
}
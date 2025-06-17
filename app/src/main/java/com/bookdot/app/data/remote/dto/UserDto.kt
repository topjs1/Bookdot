package com.bookdot.app.data.remote.dto

import com.bookdot.app.data.local.entities.UserEntity
import com.bookdot.app.domain.model.User

data class UserDto(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val isFollowing: Boolean = false,
    val createdAt: Long
)

fun UserDto.toDomainModel(): User {
    return User(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bio = bio,
        followerCount = followerCount,
        followingCount = followingCount,
        postCount = postCount,
        isFollowing = isFollowing,
        createdAt = createdAt
    )
}

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bio = bio,
        followerCount = followerCount,
        followingCount = followingCount,
        postCount = postCount,
        isFollowing = isFollowing,
        createdAt = createdAt
    )
}
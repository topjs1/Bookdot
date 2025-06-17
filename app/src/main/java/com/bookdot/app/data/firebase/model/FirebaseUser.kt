package com.bookdot.app.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FirebaseUser(
    @DocumentId
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val bio: String = "",
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // Firestore를 위한 빈 생성자
    constructor() : this("", "", "", null, "", 0, 0, 0, null)
    
    fun toDomainModel(): com.bookdot.app.domain.model.User {
        return com.bookdot.app.domain.model.User(
            id = id,
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl,
            bio = bio,
            followerCount = followerCount,
            followingCount = followingCount,
            postCount = postCount,
            isFollowing = false, // 이는 별도로 계산해야 함
            createdAt = createdAt?.time ?: 0L
        )
    }
}
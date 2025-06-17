package com.bookdot.app.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FirebasePost(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // Firestore를 위한 빈 생성자
    constructor() : this("", "", "", emptyList(), null, 0, 0, null)
}
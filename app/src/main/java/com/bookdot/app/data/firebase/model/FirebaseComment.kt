package com.bookdot.app.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FirebaseComment(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val likeCount: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // Firestore를 위한 빈 생성자
    constructor() : this("", "", "", "", 0, null)
}
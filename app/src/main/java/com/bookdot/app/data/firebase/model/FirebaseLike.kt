package com.bookdot.app.data.firebase.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FirebaseLike(
    @DocumentId
    val id: String = "",
    val targetId: String = "", // postId 또는 commentId
    val targetType: String = "", // "post" 또는 "comment"
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // Firestore를 위한 빈 생성자
    constructor() : this("", "", "", "", null)
    
    companion object {
        const val TYPE_POST = "post"
        const val TYPE_COMMENT = "comment"
    }
}
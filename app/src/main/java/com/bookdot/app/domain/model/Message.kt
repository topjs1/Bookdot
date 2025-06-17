package com.bookdot.app.domain.model

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val isEncrypted: Boolean = true,
    val readAt: Long? = null,
    val createdAt: Long
)
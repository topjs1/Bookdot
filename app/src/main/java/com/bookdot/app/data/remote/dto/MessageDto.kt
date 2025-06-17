package com.bookdot.app.data.remote.dto

import com.bookdot.app.data.local.entities.MessageEntity
import com.bookdot.app.domain.model.Message

data class MessageDto(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val isEncrypted: Boolean = true,
    val readAt: Long? = null,
    val createdAt: Long
)

data class SendMessageRequest(
    val content: String
)

fun MessageDto.toDomainModel(): Message {
    return Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        content = content,
        isEncrypted = isEncrypted,
        readAt = readAt,
        createdAt = createdAt
    )
}

fun MessageDto.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        content = content,
        isEncrypted = isEncrypted,
        readAt = readAt,
        createdAt = createdAt
    )
}
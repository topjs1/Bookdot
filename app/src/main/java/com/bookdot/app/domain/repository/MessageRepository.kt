package com.bookdot.app.domain.repository

import com.bookdot.app.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessagesByConversation(conversationId: String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, content: String): Result<Message>
    suspend fun markAsRead(messageId: String): Result<Unit>
    suspend fun markConversationAsRead(conversationId: String): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}
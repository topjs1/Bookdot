package com.bookdot.app.data.repository

import com.bookdot.app.data.local.dao.MessageDao
import com.bookdot.app.data.remote.api.MessageApi
import com.bookdot.app.data.remote.dto.SendMessageRequest
import com.bookdot.app.data.remote.dto.toDomainModel
import com.bookdot.app.data.remote.dto.toEntity
import com.bookdot.app.domain.model.Message
import com.bookdot.app.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val messageApi: MessageApi
) : MessageRepository {
    
    override fun getMessagesByConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesByConversation(conversationId).map { messageEntities ->
            messageEntities.map { entity ->
                Message(
                    id = entity.id,
                    conversationId = entity.conversationId,
                    senderId = entity.senderId,
                    content = entity.content,
                    isEncrypted = entity.isEncrypted,
                    readAt = entity.readAt,
                    createdAt = entity.createdAt
                )
            }
        }
    }
    
    override suspend fun sendMessage(conversationId: String, content: String): Result<Message> {
        return try {
            val response = messageApi.sendMessage(
                conversationId,
                SendMessageRequest(content)
            )
            messageDao.insertMessage(response.toEntity())
            Result.success(response.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markAsRead(messageId: String): Result<Unit> {
        return try {
            messageApi.markAsRead(messageId)
            messageDao.markAsRead(messageId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        return try {
            messageApi.markConversationAsRead(conversationId)
            // TODO: Get current user ID
            val currentUserId = "current_user_id"
            messageDao.markConversationAsRead(conversationId, currentUserId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            messageApi.deleteMessage(messageId)
            val message = messageDao.getMessageById(messageId)
            message?.let { messageDao.deleteMessage(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
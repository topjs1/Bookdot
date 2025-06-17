package com.bookdot.app.data.local.dao

import androidx.room.*
import com.bookdot.app.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Query("UPDATE messages SET readAt = :readAt WHERE id = :messageId")
    suspend fun markAsRead(messageId: String, readAt: Long)
    
    @Query("UPDATE messages SET readAt = :readAt WHERE conversationId = :conversationId AND senderId != :currentUserId AND readAt IS NULL")
    suspend fun markConversationAsRead(conversationId: String, currentUserId: String, readAt: Long)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesFromConversation(conversationId: String)
}
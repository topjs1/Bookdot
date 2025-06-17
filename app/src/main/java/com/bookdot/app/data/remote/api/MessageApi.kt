package com.bookdot.app.data.remote.api

import com.bookdot.app.data.remote.dto.MessageDto
import com.bookdot.app.data.remote.dto.SendMessageRequest
import retrofit2.http.*

interface MessageApi {
    @GET("conversations/{conversationId}/messages")
    suspend fun getMessages(@Path("conversationId") conversationId: String): List<MessageDto>
    
    @POST("conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String, 
        @Body request: SendMessageRequest
    ): MessageDto
    
    @PUT("messages/{messageId}/read")
    suspend fun markAsRead(@Path("messageId") messageId: String)
    
    @PUT("conversations/{conversationId}/read")
    suspend fun markConversationAsRead(@Path("conversationId") conversationId: String)
    
    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String)
}
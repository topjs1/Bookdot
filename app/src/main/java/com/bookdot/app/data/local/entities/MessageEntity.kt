package com.bookdot.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val isEncrypted: Boolean = true,
    val readAt: Long? = null,
    val createdAt: Long
)
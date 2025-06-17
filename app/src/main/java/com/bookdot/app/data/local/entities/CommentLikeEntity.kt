package com.bookdot.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "comment_likes",
    primaryKeys = ["commentId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = CommentEntity::class,
            parentColumns = ["id"],
            childColumns = ["commentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["commentId"]),
        Index(value = ["userId"])
    ]
)
data class CommentLikeEntity(
    val commentId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)
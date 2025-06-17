package com.bookdot.app.domain.repository

import com.bookdot.app.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getCommentsByPost(postId: String): Flow<List<Comment>>
    suspend fun getCommentById(commentId: String): Comment?
    suspend fun createComment(postId: String, content: String): Result<Comment>
    suspend fun likeComment(commentId: String): Result<Unit>
    suspend fun unlikeComment(commentId: String): Result<Unit>
    suspend fun deleteComment(commentId: String): Result<Unit>
}
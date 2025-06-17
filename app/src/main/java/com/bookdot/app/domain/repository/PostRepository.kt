package com.bookdot.app.domain.repository

import android.net.Uri
import com.bookdot.app.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getFeed(): Flow<List<Post>>
    fun getPostsByUser(userId: String): Flow<List<Post>>
    suspend fun getPostById(postId: String): Post?
    suspend fun createPost(content: String, images: List<Uri>): Result<Post>
    suspend fun likePost(postId: String): Result<Unit>
    suspend fun unlikePost(postId: String): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
}
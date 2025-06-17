package com.bookdot.app.data.local.dao

import androidx.room.*
import com.bookdot.app.data.local.entities.CommentLikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentLikeDao {
    
    @Query("SELECT * FROM comment_likes WHERE commentId = :commentId AND userId = :userId")
    suspend fun getCommentLike(commentId: String, userId: String): CommentLikeEntity?
    
    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId")
    suspend fun getLikeCount(commentId: String): Int
    
    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId")
    fun getLikeCountFlow(commentId: String): Flow<Int>
    
    @Query("SELECT EXISTS(SELECT 1 FROM comment_likes WHERE commentId = :commentId AND userId = :userId)")
    suspend fun isCommentLiked(commentId: String, userId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM comment_likes WHERE commentId = :commentId AND userId = :userId)")
    fun isCommentLikedFlow(commentId: String, userId: String): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommentLike(commentLike: CommentLikeEntity)
    
    @Query("DELETE FROM comment_likes WHERE commentId = :commentId AND userId = :userId")
    suspend fun deleteCommentLike(commentId: String, userId: String)
    
    @Query("DELETE FROM comment_likes WHERE commentId = :commentId")
    suspend fun deleteAllLikesForComment(commentId: String)
}
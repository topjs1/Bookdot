package com.bookdot.app.data.local.dao

import androidx.room.*
import com.bookdot.app.data.local.entities.PostLikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostLikeDao {
    
    @Query("SELECT * FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun getPostLike(postId: String, userId: String): PostLikeEntity?
    
    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    suspend fun getLikeCount(postId: String): Int
    
    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    fun getLikeCountFlow(postId: String): Flow<Int>
    
    @Query("SELECT EXISTS(SELECT 1 FROM post_likes WHERE postId = :postId AND userId = :userId)")
    suspend fun isPostLiked(postId: String, userId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM post_likes WHERE postId = :postId AND userId = :userId)")
    fun isPostLikedFlow(postId: String, userId: String): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostLike(postLike: PostLikeEntity)
    
    @Query("DELETE FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun deletePostLike(postId: String, userId: String)
    
    @Query("DELETE FROM post_likes WHERE postId = :postId")
    suspend fun deleteAllLikesForPost(postId: String)
}
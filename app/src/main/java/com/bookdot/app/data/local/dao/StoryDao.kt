package com.bookdot.app.data.local.dao

import androidx.room.*
import com.bookdot.app.data.local.entities.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories WHERE expiresAt > :currentTime ORDER BY createdAt DESC")
    fun getActiveStories(currentTime: Long): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM stories WHERE userId = :userId AND expiresAt > :currentTime ORDER BY createdAt DESC")
    fun getStoriesByUser(userId: String, currentTime: Long): Flow<List<StoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)
    
    @Query("UPDATE stories SET isViewed = :isViewed WHERE id = :storyId")
    suspend fun updateViewedStatus(storyId: String, isViewed: Boolean)
    
    @Query("DELETE FROM stories WHERE expiresAt <= :currentTime")
    suspend fun deleteExpiredStories(currentTime: Long)
    
    @Delete
    suspend fun deleteStory(story: StoryEntity)
}
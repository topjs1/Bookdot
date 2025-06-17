package com.bookdot.app.di

import android.content.Context
import androidx.room.Room
import com.bookdot.app.data.local.database.BootDotDatabase
import com.bookdot.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideBootDotDatabase(@ApplicationContext context: Context): BootDotDatabase {
        return Room.databaseBuilder(
            context,
            BootDotDatabase::class.java,
            "bootdot_database"
        ).fallbackToDestructiveMigration().build()
    }
    
    @Provides
    fun provideUserDao(database: BootDotDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun providePostDao(database: BootDotDatabase): PostDao {
        return database.postDao()
    }
    
    @Provides
    fun providePostLikeDao(database: BootDotDatabase): com.bookdot.app.data.local.dao.PostLikeDao {
        return database.postLikeDao()
    }
    
    @Provides
    fun provideStoryDao(database: BootDotDatabase): StoryDao {
        return database.storyDao()
    }
    
    @Provides
    fun provideMessageDao(database: BootDotDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    fun provideCommentDao(database: BootDotDatabase): CommentDao {
        return database.commentDao()
    }
    
    @Provides
    fun provideCommentLikeDao(database: BootDotDatabase): com.bookdot.app.data.local.dao.CommentLikeDao {
        return database.commentLikeDao()
    }
}
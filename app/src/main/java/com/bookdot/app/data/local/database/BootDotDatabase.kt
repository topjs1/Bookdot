package com.bookdot.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.bookdot.app.data.local.dao.*
import com.bookdot.app.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        PostLikeEntity::class,
        StoryEntity::class,
        MessageEntity::class,
        CommentEntity::class,
        CommentLikeEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BootDotDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun postLikeDao(): PostLikeDao
    abstract fun storyDao(): StoryDao
    abstract fun messageDao(): MessageDao
    abstract fun commentDao(): CommentDao
    abstract fun commentLikeDao(): CommentLikeDao
    
    companion object {
        @Volatile
        private var INSTANCE: BootDotDatabase? = null
        
        fun getDatabase(context: Context): BootDotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BootDotDatabase::class.java,
                    "bootdot_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
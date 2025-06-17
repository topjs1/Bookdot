package com.bookdot.app.di

import com.bookdot.app.data.repository.UserRepositoryImpl
import com.bookdot.app.data.repository.MessageRepositoryImpl
import com.bookdot.app.domain.repository.UserRepository
import com.bookdot.app.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    // PostRepository와 CommentRepository는 FirebaseModule에서 제공됨
    
    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    abstract fun bindMessageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ): MessageRepository
}
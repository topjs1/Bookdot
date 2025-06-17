package com.bookdot.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bookdot.app.data.firebase.auth.FirebaseAuthManager
import com.bookdot.app.data.firebase.repository.FirebasePostRepository
import com.bookdot.app.data.firebase.repository.FirebaseCommentRepository
import com.bookdot.app.domain.repository.PostRepository
import com.bookdot.app.domain.repository.CommentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuthManager(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirebaseAuthManager {
        return FirebaseAuthManager(firebaseAuth, firestore)
    }
    
    @Provides
    @Singleton
    fun providePostRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): PostRepository {
        return FirebasePostRepository(firestore, firebaseAuth)
    }
    
    @Provides
    @Singleton
    fun provideCommentRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): CommentRepository {
        return FirebaseCommentRepository(firestore, firebaseAuth)
    }
}
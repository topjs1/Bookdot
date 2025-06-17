package com.bookdot.app.domain.repository

import com.bookdot.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByUsername(username: String): User?
    fun searchUsers(query: String): Flow<List<User>>
    suspend fun followUser(userId: String): Result<Unit>
    suspend fun unfollowUser(userId: String): Result<Unit>
    suspend fun updateProfile(user: User): Result<User>
}
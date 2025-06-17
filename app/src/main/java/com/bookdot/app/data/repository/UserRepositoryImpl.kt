package com.bookdot.app.data.repository

import com.bookdot.app.data.local.dao.UserDao
import com.bookdot.app.data.remote.api.UserApi
import com.bookdot.app.data.remote.dto.toDomainModel
import com.bookdot.app.data.remote.dto.toEntity
import com.bookdot.app.domain.model.User
import com.bookdot.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi
) : UserRepository {
    
    override suspend fun getCurrentUser(): User? {
        return try {
            val userDto = userApi.getCurrentUser()
            userDao.insertUser(userDto.toEntity())
            userDto.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getUserById(userId: String): User? {
        // Check local cache first
        val cachedUser = userDao.getUserById(userId)
        if (cachedUser != null) {
            return User(
                id = cachedUser.id,
                username = cachedUser.username,
                displayName = cachedUser.displayName,
                avatarUrl = cachedUser.avatarUrl,
                bio = cachedUser.bio,
                followerCount = cachedUser.followerCount,
                followingCount = cachedUser.followingCount,
                postCount = cachedUser.postCount,
                isFollowing = cachedUser.isFollowing,
                createdAt = cachedUser.createdAt
            )
        }
        
        // Fetch from remote
        return try {
            val userDto = userApi.getUserById(userId)
            userDao.insertUser(userDto.toEntity())
            userDto.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getUserByUsername(username: String): User? {
        // Check local cache first
        val cachedUser = userDao.getUserByUsername(username)
        if (cachedUser != null) {
            return User(
                id = cachedUser.id,
                username = cachedUser.username,
                displayName = cachedUser.displayName,
                avatarUrl = cachedUser.avatarUrl,
                bio = cachedUser.bio,
                followerCount = cachedUser.followerCount,
                followingCount = cachedUser.followingCount,
                postCount = cachedUser.postCount,
                isFollowing = cachedUser.isFollowing,
                createdAt = cachedUser.createdAt
            )
        }
        
        // Fetch from remote
        return try {
            val userDto = userApi.getUserByUsername(username)
            userDao.insertUser(userDto.toEntity())
            userDto.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }
    
    override fun searchUsers(query: String): Flow<List<User>> {
        return userDao.searchUsers(query).map { userEntities ->
            userEntities.map { entity ->
                User(
                    id = entity.id,
                    username = entity.username,
                    displayName = entity.displayName,
                    avatarUrl = entity.avatarUrl,
                    bio = entity.bio,
                    followerCount = entity.followerCount,
                    followingCount = entity.followingCount,
                    postCount = entity.postCount,
                    isFollowing = entity.isFollowing,
                    createdAt = entity.createdAt
                )
            }
        }
    }
    
    override suspend fun followUser(userId: String): Result<Unit> {
        return try {
            userApi.followUser(userId)
            val user = userDao.getUserById(userId)
            user?.let {
                userDao.updateUser(it.copy(isFollowing = true, followerCount = it.followerCount + 1))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun unfollowUser(userId: String): Result<Unit> {
        return try {
            userApi.unfollowUser(userId)
            val user = userDao.getUserById(userId)
            user?.let {
                userDao.updateUser(it.copy(isFollowing = false, followerCount = it.followerCount - 1))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateProfile(user: User): Result<User> {
        return try {
            val userDto = com.bookdot.app.data.remote.dto.UserDto(
                id = user.id,
                username = user.username,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                bio = user.bio,
                followerCount = user.followerCount,
                followingCount = user.followingCount,
                postCount = user.postCount,
                isFollowing = user.isFollowing,
                createdAt = user.createdAt
            )
            val updatedUser = userApi.updateProfile(userDto)
            userDao.insertUser(updatedUser.toEntity())
            Result.success(updatedUser.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
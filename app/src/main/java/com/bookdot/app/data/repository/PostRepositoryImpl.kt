package com.bookdot.app.data.repository

import android.net.Uri
import com.bookdot.app.data.local.dao.PostDao
import com.bookdot.app.data.local.dao.UserDao
import com.bookdot.app.data.local.entities.PostEntity
import com.bookdot.app.data.remote.api.PostApi
import com.bookdot.app.data.remote.dto.CreatePostRequest
import com.bookdot.app.data.remote.dto.toDomainModel
import com.bookdot.app.data.remote.dto.toEntity
import com.bookdot.app.domain.model.Post
import com.bookdot.app.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val postLikeDao: com.bookdot.app.data.local.dao.PostLikeDao,
    private val userDao: UserDao,
    private val postApi: PostApi,
    private val accountManager: com.bookdot.app.security.AccountManager
) : PostRepository {
    
    override fun getFeed(): Flow<List<Post>> {
        return flow {
            postDao.getPosts(50, 0).collect { postEntities ->
                val currentUserId = accountManager.getCurrentUserId()
                val posts = postEntities.mapNotNull { postEntity ->
                    val user = userDao.getUserById(postEntity.userId)
                    user?.let { userEntity ->
                        val actualLikeCount = postLikeDao.getLikeCount(postEntity.id)
                        val isLiked = currentUserId?.let { 
                            postLikeDao.isPostLiked(postEntity.id, it) 
                        } ?: false
                        
                        Post(
                            id = postEntity.id,
                            userId = postEntity.userId,
                            user = com.bookdot.app.domain.model.User(
                                id = userEntity.id,
                                username = userEntity.username,
                                displayName = userEntity.displayName,
                                avatarUrl = userEntity.avatarUrl,
                                bio = userEntity.bio,
                                followerCount = userEntity.followerCount,
                                followingCount = userEntity.followingCount,
                                postCount = userEntity.postCount,
                                isFollowing = userEntity.isFollowing,
                                createdAt = userEntity.createdAt
                            ),
                            content = postEntity.content,
                            imageUrls = postEntity.imageUrls,
                            videoUrl = postEntity.videoUrl,
                            likeCount = actualLikeCount,
                            commentCount = postEntity.commentCount,
                            isLiked = isLiked,
                            createdAt = postEntity.createdAt
                        )
                    }
                }
                emit(posts)
            }
        }
    }
    
    override fun getPostsByUser(userId: String): Flow<List<Post>> {
        return flow {
            postDao.getPostsByUser(userId).collect { postEntities ->
                val currentUserId = accountManager.getCurrentUserId()
                val posts = postEntities.mapNotNull { postEntity ->
                    val user = userDao.getUserById(postEntity.userId)
                    user?.let { userEntity ->
                        val actualLikeCount = postLikeDao.getLikeCount(postEntity.id)
                        val isLiked = currentUserId?.let { 
                            postLikeDao.isPostLiked(postEntity.id, it) 
                        } ?: false
                        
                        Post(
                            id = postEntity.id,
                            userId = postEntity.userId,
                            user = userEntity.let { 
                                com.bookdot.app.domain.model.User(
                                    id = it.id,
                                    username = it.username,
                                    displayName = it.displayName,
                                    avatarUrl = it.avatarUrl,
                                    bio = it.bio,
                                    followerCount = it.followerCount,
                                    followingCount = it.followingCount,
                                    postCount = it.postCount,
                                    isFollowing = it.isFollowing,
                                    createdAt = it.createdAt
                                )
                            },
                            content = postEntity.content,
                            imageUrls = postEntity.imageUrls,
                            videoUrl = postEntity.videoUrl,
                            likeCount = actualLikeCount,
                            commentCount = postEntity.commentCount,
                            isLiked = isLiked,
                            createdAt = postEntity.createdAt
                        )
                    }
                }
                emit(posts)
            }
        }
    }
    
    override suspend fun getPostById(postId: String): Post? {
        val postEntity = postDao.getPostById(postId)
        return postEntity?.let { entity ->
            val user = userDao.getUserById(entity.userId)
            user?.let { userEntity ->
                val currentUserId = accountManager.getCurrentUserId()
                val actualLikeCount = postLikeDao.getLikeCount(entity.id)
                val isLiked = currentUserId?.let { 
                    postLikeDao.isPostLiked(entity.id, it) 
                } ?: false
                
                Post(
                    id = entity.id,
                    userId = entity.userId,
                    user = userEntity.let { 
                        com.bookdot.app.domain.model.User(
                            id = it.id,
                            username = it.username,
                            displayName = it.displayName,
                            avatarUrl = it.avatarUrl,
                            bio = it.bio,
                            followerCount = it.followerCount,
                            followingCount = it.followingCount,
                            postCount = it.postCount,
                            isFollowing = it.isFollowing,
                            createdAt = it.createdAt
                        )
                    },
                    content = entity.content,
                    imageUrls = entity.imageUrls,
                    videoUrl = entity.videoUrl,
                    likeCount = actualLikeCount,
                    commentCount = entity.commentCount,
                    isLiked = isLiked,
                    createdAt = entity.createdAt
                )
            }
        }
    }
    
    override suspend fun createPost(content: String, images: List<Uri>): Result<Post> {
        return try {
            // 현재 로그인한 사용자 정보
            val currentUserId = accountManager.getCurrentUserId() ?: return Result.failure(Exception("로그인이 필요합니다"))
            val currentUser = userDao.getUserById(currentUserId) ?: return Result.failure(Exception("사용자를 찾을 수 없습니다"))
            
            // 새 게시물 생성
            val newPost = PostEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = currentUser.id,
                content = content,
                imageUrls = emptyList(), // 이미지는 나중에 구현
                videoUrl = null,
                likeCount = 0,
                commentCount = 0,
                createdAt = System.currentTimeMillis()
            )
            
            // 데이터베이스에 저장
            postDao.insertPost(newPost)
            
            // Domain 모델로 변환해서 반환
            val domainPost = Post(
                id = newPost.id,
                userId = newPost.userId,
                user = com.bookdot.app.domain.model.User(
                    id = currentUser.id,
                    username = currentUser.username,
                    displayName = currentUser.displayName,
                    avatarUrl = currentUser.avatarUrl,
                    bio = currentUser.bio,
                    followerCount = currentUser.followerCount,
                    followingCount = currentUser.followingCount,
                    postCount = currentUser.postCount,
                    isFollowing = currentUser.isFollowing,
                    createdAt = currentUser.createdAt
                ),
                content = newPost.content,
                imageUrls = newPost.imageUrls,
                videoUrl = newPost.videoUrl,
                likeCount = newPost.likeCount,
                commentCount = newPost.commentCount,
                isLiked = false,
                createdAt = newPost.createdAt
            )
            
            Result.success(domainPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun likePost(postId: String): Result<Unit> {
        return try {
            val currentUserId = accountManager.getCurrentUserId() 
                ?: return Result.failure(Exception("로그인이 필요합니다"))
            
            val isAlreadyLiked = postLikeDao.isPostLiked(postId, currentUserId)
            
            if (isAlreadyLiked) {
                // 좋아요 취소
                postLikeDao.deletePostLike(postId, currentUserId)
            } else {
                // 좋아요 추가
                postLikeDao.insertPostLike(
                    com.bookdot.app.data.local.entities.PostLikeEntity(
                        postId = postId,
                        userId = currentUserId
                    )
                )
            }
            
            // 전체 좋아요 수 업데이트
            val newLikeCount = postLikeDao.getLikeCount(postId)
            postDao.updateLikeCount(postId, newLikeCount)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun unlikePost(postId: String): Result<Unit> {
        return try {
            val currentUserId = accountManager.getCurrentUserId() 
                ?: return Result.failure(Exception("로그인이 필요합니다"))
            
            // 좋아요 제거
            postLikeDao.deletePostLike(postId, currentUserId)
            
            // 전체 좋아요 수 업데이트
            val newLikeCount = postLikeDao.getLikeCount(postId)
            postDao.updateLikeCount(postId, newLikeCount)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            postApi.deletePost(postId)
            val post = postDao.getPostById(postId)
            post?.let { postDao.deletePost(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
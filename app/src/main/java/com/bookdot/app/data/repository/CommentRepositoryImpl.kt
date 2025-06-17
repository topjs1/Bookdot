package com.bookdot.app.data.repository

import com.bookdot.app.data.local.dao.CommentDao
import com.bookdot.app.data.local.dao.CommentLikeDao
import com.bookdot.app.data.local.dao.UserDao
import com.bookdot.app.data.local.dao.PostDao
import com.bookdot.app.data.local.entities.CommentEntity
import com.bookdot.app.data.local.entities.CommentLikeEntity
import com.bookdot.app.domain.model.Comment
import com.bookdot.app.domain.repository.CommentRepository
import com.bookdot.app.security.AccountManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao,
    private val commentLikeDao: CommentLikeDao,
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val accountManager: AccountManager
) : CommentRepository {

    override fun getCommentsByPost(postId: String): Flow<List<Comment>> {
        return flow {
            commentDao.getCommentsByPost(postId).collect { commentEntities ->
                val currentUserId = accountManager.getCurrentUserId()
                val comments = commentEntities.mapNotNull { commentEntity ->
                    val user = userDao.getUserById(commentEntity.userId)
                    user?.let { userEntity ->
                        val actualLikeCount = commentLikeDao.getLikeCount(commentEntity.id)
                        val isLiked = currentUserId?.let { 
                            commentLikeDao.isCommentLiked(commentEntity.id, it) 
                        } ?: false
                        
                        Comment(
                            id = commentEntity.id,
                            postId = commentEntity.postId,
                            userId = commentEntity.userId,
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
                            content = commentEntity.content,
                            likeCount = actualLikeCount,
                            isLiked = isLiked,
                            createdAt = commentEntity.createdAt
                        )
                    }
                }
                emit(comments)
            }
        }
    }

    override suspend fun getCommentById(commentId: String): Comment? {
        val commentEntity = commentDao.getCommentById(commentId)
        return commentEntity?.let { entity ->
            val user = userDao.getUserById(entity.userId)
            user?.let { userEntity ->
                val currentUserId = accountManager.getCurrentUserId()
                val actualLikeCount = commentLikeDao.getLikeCount(entity.id)
                val isLiked = currentUserId?.let { 
                    commentLikeDao.isCommentLiked(entity.id, it) 
                } ?: false
                
                Comment(
                    id = entity.id,
                    postId = entity.postId,
                    userId = entity.userId,
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
                    content = entity.content,
                    likeCount = actualLikeCount,
                    isLiked = isLiked,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun createComment(postId: String, content: String): Result<Comment> {
        return try {
            val currentUserId = accountManager.getCurrentUserId() 
                ?: return Result.failure(Exception("로그인이 필요합니다"))
            val currentUser = userDao.getUserById(currentUserId) 
                ?: return Result.failure(Exception("사용자를 찾을 수 없습니다"))

            // 새 댓글 생성
            val newComment = CommentEntity(
                id = java.util.UUID.randomUUID().toString(),
                postId = postId,
                userId = currentUser.id,
                content = content,
                likeCount = 0,
                createdAt = System.currentTimeMillis()
            )

            // 댓글 저장
            commentDao.insertComment(newComment)

            // 게시물의 댓글 수 업데이트
            val commentCount = commentDao.getCommentCount(postId)
            postDao.getPostById(postId)?.let { post ->
                val updatedPost = post.copy(commentCount = commentCount)
                postDao.updatePost(updatedPost)
            }

            // Domain 모델로 변환해서 반환
            val domainComment = Comment(
                id = newComment.id,
                postId = newComment.postId,
                userId = newComment.userId,
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
                content = newComment.content,
                likeCount = 0,
                isLiked = false,
                createdAt = newComment.createdAt
            )

            Result.success(domainComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likeComment(commentId: String): Result<Unit> {
        return try {
            val currentUserId = accountManager.getCurrentUserId() 
                ?: return Result.failure(Exception("로그인이 필요합니다"))
            
            val isAlreadyLiked = commentLikeDao.isCommentLiked(commentId, currentUserId)
            
            if (isAlreadyLiked) {
                // 좋아요 취소
                commentLikeDao.deleteCommentLike(commentId, currentUserId)
            } else {
                // 좋아요 추가
                commentLikeDao.insertCommentLike(
                    CommentLikeEntity(
                        commentId = commentId,
                        userId = currentUserId
                    )
                )
            }
            
            // 전체 좋아요 수 업데이트
            val newLikeCount = commentLikeDao.getLikeCount(commentId)
            commentDao.updateLikeCount(commentId, newLikeCount)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikeComment(commentId: String): Result<Unit> {
        return try {
            val currentUserId = accountManager.getCurrentUserId() 
                ?: return Result.failure(Exception("로그인이 필요합니다"))
            
            // 좋아요 제거
            commentLikeDao.deleteCommentLike(commentId, currentUserId)
            
            // 전체 좋아요 수 업데이트
            val newLikeCount = commentLikeDao.getLikeCount(commentId)
            commentDao.updateLikeCount(commentId, newLikeCount)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            val currentUserId = accountManager.getCurrentUserId() 
                ?: return Result.failure(Exception("로그인이 필요합니다"))
            
            val comment = commentDao.getCommentById(commentId)
                ?: return Result.failure(Exception("댓글을 찾을 수 없습니다"))
            
            // 자신의 댓글만 삭제 가능
            if (comment.userId != currentUserId) {
                return Result.failure(Exception("자신의 댓글만 삭제할 수 있습니다"))
            }
            
            // 댓글 삭제
            commentDao.deleteComment(comment)
            
            // 게시물의 댓글 수 업데이트
            val commentCount = commentDao.getCommentCount(comment.postId)
            postDao.getPostById(comment.postId)?.let { post ->
                val updatedPost = post.copy(commentCount = commentCount)
                postDao.updatePost(updatedPost)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
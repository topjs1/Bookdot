package com.bookdot.app.data.firebase.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.bookdot.app.data.firebase.model.FirebaseComment
import com.bookdot.app.data.firebase.model.FirebaseUser
import com.bookdot.app.data.firebase.model.FirebaseLike
import com.bookdot.app.domain.model.Comment
import com.bookdot.app.domain.repository.CommentRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCommentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : CommentRepository {

    override fun getCommentsByPost(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    launch {
                        val comments = snapshot.documents.mapNotNull { doc ->
                            val firebaseComment = doc.toObject(FirebaseComment::class.java)
                            firebaseComment?.let { convertToComment(it) }
                        }
                        trySend(comments)
                    }
                }
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getCommentById(commentId: String): Comment? {
        return try {
            val doc = firestore.collection("comments").document(commentId).get().await()
            val firebaseComment = doc.toObject(FirebaseComment::class.java)
            firebaseComment?.let { convertToComment(it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createComment(postId: String, content: String): Result<Comment> {
        return try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("로그인이 필요합니다")
            
            // 새 댓글 생성
            val newComment = FirebaseComment(
                postId = postId,
                userId = currentUser.uid,
                content = content,
                likeCount = 0
            )
            
            // Firestore에 저장
            val docRef = firestore.collection("comments").add(newComment).await()
            val commentWithId = newComment.copy(id = docRef.id)
            
            // 해당 게시물의 commentCount 증가
            firestore.collection("posts").document(postId)
                .update("commentCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            
            val domainComment = convertToComment(commentWithId)
            Result.success(domainComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likeComment(commentId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("로그인이 필요합니다")
            
            val likeId = "${commentId}_${currentUser.uid}"
            val likeDoc = firestore.collection("likes").document(likeId)
            val likeSnapshot = likeDoc.get().await()
            
            if (likeSnapshot.exists()) {
                // 좋아요 취소
                likeDoc.delete().await()
                firestore.collection("comments").document(commentId)
                    .update("likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
            } else {
                // 좋아요 추가
                val like = FirebaseLike(
                    id = likeId,
                    targetId = commentId,
                    targetType = FirebaseLike.TYPE_COMMENT,
                    userId = currentUser.uid
                )
                likeDoc.set(like).await()
                firestore.collection("comments").document(commentId)
                    .update("likeCount", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikeComment(commentId: String): Result<Unit> {
        return likeComment(commentId) // 같은 로직으로 토글
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("로그인이 필요합니다")
            
            // 댓글 소유권 확인
            val commentDoc = firestore.collection("comments").document(commentId).get().await()
            val comment = commentDoc.toObject(FirebaseComment::class.java)
            
            if (comment?.userId != currentUser.uid) {
                throw Exception("자신의 댓글만 삭제할 수 있습니다")
            }
            
            // 댓글 삭제
            firestore.collection("comments").document(commentId).delete().await()
            
            // 해당 게시물의 commentCount 감소
            firestore.collection("posts").document(comment.postId)
                .update("commentCount", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun convertToComment(firebaseComment: FirebaseComment): Comment {
        // 사용자 정보 가져오기
        val userDoc = firestore.collection("users").document(firebaseComment.userId).get().await()
        val firebaseUser = userDoc.toObject(FirebaseUser::class.java)
        
        // 현재 사용자의 좋아요 상태 확인
        val currentUser = firebaseAuth.currentUser
        var isLiked = false
        if (currentUser != null) {
            val likeId = "${firebaseComment.id}_${currentUser.uid}"
            val likeDoc = firestore.collection("likes").document(likeId).get().await()
            isLiked = likeDoc.exists()
        }
        
        return Comment(
            id = firebaseComment.id,
            postId = firebaseComment.postId,
            userId = firebaseComment.userId,
            user = firebaseUser?.toDomainModel() ?: com.bookdot.app.domain.model.User(
                id = firebaseComment.userId,
                username = "unknown",
                displayName = "Unknown User",
                avatarUrl = null,
                bio = "",
                followerCount = 0,
                followingCount = 0,
                postCount = 0,
                isFollowing = false,
                createdAt = 0L
            ),
            content = firebaseComment.content,
            likeCount = firebaseComment.likeCount,
            isLiked = isLiked,
            createdAt = firebaseComment.createdAt?.time ?: 0L
        )
    }
}
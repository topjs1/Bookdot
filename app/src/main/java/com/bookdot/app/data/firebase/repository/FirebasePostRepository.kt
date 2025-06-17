package com.bookdot.app.data.firebase.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.bookdot.app.data.firebase.model.FirebasePost
import com.bookdot.app.data.firebase.model.FirebaseUser
import com.bookdot.app.data.firebase.model.FirebaseLike
import com.bookdot.app.domain.model.Post
import com.bookdot.app.domain.repository.PostRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : PostRepository {

    override fun getFeed(): Flow<List<Post>> = callbackFlow {
        val listener = firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    launch {
                        val posts = snapshot.documents.mapNotNull { doc ->
                            val firebasePost = doc.toObject(FirebasePost::class.java)
                            firebasePost?.let { convertToPost(it) }
                        }
                        trySend(posts)
                    }
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun getPostsByUser(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    launch {
                        val posts = snapshot.documents.mapNotNull { doc ->
                            val firebasePost = doc.toObject(FirebasePost::class.java)
                            firebasePost?.let { convertToPost(it) }
                        }
                        trySend(posts)
                    }
                }
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getPostById(postId: String): Post? {
        return try {
            val doc = firestore.collection("posts").document(postId).get().await()
            val firebasePost = doc.toObject(FirebasePost::class.java)
            firebasePost?.let { convertToPost(it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createPost(content: String, images: List<Uri>): Result<Post> {
        return try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("로그인이 필요합니다")
            
            // 새 게시물 생성
            val newPost = FirebasePost(
                userId = currentUser.uid,
                content = content,
                imageUrls = emptyList(), // 이미지는 나중에 구현
                videoUrl = null,
                likeCount = 0,
                commentCount = 0
            )
            
            // Firestore에 저장
            val docRef = firestore.collection("posts").add(newPost).await()
            val postWithId = newPost.copy(id = docRef.id)
            
            // 사용자의 postCount 증가
            firestore.collection("users").document(currentUser.uid)
                .update("postCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            
            val domainPost = convertToPost(postWithId)
            Result.success(domainPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likePost(postId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("로그인이 필요합니다")
            
            val likeId = "${postId}_${currentUser.uid}"
            val likeDoc = firestore.collection("likes").document(likeId)
            val likeSnapshot = likeDoc.get().await()
            
            if (likeSnapshot.exists()) {
                // 좋아요 취소
                likeDoc.delete().await()
                firestore.collection("posts").document(postId)
                    .update("likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
            } else {
                // 좋아요 추가
                val like = FirebaseLike(
                    id = likeId,
                    targetId = postId,
                    targetType = FirebaseLike.TYPE_POST,
                    userId = currentUser.uid
                )
                likeDoc.set(like).await()
                firestore.collection("posts").document(postId)
                    .update("likeCount", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikePost(postId: String): Result<Unit> {
        return likePost(postId) // 같은 로직으로 토글
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("로그인이 필요합니다")
            
            // 게시물 소유권 확인
            val postDoc = firestore.collection("posts").document(postId).get().await()
            val post = postDoc.toObject(FirebasePost::class.java)
            
            if (post?.userId != currentUser.uid) {
                throw Exception("자신의 게시물만 삭제할 수 있습니다")
            }
            
            // 게시물 삭제
            firestore.collection("posts").document(postId).delete().await()
            
            // 사용자의 postCount 감소
            firestore.collection("users").document(currentUser.uid)
                .update("postCount", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun convertToPost(firebasePost: FirebasePost): Post {
        // 사용자 정보 가져오기
        val userDoc = firestore.collection("users").document(firebasePost.userId).get().await()
        val firebaseUser = userDoc.toObject(FirebaseUser::class.java)
        
        // 현재 사용자의 좋아요 상태 확인
        val currentUser = firebaseAuth.currentUser
        var isLiked = false
        if (currentUser != null) {
            val likeId = "${firebasePost.id}_${currentUser.uid}"
            val likeDoc = firestore.collection("likes").document(likeId).get().await()
            isLiked = likeDoc.exists()
        }
        
        return Post(
            id = firebasePost.id,
            userId = firebasePost.userId,
            user = firebaseUser?.toDomainModel() ?: com.bookdot.app.domain.model.User(
                id = firebasePost.userId,
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
            content = firebasePost.content,
            imageUrls = firebasePost.imageUrls,
            videoUrl = firebasePost.videoUrl,
            likeCount = firebasePost.likeCount,
            commentCount = firebasePost.commentCount,
            isLiked = isLiked,
            createdAt = firebasePost.createdAt?.time ?: 0L
        )
    }
}
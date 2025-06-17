package com.bookdot.app.data.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.bookdot.app.data.firebase.model.FirebaseUser as AppFirebaseUser
import com.bookdot.app.domain.model.AuthUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
    
    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null
    
    suspend fun createAccountWithAccountId(): Result<AuthUser> {
        return try {
            val accountId = generateAccountId()
            val defaultName = "User${Random.nextInt(1000, 9999)}"
            
            // Firebase Anonymous Auth로 익명 계정 생성
            val authResult = firebaseAuth.signInAnonymously().await()
            val firebaseUser = authResult.user ?: throw Exception("계정 생성에 실패했습니다")
            
            // Firestore에 사용자 정보 저장
            val appUser = AppFirebaseUser(
                id = firebaseUser.uid,
                username = accountId.replace("-", ""),
                displayName = defaultName,
                avatarUrl = null,
                bio = "새로운 Book dot 독자입니다!",
                followerCount = 0,
                followingCount = 0,
                postCount = 0
            )
            
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(appUser)
                .await()
            
            // Custom Claims에 accountId 저장
            firestore.collection("userAccountIds")
                .document(accountId)
                .set(mapOf("uid" to firebaseUser.uid))
                .await()
            
            val authUser = AuthUser(
                accountId = accountId,
                displayName = defaultName,
                isLoggedIn = false, // 로그인은 별도로 해야 함
                createdAt = System.currentTimeMillis()
            )
            
            // 일단 로그아웃
            firebaseAuth.signOut()
            
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginWithAccountId(accountId: String): Result<AuthUser> {
        return try {
            // accountId로 uid 찾기
            val accountDoc = firestore.collection("userAccountIds")
                .document(accountId)
                .get()
                .await()
            
            if (!accountDoc.exists()) {
                throw Exception("계정을 찾을 수 없습니다")
            }
            
            val uid = accountDoc.getString("uid") ?: throw Exception("잘못된 계정 정보입니다")
            
            // 사용자 정보 가져오기
            val userDoc = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            
            if (!userDoc.exists()) {
                throw Exception("사용자 정보를 찾을 수 없습니다")
            }
            
            val firebaseUser = userDoc.toObject(AppFirebaseUser::class.java)
                ?: throw Exception("사용자 정보를 읽을 수 없습니다")
            
            // 기존 익명 계정이 있다면 로그아웃
            firebaseAuth.signOut()
            
            // 새로운 익명 계정으로 로그인 (실제로는 기존 uid와 연결)
            val authResult = firebaseAuth.signInAnonymously().await()
            val currentUser = authResult.user ?: throw Exception("로그인에 실패했습니다")
            
            // 현재 uid를 기존 계정 정보로 업데이트
            firestore.collection("userAccountIds")
                .document(accountId)
                .set(mapOf("uid" to currentUser.uid))
                .await()
            
            // 사용자 정보를 새 uid로 복사
            firestore.collection("users")
                .document(currentUser.uid)
                .set(firebaseUser.copy(id = currentUser.uid))
                .await()
            
            val authUser = AuthUser(
                accountId = accountId,
                displayName = firebaseUser.displayName,
                isLoggedIn = true,
                createdAt = firebaseUser.createdAt?.time ?: System.currentTimeMillis()
            )
            
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateDisplayName(newName: String): Result<AuthUser> {
        return try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("로그인이 필요합니다")
            
            // Firestore에서 사용자 정보 업데이트
            firestore.collection("users")
                .document(currentUser.uid)
                .update("displayName", newName)
                .await()
            
            // accountId 가져오기 (역참조)
            val accountQuery = firestore.collection("userAccountIds")
                .whereEqualTo("uid", currentUser.uid)
                .get()
                .await()
            
            val accountId = accountQuery.documents.firstOrNull()?.id ?: ""
            
            val authUser = AuthUser(
                accountId = accountId,
                displayName = newName,
                isLoggedIn = true,
                createdAt = System.currentTimeMillis()
            )
            
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        firebaseAuth.signOut()
    }
    
    private fun generateAccountId(): String {
        val parts = mutableListOf<String>()
        repeat(4) {
            val part = Random.nextInt(1000, 9999).toString()
            parts.add(part)
        }
        return parts.joinToString("-")
    }
}
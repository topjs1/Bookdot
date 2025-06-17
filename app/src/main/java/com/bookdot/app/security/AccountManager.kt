package com.bookdot.app.security

import android.content.Context
import com.bookdot.app.data.firebase.auth.FirebaseAuthManager
import com.bookdot.app.domain.model.AuthUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuthManager: FirebaseAuthManager
) {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()
    
    suspend fun createAccount(): Result<AuthUser> {
        return try {
            val result = firebaseAuthManager.createAccountWithAccountId()
            if (result.isSuccess) {
                val authUser = result.getOrThrow()
                // 계정 생성 후 자동 로그인 안함
                Result.success(authUser)
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(accountId: String): Result<AuthUser> {
        return try {
            val result = firebaseAuthManager.loginWithAccountId(accountId)
            if (result.isSuccess) {
                val authUser = result.getOrThrow()
                saveCurrentUser(authUser)
                Result.success(authUser)
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateDisplayName(newName: String): Result<AuthUser> {
        return try {
            val result = firebaseAuthManager.updateDisplayName(newName)
            if (result.isSuccess) {
                val authUser = result.getOrThrow()
                saveCurrentUser(authUser)
                Result.success(authUser)
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        firebaseAuthManager.logout()
        _currentUser.value = null
    }
    
    suspend fun checkLoginStatus(): AuthUser? {
        return if (firebaseAuthManager.isLoggedIn()) {
            val currentUser = firebaseAuthManager.getCurrentUser()
            if (currentUser != null) {
                // Firebase에서 사용자 정보를 가져와서 AuthUser로 변환
                // 여기서는 간단히 처리하고 실제로는 Firestore에서 사용자 정보를 가져와야 함
                val authUser = AuthUser(
                    accountId = currentUser.uid,
                    displayName = currentUser.displayName ?: "Unknown User",
                    isLoggedIn = true,
                    createdAt = System.currentTimeMillis()
                )
                saveCurrentUser(authUser)
                authUser
            } else {
                null
            }
        } else {
            null
        }
    }
    
    private fun saveCurrentUser(user: AuthUser) {
        _currentUser.value = user
    }
    
    fun isLoggedIn(): Boolean {
        return firebaseAuthManager.isLoggedIn()
    }
    
    fun getCurrentUserId(): String? {
        return firebaseAuthManager.getCurrentUser()?.uid
    }
}
package com.bookdot.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookdot.app.domain.model.AuthUser
import com.bookdot.app.security.AccountManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val accountManager: AccountManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    // 자동 로그인 비활성화 - 사용자가 수동으로 로그인해야 함
    
    fun createAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            accountManager.createAccount()
                .onSuccess { authUser ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            currentUser = authUser,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "계정 생성에 실패했습니다"
                        )
                    }
                }
        }
    }
    
    fun login(accountId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            accountManager.login(accountId)
                .onSuccess { authUser ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentUser = authUser,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "로그인에 실패했습니다"
                        )
                    }
                }
        }
    }
    
    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            accountManager.updateDisplayName(newName)
                .onSuccess { authUser ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentUser = authUser,
                            isNameUpdated = true,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "이름 변경에 실패했습니다"
                        )
                    }
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            accountManager.logout()
            _uiState.update { 
                AuthUiState(currentUser = null)
            }
        }
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val user = accountManager.checkLoginStatus()
            _uiState.update { it.copy(currentUser = user) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AuthUiState(
    val currentUser: AuthUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isNameUpdated: Boolean = false
)
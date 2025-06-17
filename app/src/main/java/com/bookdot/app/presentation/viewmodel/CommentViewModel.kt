package com.bookdot.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookdot.app.domain.model.Comment
import com.bookdot.app.domain.repository.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val accountManager: com.bookdot.app.security.AccountManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()
    
    fun loadComments(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                commentRepository.getCommentsByPost(postId).collect { comments ->
                    _uiState.update { 
                        it.copy(
                            comments = comments,
                            isLoading = false,
                            postId = postId
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "댓글을 불러오는데 실패했습니다"
                    )
                }
            }
        }
    }
    
    fun createComment(content: String) {
        val postId = _uiState.value.postId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingComment = true, error = null) }
            
            commentRepository.createComment(postId, content)
                .onSuccess { comment ->
                    _uiState.update { 
                        it.copy(
                            isCreatingComment = false,
                            newCommentContent = ""
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isCreatingComment = false,
                            error = error.message ?: "댓글 작성에 실패했습니다"
                        )
                    }
                }
        }
    }
    
    fun likeComment(commentId: String) {
        viewModelScope.launch {
            commentRepository.likeComment(commentId)
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(error = error.message ?: "좋아요 처리에 실패했습니다")
                    }
                }
        }
    }
    
    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            commentRepository.deleteComment(commentId)
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(error = error.message ?: "댓글 삭제에 실패했습니다")
                    }
                }
        }
    }
    
    fun updateNewCommentContent(content: String) {
        _uiState.update { it.copy(newCommentContent = content) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun getCurrentUserId(): String? {
        return accountManager.getCurrentUserId()
    }
}

data class CommentUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingComment: Boolean = false,
    val newCommentContent: String = "",
    val postId: String? = null,
    val error: String? = null
)
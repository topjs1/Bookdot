package com.bookdot.app.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookdot.app.domain.model.Post
import com.bookdot.app.domain.usecase.CreatePostUseCase
import com.bookdot.app.domain.usecase.GetFeedUseCase
import com.bookdot.app.domain.usecase.LikePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedUseCase: GetFeedUseCase,
    private val createPostUseCase: CreatePostUseCase,
    private val likePostUseCase: LikePostUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    
    init {
        loadFeed()
    }
    
    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getFeedUseCase()
                .catch { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = error.message)
                    }
                }
                .collect { posts ->
                    _uiState.update { 
                        it.copy(isLoading = false, posts = posts, error = null)
                    }
                }
        }
    }
    
    fun likePost(postId: String) {
        val currentPosts = _uiState.value.posts
        val post = currentPosts.find { it.id == postId } ?: return
        
        viewModelScope.launch {
            likePostUseCase(postId, post.isLiked)
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(error = error.message)
                    }
                }
        }
    }
    
    fun createPost(content: String, images: List<Uri>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingPost = true) }
            
            createPostUseCase(content, images)
                .onSuccess { post ->
                    _uiState.update { 
                        it.copy(
                            isCreatingPost = false,
                            posts = listOf(post) + it.posts
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(isCreatingPost = false, error = error.message)
                    }
                }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingPost: Boolean = false,
    val error: String? = null
)